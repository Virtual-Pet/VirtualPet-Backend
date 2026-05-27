package com.virtualpet.orders.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(schema = "orders", name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id")
  private UUID userId;

  @Column(name = "session_id")
  private UUID sessionId;

  @Column(name = "order_id")
  private UUID orderId;

  @Column(nullable = false, length = 50)
  private String provider;

  @Column(name = "provider_payment_id", length = 255)
  private String providerPaymentId;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  @Builder.Default
  private String currency = "ARS";

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private PaymentStatus status = PaymentStatus.PENDING;

  @Column(name = "idempotency_key", length = 255)
  private String idempotencyKey;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;
}
