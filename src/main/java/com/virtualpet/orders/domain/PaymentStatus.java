package com.virtualpet.orders.domain;

/** Spec-aligned payment lifecycle (RFC 7807 / OpenAPI). */
public enum PaymentStatus {
  PENDING,
  PROCESSING,
  PAID,
  FAILED,
  REFUNDED
}
