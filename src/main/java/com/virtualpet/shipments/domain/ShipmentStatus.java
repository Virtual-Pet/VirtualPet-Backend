package com.virtualpet.shipments.domain;

/** Spec-aligned shipment lifecycle. */
public enum ShipmentStatus {
  CONFIRMED,
  PREPARED,
  IN_TRANSIT,
  DELIVERED,
  CANCELLED
}
