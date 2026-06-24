package com.virtualpet.shipments.controller;

import com.virtualpet.common.pagination.CursorPage;
import com.virtualpet.common.security.UserPrincipal;
import com.virtualpet.shipments.domain.ShipmentStatus;
import com.virtualpet.shipments.dto.ShipmentDTO.AdvanceShipmentRequestDTO;
import com.virtualpet.shipments.dto.ShipmentDTO.ShipmentResponseDTO;
import com.virtualpet.shipments.dto.ShipmentDTO.ShipmentSummaryDTO;
import com.virtualpet.shipments.service.ShipmentService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/shipments")
@RequiredArgsConstructor
public class ShipmentController {

  private final ShipmentService shipmentService;

  @GetMapping
  public CursorPage<ShipmentSummaryDTO> list(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @RequestParam(required = false, name = "user") String userFilter,
      @RequestParam(required = false, name = "rider_id") UUID riderId,
      @RequestParam(required = false) ShipmentStatus status,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit) {
    boolean userIsMe = "me".equalsIgnoreCase(userFilter);
    return shipmentService.list(
        currentUser.getId(), isCustomer(currentUser), userIsMe, riderId, status, cursor, limit);
  }

  @GetMapping("/{id}")
  public ShipmentResponseDTO get(
      @AuthenticationPrincipal UserPrincipal currentUser, @PathVariable UUID id) {
    return shipmentService.getById(id, currentUser.getId(), isCustomer(currentUser));
  }

  @PostMapping("/{id}/assign")
  @PreAuthorize("hasRole('RIDER')")
  public ShipmentSummaryDTO assign(
      @AuthenticationPrincipal UserPrincipal currentUser, @PathVariable UUID id) {
    return shipmentService.assign(id, currentUser.getId());
  }

  @PatchMapping("/{id}")
  @PreAuthorize("hasAnyRole('EMPLOYEE','ADMIN','RIDER')")
  public ShipmentResponseDTO advance(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @PathVariable UUID id,
      @Valid @RequestBody AdvanceShipmentRequestDTO request) {
    return shipmentService.advance(id, request.status(), currentUser.getId(), isRider(currentUser));
  }

  private static boolean isCustomer(UserPrincipal user) {
    String role = user.getRole();
    return role != null && role.endsWith("CUSTOMER");
  }

  private static boolean isRider(UserPrincipal user) {
    String role = user.getRole();
    return role != null && role.endsWith("RIDER");
  }
}
