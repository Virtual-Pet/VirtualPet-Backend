package com.virtualpet.catalog.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * DTOs for /products endpoints. Records map 1:1 to schemas defined in
 * docs/api/virtualpet-openapi.yaml.
 */
public final class CatalogDTO {

  private CatalogDTO() {}

  public record ProductSummaryDTO(
      UUID id,
      String name,
      String category,
      String petType,
      BigDecimal basePrice,
      String thumbnail) {}

  public record ProductDTO(
      UUID id,
      String name,
      String description,
      String category,
      String petType,
      List<String> images,
      List<SkuDTO> skus) {}

  public record SkuDTO(
      UUID skuId, Map<String, String> attributes, BigDecimal price, boolean available) {}
}
