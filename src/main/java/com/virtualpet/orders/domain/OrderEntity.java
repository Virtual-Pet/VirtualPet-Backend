package com.virtualpet.orders.domain;

import com.virtualpet.orders.domain.JsonAttributeConverters.AddressJsonConverter;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(schema = "orders", name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "session_id", unique = true)
  private UUID sessionId;

  @Column(name = "warehouse_id")
  private Integer warehouseId;

  @Column(name = "contact_name", length = 50)
  private String contactName;

  @Column(name = "contact_lastname", length = 50)
  private String contactLastname;

  @Column(name = "contact_email")
  private String contactEmail;

  @Column(name = "contact_phone", length = 30)
  private String contactPhone;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private OrderStatus status;

  @Column(nullable = false, precision = 10, scale = 2)
  private BigDecimal total;

  @Convert(converter = AddressJsonConverter.class)
  @Column(name = "shipping_address", nullable = false, columnDefinition = "text")
  private Address shippingAddress;

  @Column(name = "shipping_attempts", nullable = false)
  @Builder.Default
  private short shippingAttempts = 0;

  @Version private Long version;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private Instant createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private Instant updatedAt;

  @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  private List<OrderItemEntity> items = new ArrayList<>();

  public void addItem(OrderItemEntity item) {
    items.add(item);
    item.setOrder(this);
  }
}
