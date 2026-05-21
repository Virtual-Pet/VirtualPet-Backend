package com.virtualpet.backend.catalog.api;

import com.virtualpet.backend.catalog.dto.CatalogDtos.CatalogFacetsResponse;
import com.virtualpet.backend.catalog.dto.CatalogDtos.CategoryResponse;
import com.virtualpet.backend.catalog.dto.CatalogDtos.ProductDetailResponse;
import com.virtualpet.backend.catalog.dto.CatalogDtos.ProductPageResponse;
import com.virtualpet.backend.catalog.service.CatalogService;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CatalogController {

  private final CatalogService catalogService;

  @GetMapping("/products")
  public ProductPageResponse list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String petType,
      @RequestParam(required = false) String brand,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) String sort,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    return catalogService.list(q, category, petType, brand, minPrice, maxPrice, sort, page, size);
  }

  @GetMapping("/products/facets")
  public CatalogFacetsResponse facets() {
    return catalogService.getFacets();
  }

  @GetMapping("/products/by-slug/{slug}")
  public ProductDetailResponse getBySlug(@PathVariable String slug) {
    return catalogService.getBySlug(slug);
  }

  @GetMapping("/products/{id}")
  public ProductDetailResponse get(@PathVariable UUID id) {
    return catalogService.getById(id);
  }

  @GetMapping("/categories")
  public List<CategoryResponse> categories() {
    return catalogService.listCategories();
  }
}
