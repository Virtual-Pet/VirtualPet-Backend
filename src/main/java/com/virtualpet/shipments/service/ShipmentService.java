package com.virtualpet.shipments.service;

import com.virtualpet.common.exception.ApiException;
import com.virtualpet.common.pagination.Cursor;
import com.virtualpet.common.pagination.CursorCodec;
import com.virtualpet.common.pagination.CursorPage;
import com.virtualpet.orders.domain.OrderEntity;
import com.virtualpet.orders.repository.OrderRepository;
import com.virtualpet.shipments.domain.ShipmentEntity;
import com.virtualpet.shipments.domain.ShipmentStatus;
import com.virtualpet.shipments.domain.ShipmentStatusHistoryEntity;
import com.virtualpet.shipments.dto.ShipmentDTO.ShipmentResponseDTO;
import com.virtualpet.shipments.dto.ShipmentDTO.ShipmentStatusEventDTO;
import com.virtualpet.shipments.dto.ShipmentDTO.ShipmentSummaryDTO;
import com.virtualpet.shipments.repository.ShipmentRepository;
import com.virtualpet.shipments.repository.ShipmentStatusHistoryRepository;
import com.virtualpet.shipments.spec.ShipmentSpecifications;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.repository.UserRepository;
import com.virtualpet.auth.domain.CustomerEntity;
import com.virtualpet.auth.repository.CustomerRepository;
import java.util.stream.Collectors;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Read + advance endpoints for shipments. CUSTOMER callers (and any {@code ?user=me} request) are
 * scoped to shipments whose underlying order belongs to them; EMPLOYEE/ADMIN see everything.
 * Cancellation is owned by {@code /orders/{id}/cancel} — this service only advances forward through
 * the logistics flow.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {

  private static final Map<ShipmentStatus, ShipmentStatus> NEXT_ALLOWED =
      new EnumMap<>(ShipmentStatus.class);

  static {
    NEXT_ALLOWED.put(ShipmentStatus.CONFIRMED, ShipmentStatus.PREPARED);
    NEXT_ALLOWED.put(ShipmentStatus.PREPARED, ShipmentStatus.IN_TRANSIT);
    NEXT_ALLOWED.put(ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERED);
  }

  private static final EnumSet<ShipmentStatus> ADVANCE_TARGETS =
      EnumSet.of(ShipmentStatus.PREPARED, ShipmentStatus.IN_TRANSIT, ShipmentStatus.DELIVERED);

  private final ShipmentRepository shipmentRepository;
  private final ShipmentStatusHistoryRepository historyRepository;
  private final OrderRepository orderRepository;
  private final CursorCodec cursorCodec;
  private final UserRepository userRepository;
  private final CustomerRepository customerRepository;

  /* ---------- List ---------- */

  @Transactional(readOnly = true)
  public CursorPage<ShipmentSummaryDTO> list(
      UUID callerId,
      boolean isCustomer,
      boolean userIsMe,
      ShipmentStatus status,
      String cursor,
      int limit) {
    int effectiveLimit = clampLimit(limit);
    Cursor decoded = cursorCodec.decode(cursor);

    UUID userScope = (isCustomer || userIsMe) ? callerId : null;

    Specification<ShipmentEntity> spec =
        Specification.allOf(
            Stream.of(
                    ShipmentSpecifications.byStatus(status),
                    ShipmentSpecifications.ownedByUser(userScope),
                    ShipmentSpecifications.afterCursor(decoded))
                .filter(Objects::nonNull)
                .toList());

    Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    List<ShipmentEntity> rows =
        shipmentRepository.findAll(spec, PageRequest.of(0, effectiveLimit + 1, sort)).getContent();

    boolean hasMore = rows.size() > effectiveLimit;
    if (hasMore) {
      rows = rows.subList(0, effectiveLimit);
    }

    List<UUID> orderIds = rows.stream().map(ShipmentEntity::getOrderId).distinct().toList();
    List<OrderEntity> orders = orderRepository.findAllById(orderIds);
    Map<UUID, OrderEntity> orderMap = orders.stream().collect(Collectors.toMap(OrderEntity::getId, o -> o));

    List<UUID> userIds = orders.stream().map(OrderEntity::getUserId).distinct().toList();
    List<UserEntity> users = userRepository.findAllById(userIds);
    Map<UUID, UserEntity> userMap = users.stream().collect(Collectors.toMap(UserEntity::getId, u -> u));

    List<CustomerEntity> customers = customerRepository.findAllById(userIds);
    Map<UUID, CustomerEntity> customerMap = customers.stream().collect(Collectors.toMap(CustomerEntity::getUserId, c -> c));

    List<ShipmentSummaryDTO> data =
        rows.stream()
            .map(
                s -> {
                    OrderEntity order = orderMap.get(s.getOrderId());
                    UserEntity user = order != null ? userMap.get(order.getUserId()) : null;
                    CustomerEntity customer = order != null ? customerMap.get(order.getUserId()) : null;

                    String cName = "Invitado";
                    if (order != null && order.getContactName() != null && !order.getContactName().isBlank()) {
                        cName = order.getContactName() + (order.getContactLastname() != null ? " " + order.getContactLastname() : "");
                    } else if (customer != null) {
                        cName = customer.getName() + " " + customer.getLastname();
                    } else if (user != null) {
                        cName = user.getEmail();
                    }

                    String cEmail = "guest@virtualpet.com";
                    if (order != null && order.getContactEmail() != null && !order.getContactEmail().isBlank()) {
                        cEmail = order.getContactEmail();
                    } else if (user != null) {
                        cEmail = user.getEmail();
                    }

                    return new ShipmentSummaryDTO(
                        s.getId(),
                        s.getOrderId(),
                        s.getStatus(),
                        s.getUpdatedAt() == null ? s.getCreatedAt() : s.getUpdatedAt(),
                        cName,
                        cEmail,
                        order != null ? order.getTotal() : java.math.BigDecimal.ZERO);
                })
            .toList();

    String nextCursor =
        hasMore
            ? cursorCodec.encode(Cursor.of(rows.getLast().getCreatedAt(), rows.getLast().getId()))
            : null;
    return CursorPage.of(data, effectiveLimit, nextCursor);
  }

  /* ---------- Detail ---------- */

  @Transactional(readOnly = true)
  public ShipmentResponseDTO getById(UUID id, UUID callerId, boolean isCustomer) {
    ShipmentEntity shipment = loadAccessible(id, callerId, isCustomer);
    OrderEntity order =
        orderRepository
            .findById(shipment.getOrderId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shipment not found"));
    List<ShipmentStatusEventDTO> history =
        historyRepository.findByShipmentIdOrderByCreatedAtAsc(shipment.getId()).stream()
            .map(h -> new ShipmentStatusEventDTO(parseStatus(h.getNewStatus()), h.getCreatedAt()))
            .filter(e -> e.status() != null)
            .toList();
    return new ShipmentResponseDTO(
        shipment.getId(),
        shipment.getOrderId(),
        shipment.getStatus(),
        order.getShippingAddress(),
        history);
  }

  /* ---------- Advance ---------- */

  @Transactional
  public ShipmentResponseDTO advance(UUID id, ShipmentStatus target, UUID operatorId) {
    if (!ADVANCE_TARGETS.contains(target)) {
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_CONTENT, "Allowed transitions: PREPARED, IN_TRANSIT, DELIVERED");
    }
    ShipmentEntity shipment =
        shipmentRepository
            .findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shipment not found"));

    ShipmentStatus expected = NEXT_ALLOWED.get(shipment.getStatus());
    if (expected != target) {
      throw new ApiException(
          HttpStatus.CONFLICT, "Invalid transition: " + shipment.getStatus() + " → " + target);
    }

    ShipmentStatus previous = shipment.getStatus();
    shipment.setStatus(target);
    shipmentRepository.save(shipment);
    historyRepository.save(
        ShipmentStatusHistoryEntity.builder()
            .shipmentId(shipment.getId())
            .prevStatus(previous.name())
            .newStatus(target.name())
            .modifiedBy(operatorId)
            .build());
    log.info(
        "Shipment {} transitioned: {} → {} (operator={})",
        shipment.getId(),
        previous,
        target,
        operatorId);
    return getByIdInternal(shipment);
  }

  /* ---------- Internals ---------- */

  private ShipmentResponseDTO getByIdInternal(ShipmentEntity shipment) {
    OrderEntity order = orderRepository.findById(shipment.getOrderId()).orElse(null);
    List<ShipmentStatusEventDTO> history =
        historyRepository.findByShipmentIdOrderByCreatedAtAsc(shipment.getId()).stream()
            .map(h -> new ShipmentStatusEventDTO(parseStatus(h.getNewStatus()), h.getCreatedAt()))
            .filter(e -> e.status() != null)
            .toList();
    return new ShipmentResponseDTO(
        shipment.getId(),
        shipment.getOrderId(),
        shipment.getStatus(),
        order == null ? null : order.getShippingAddress(),
        history);
  }

  private ShipmentEntity loadAccessible(UUID id, UUID callerId, boolean isCustomer) {
    ShipmentEntity shipment =
        shipmentRepository
            .findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shipment not found"));
    if (isCustomer) {
      OrderEntity order =
          orderRepository
              .findById(shipment.getOrderId())
              .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Shipment not found"));
      if (!order.getUserId().equals(callerId)) {
        throw new ApiException(HttpStatus.NOT_FOUND, "Shipment not found");
      }
    }
    return shipment;
  }

  private int clampLimit(int requested) {
    if (requested <= 0) {
      return 20;
    }
    return Math.min(requested, 100);
  }

  private static ShipmentStatus parseStatus(String raw) {
    if (raw == null) {
      return null;
    }
    try {
      return ShipmentStatus.valueOf(raw);
    } catch (IllegalArgumentException ex) {
      return null; // legacy label in the audit log — skip
    }
  }

  /** Records the initial status when a shipment is first created (used by confirm orchestrator). */
  @Transactional
  public void recordInitialStatus(UUID shipmentId, ShipmentStatus status, UUID actor) {
    historyRepository.save(
        ShipmentStatusHistoryEntity.builder()
            .shipmentId(shipmentId)
            .prevStatus(status.name())
            .newStatus(status.name())
            .modifiedBy(actor)
            .build());
  }

  /** Records a CANCELLED transition (used by cancel orchestrator). */
  @Transactional
  public void recordCancellation(
      UUID shipmentId, ShipmentStatus previous, String reason, UUID actor) {
    historyRepository.save(
        ShipmentStatusHistoryEntity.builder()
            .shipmentId(shipmentId)
            .prevStatus(previous.name())
            .newStatus(ShipmentStatus.CANCELLED.name())
            .reason(reason)
            .modifiedBy(actor)
            .build());
  }
}
