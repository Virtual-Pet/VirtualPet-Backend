package com.virtualpet.backend.logistics.dto;

import java.math.BigDecimal;
import java.util.UUID;

public final class LogisticsDTO {

    private LogisticsDTO() {}

    /**
     * Summary of an order visible to warehouse operators. The order_id comes from the
     * logistics.shipments cross-reference to orders.orders.
     */
    public record PendingOrderResponse(
            UUID shipmentId,
            UUID orderId,
            String shipmentStatus,
            String contactName,
            String contactEmail,
            BigDecimal total,
            String createdAt) {}

    public record UpdateShipmentStatusRequest(String status) {}

    public record ShipmentStatusResponse(
            UUID shipmentId, UUID orderId, String status, String updatedAt) {}
}
