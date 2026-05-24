package com.virtualpet.shipments.controller;

import com.virtualpet.common.security.UserPrincipal;
import com.virtualpet.shipments.domain.ShipmentStatus;
import com.virtualpet.shipments.dto.LogisticsDTO.PendingOrderResponse;
import com.virtualpet.shipments.dto.LogisticsDTO.ShipmentStatusResponse;
import com.virtualpet.shipments.dto.LogisticsDTO.UpdateShipmentStatusRequest;
import com.virtualpet.shipments.service.LogisticsService;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/backoffice")
@RequiredArgsConstructor
public class LogisticsController {

  private final LogisticsService logisticsService;

  /** List all orders with a given shipment status (default: PENDING). */
  @GetMapping("/orders")
  public ResponseEntity<List<PendingOrderResponse>> listOrders(
      @RequestParam(required = false, defaultValue = "PENDING") String status) {
    ShipmentStatus shipmentStatus;
    try {
      shipmentStatus = ShipmentStatus.valueOf(status.toUpperCase());
    } catch (IllegalArgumentException e) {
      shipmentStatus = ShipmentStatus.PENDING;
    }
    return ResponseEntity.ok(logisticsService.listByStatus(shipmentStatus));
  }

  /** Advance the shipment status for an order. */
  @PatchMapping("/orders/{orderId}/status")
  public ResponseEntity<ShipmentStatusResponse> updateStatus(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @PathVariable UUID orderId,
      @RequestBody UpdateShipmentStatusRequest request) {
    return ResponseEntity.ok(
        logisticsService.updateStatus(orderId, request.status(), currentUser.getId()));
  }
}
