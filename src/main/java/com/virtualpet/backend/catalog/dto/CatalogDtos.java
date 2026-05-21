package com.virtualpet.backend.catalog.dto;

import java.math.BigDecimal;
import java.util.List;

public final class CatalogDtos {

  private CatalogDtos() {}

  public record ProductSummaryResponse(
      String id,
      String slug,
      String name,
      String description,
      BigDecimal basePrice,
      String category,
      String categorySlug,
      String petType,
      String brand,
      String imageUrl,
      BigDecimal minPrice) {}

  public record VariantResponse(
      String id, String sku, String attributes, BigDecimal price, int stock, String imageUrl) {}

  public record ProductDetailResponse(
      String id,
      String slug,
      String name,
      String description,
      BigDecimal basePrice,
      String category,
      String categorySlug,
      String petType,
      String brand,
      List<VariantResponse> variants) {}

  public record ProductPageResponse(
      List<ProductSummaryResponse> items, long total, int page, int size) {}

  public record CategoryResponse(String id, String name, String slug, long productCount) {}

  public record FacetOption(String value, String label, long count) {}

  public record CatalogFacetsResponse(
      List<FacetOption> petTypes, List<CategoryResponse> categories, List<FacetOption> brands) {}

  public record VariantBySkuResponse(
      VariantResponse variant,
      String productId,
      String productSlug,
      String productName,
      String categorySlug,
      String petType,
      String brand) {}
}
