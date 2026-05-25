package com.virtualpet.shipments.domain;

/**
 * Shipment lifecycle. The OpenAPI contract uses {CONFIRMED, PREPARED, IN_TRANSIT, DELIVERED,
 * CANCELLED}; legacy values remain until PR7 backfills + removes them.
 */
public enum ShipmentStatus {
  CONFIRMED,
  PREPARED,
  IN_TRANSIT,
  DELIVERED,
  CANCELLED,
  // legacy — being phased out
  PENDING,
  IN_PREPARATION,
  SHIPPED,
  FAILED,
  CANCELED
}
