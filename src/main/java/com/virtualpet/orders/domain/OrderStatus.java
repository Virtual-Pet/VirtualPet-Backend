package com.virtualpet.orders.domain;

/** Spec-aligned order lifecycle: confirmed or cancelled (granular tracking lives on Shipment). */
public enum OrderStatus {
  CONFIRMED,
  CANCELLED
}
