package com.virtualpet.catalog.service;

import com.virtualpet.catalog.domain.ProductEntity;
import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.catalog.dto.CatalogDTO.ProductDTO;
import com.virtualpet.catalog.dto.CatalogDTO.ProductSummaryDTO;
import com.virtualpet.catalog.dto.CatalogDTO.SkuDTO;
import com.virtualpet.catalog.repository.ProductRepository;
import com.virtualpet.catalog.repository.ProductVariantRepository;
import com.virtualpet.catalog.spec.ProductSpecifications;
import com.virtualpet.common.exception.ApiException;
import com.virtualpet.common.pagination.Cursor;
import com.virtualpet.common.pagination.CursorCodec;
import com.virtualpet.common.pagination.CursorPage;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {

  private static final TypeReference<Map<String, String>> ATTRS_TYPE = new TypeReference<>() {};

  private final ProductRepository productRepository;
  private final ProductVariantRepository variantRepository;
  private final CursorCodec cursorCodec;
  private final ObjectMapper objectMapper;

  @Transactional(readOnly = true)
  public CursorPage<ProductSummaryDTO> list(
      String q,
      String category,
      String petType,
      BigDecimal minPrice,
      BigDecimal maxPrice,
      String cursor,
      int limit) {
    int effectiveLimit = clampLimit(limit);
    Cursor decoded = cursorCodec.decode(cursor);

    Specification<ProductEntity> spec =
        Specification.allOf(
            Stream.of(
                    ProductSpecifications.active(),
                    ProductSpecifications.search(q),
                    ProductSpecifications.category(category),
                    ProductSpecifications.petType(petType),
                    ProductSpecifications.priceRange(minPrice, maxPrice),
                    ProductSpecifications.afterCursor(decoded))
                .filter(Objects::nonNull)
                .toList());

    Sort sort = Sort.by(Sort.Order.desc("createdAt"), Sort.Order.desc("id"));
    List<ProductEntity> rows =
        productRepository.findAll(spec, PageRequest.of(0, effectiveLimit + 1, sort)).getContent();

    boolean hasMore = rows.size() > effectiveLimit;
    if (hasMore) {
      rows = rows.subList(0, effectiveLimit);
    }

    Map<UUID, List<ProductVariantEntity>> variantsByProduct = loadVariantsByProductId(rows);
    List<ProductSummaryDTO> data =
        rows.stream()
            .map(p -> toSummary(p, variantsByProduct.getOrDefault(p.getId(), List.of())))
            .toList();

    String nextCursor =
        hasMore
            ? cursorCodec.encode(Cursor.of(rows.getLast().getCreatedAt(), rows.getLast().getId()))
            : null;
    log.debug(
        "Product list: q={}, category={}, petType={}, returned={}, hasMore={}",
        q,
        category,
        petType,
        data.size(),
        hasMore);
    return CursorPage.of(data, effectiveLimit, nextCursor);
  }

  @Transactional(readOnly = true)
  public ProductDTO getById(UUID id) {
    ProductEntity product =
        productRepository
            .findByIdWithVariantsAndCategory(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Product not found"));
    return toDetail(product);
  }

  private int clampLimit(int requested) {
    if (requested <= 0) {
      return 20;
    }
    return Math.min(requested, 100);
  }

  private Map<UUID, List<ProductVariantEntity>> loadVariantsByProductId(List<ProductEntity> rows) {
    if (rows.isEmpty()) {
      return Map.of();
    }
    List<UUID> ids = rows.stream().map(ProductEntity::getId).toList();
    return variantRepository.findByProductIdIn(ids).stream()
        .collect(Collectors.groupingBy(v -> v.getProduct().getId()));
  }

  private ProductSummaryDTO toSummary(ProductEntity product, List<ProductVariantEntity> variants) {
    BigDecimal basePrice =
        variants.stream()
            .map(ProductVariantEntity::getPrice)
            .min(Comparator.naturalOrder())
            .orElse(BigDecimal.ZERO);
    String thumbnail =
        variants.stream()
            .map(ProductVariantEntity::getImageUrl)
            .filter(url -> url != null && !url.isBlank())
            .findFirst()
            .orElse(null);
    return new ProductSummaryDTO(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getBrand(),
        product.getCategory() == null ? null : product.getCategory().getName(),
        product.getPetType(),
        basePrice,
        thumbnail,
        product.isActive(),
        variants.size());
  }

  private ProductDTO toDetail(ProductEntity product) {
    List<ProductVariantEntity> variants = product.getVariants();
    List<SkuDTO> skus = variants.stream().map(this::toSku).toList();
    List<String> images =
        variants.stream()
            .map(ProductVariantEntity::getImageUrl)
            .filter(url -> url != null && !url.isBlank())
            .distinct()
            .toList();
    return new ProductDTO(
        product.getId(),
        product.getName(),
        product.getDescription(),
        product.getBrand(),
        product.getCategory() == null ? null : product.getCategory().getName(),
        product.getPetType(),
        product.isActive(),
        product.getCreatedAt(),
        images,
        skus);
  }

  private SkuDTO toSku(ProductVariantEntity variant) {
    return new SkuDTO(
        variant.getId(),
        variant.getSku(),
        parseAttributes(variant.getAttributes()),
        variant.getPrice(),
        variant.getStock(),
        variant.getStockMin(),
        variant.getImageUrl(),
        variant.isActive(),
        variant.getCreatedAt(),
        variant.getStock() > 0);
  }

  private Map<String, String> parseAttributes(String json) {
    if (json == null || json.isBlank()) {
      return new LinkedHashMap<>();
    }
    try {
      return objectMapper.readValue(json, ATTRS_TYPE);
    } catch (Exception ex) {
      log.warn("Failed to parse attributes JSON: {}", ex.getMessage());
      return new LinkedHashMap<>();
    }
  }
}
