package com.virtualpet.shipments.domain;

/** Shipment lifecycle. */
public enum ShipmentStatus {
  CONFIRMED,
  PREPARED,
  ASSIGNED,
  DELIVERED,
  RETURNED,
  CANCELLED
}
