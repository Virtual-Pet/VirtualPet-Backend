package com.virtualpet.orders.domain;

/**
 * Order lifecycle. The OpenAPI contract collapses to {CONFIRMED, CANCELLED}; the legacy values
 * remain here until PR6 backfills + removes them.
 */
public enum OrderStatus {
  CONFIRMED,
  CANCELLED,
  // legacy — being phased out
  PENDING_PAYMENT,
  PAID,
  IN_PREPARATION,
  PREPARED,
  SHIPPED,
  DELIVERED,
  SHIPPING_FAILED,
  CANCELED
}
