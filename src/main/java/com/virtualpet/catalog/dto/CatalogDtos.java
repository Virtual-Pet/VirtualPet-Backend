package com.virtualpet.catalog.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTOs for /products endpoints. Records map 1:1 to schemas defined in
 * docs/api/virtualpet-openapi.yaml.
 */
public final class CatalogDtos {

  private CatalogDtos() {}

  public record ProductSummary(
      UUID id,
      String name,
      String category,
      String petType,
      BigDecimal basePrice,
      String thumbnail) {}

  public record Product(
      UUID id,
      String name,
      String description,
      String category,
      String petType,
      List<String> images,
      List<Sku> skus) {}

  public record Sku(
      UUID skuId, Map<String, String> attributes, BigDecimal price, boolean available) {}
}
