package com.virtualpet.orders.domain;

import com.virtualpet.orders.domain.JsonAttributeConverters.AddressJsonConverter;
import com.virtualpet.orders.domain.JsonAttributeConverters.SessionLineItemsJsonConverter;
import com.virtualpet.orders.domain.JsonAttributeConverters.SessionTotalsJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(schema = "orders", name = "checkout_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSessionEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private SessionStatus status = SessionStatus.PENDING;

  @Convert(converter = SessionLineItemsJsonConverter.class)
  @Column(name = "line_items", nullable = false, columnDefinition = "text")
  @Builder.Default
  private List<SessionLineItem> lineItems = new ArrayList<>();

  @Convert(converter = SessionTotalsJsonConverter.class)
  @Column(nullable = false, columnDefinition = "text")
  private SessionTotals totals;

  @Column(nullable = false, length = 3)
  private String currency;

  @Convert(converter = AddressJsonConverter.class)
  @Column(name = "shipping_address", columnDefinition = "text")
  private Address shippingAddress;

  @Column(name = "expires_at", nullable = false)
  private Instant expiresAt;

  @Version private Long version;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;
}
