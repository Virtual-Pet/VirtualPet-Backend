package com.virtualpet.backend.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(schema = "catalog", name = "product_variants")
@Getter
@Setter
public class ProductVariantEntity {

  @Id private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "product_id", nullable = false)
  private ProductEntity product;

  @Column(nullable = false, unique = true)
  private String sku;

  @Column(columnDefinition = "jsonb", nullable = false)
  private String attributes;

  @Column(nullable = false)
  private BigDecimal price = BigDecimal.ZERO;

  @Column(nullable = false)
  private int stock;

  @Column(name = "image_url")
  private String imageUrl;
}
