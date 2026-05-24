package com.virtualpet.shipments.service;

import com.virtualpet.orders.repository.OrderRepository;
import com.virtualpet.common.exception.ApiException;
import com.virtualpet.shipments.domain.ShipmentEntity;
import com.virtualpet.shipments.domain.ShipmentStatus;
import com.virtualpet.shipments.dto.LogisticsDTO.PendingOrderResponse;
import com.virtualpet.shipments.dto.LogisticsDTO.ShipmentStatusResponse;
import com.virtualpet.shipments.repository.ShipmentRepository;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsService {

  private static final int DEFAULT_WAREHOUSE_ID = 1; // Depósito Central MdP

  private final ShipmentRepository shipmentRepository;
  private final OrderRepository orderRepository;

  // -------------------------------------------------------------------------
  // Called by CheckoutService after successful payment
  // -------------------------------------------------------------------------

  @Transactional
  public void createShipmentForOrder(UUID orderId) {
    // Idempotent: don't create duplicate shipments
    if (shipmentRepository.findByOrderId(orderId).isPresent()) {
      log.warn("Shipment already exists for orderId={}", orderId);
      return;
    }
    ShipmentEntity shipment =
        ShipmentEntity.builder()
            .orderId(orderId)
            .warehouseId(DEFAULT_WAREHOUSE_ID)
            .courier("OWN_DELIVERY")
            .status(ShipmentStatus.PENDING)
            .attempts((short) 0)
            .build();
    shipmentRepository.save(shipment);
    log.info("Shipment created for orderId={}", orderId);
  }

  // -------------------------------------------------------------------------
  // Backoffice: list orders by shipment status
  // -------------------------------------------------------------------------

  @Transactional(readOnly = true)
  public List<PendingOrderResponse> listPendingOrders() {
    return listByStatus(ShipmentStatus.PENDING);
  }

  @Transactional(readOnly = true)
  public List<PendingOrderResponse> listByStatus(ShipmentStatus status) {
    return shipmentRepository.findByStatusOrderByCreatedAtAsc(status).stream()
        .map(this::buildPendingResponse)
        .toList();
  }

  // -------------------------------------------------------------------------
  // Backoffice: advance shipment status
  // -------------------------------------------------------------------------

  @Transactional
  public ShipmentStatusResponse updateStatus(UUID orderId, String newStatusStr, UUID operatorId) {
    ShipmentStatus newStatus;
    try {
      newStatus = ShipmentStatus.valueOf(newStatusStr.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "Estado inválido: " + newStatusStr);
    }

    ShipmentEntity shipment =
        shipmentRepository
            .findByOrderIdForUpdate(orderId)
            .orElseThrow(
                () ->
                    new ApiException(
                        HttpStatus.NOT_FOUND, "Envío no encontrado para orderId=" + orderId));

    validateTransition(shipment.getStatus(), newStatus);
    shipment.setStatus(newStatus);
    ShipmentEntity saved = shipmentRepository.save(shipment);

    log.info(
        "Shipment status updated: orderId={}, {} -> {}, operator={}",
        orderId,
        shipment.getStatus(),
        newStatus,
        operatorId);

    return new ShipmentStatusResponse(
        saved.getId(),
        saved.getOrderId(),
        saved.getStatus().name(),
        saved.getUpdatedAt() != null ? saved.getUpdatedAt().toString() : "");
  }

  // -------------------------------------------------------------------------
  // Helpers
  // -------------------------------------------------------------------------

  private PendingOrderResponse buildPendingResponse(ShipmentEntity s) {
    return orderRepository
        .findById(s.getOrderId())
        .map(
            o ->
                new PendingOrderResponse(
                    s.getId(),
                    s.getOrderId(),
                    s.getStatus().name(),
                    o.getContactName() + " " + o.getContactLastname(),
                    o.getContactEmail(),
                    o.getTotal(),
                    o.getCreatedAt().toString()))
        .orElseGet(
            () ->
                new PendingOrderResponse(
                    s.getId(), s.getOrderId(), s.getStatus().name(), "N/A", "N/A", null, ""));
  }

  private void validateTransition(ShipmentStatus current, ShipmentStatus next) {
    // Simple linear flow: PENDING → IN_PREPARATION → PREPARED → SHIPPED → DELIVERED
    // CANCELED is always allowed from any non-terminal state
    if (next == ShipmentStatus.CANCELED) return;

    boolean valid =
        switch (current) {
          case PENDING -> next == ShipmentStatus.IN_PREPARATION;
          case IN_PREPARATION -> next == ShipmentStatus.PREPARED;
          case PREPARED -> next == ShipmentStatus.SHIPPED;
          case SHIPPED -> next == ShipmentStatus.DELIVERED || next == ShipmentStatus.FAILED;
          default -> false;
        };

    if (!valid) {
      throw new ApiException(HttpStatus.CONFLICT, "Transición inválida: " + current + " → " + next);
    }
  }
}
