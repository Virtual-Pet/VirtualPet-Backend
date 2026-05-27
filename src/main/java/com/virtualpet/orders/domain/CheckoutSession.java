package com.virtualpet.orders.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Ephemeral checkout session stored in Redis with TTL. Pre-payment state lives only here; once the
 * session reaches CONFIRMED the durable record is the OrderEntity in Postgres.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSession {

  private UUID id;
  private UUID userId;

  @Builder.Default private SessionStatus status = SessionStatus.PENDING;

  @Builder.Default private List<SessionLineItem> lineItems = new ArrayList<>();

  private SessionTotals totals;
  private String currency;
  private Address shippingAddress;
  private Instant expiresAt;
}
