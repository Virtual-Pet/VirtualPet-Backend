package com.virtualpet.catalog.controller;

import com.virtualpet.catalog.dto.CatalogDTO.ProductDTO;
import com.virtualpet.catalog.dto.CatalogDTO.ProductSummaryDTO;
import com.virtualpet.catalog.service.CatalogService;
import com.virtualpet.common.cache.ETagSupport;
import com.virtualpet.common.pagination.CursorPage;
import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class CatalogController {

  private static final CacheControl CACHE =
      CacheControl.maxAge(Duration.ofMinutes(5)).cachePublic();

  private final CatalogService catalogService;
  private final ETagSupport etag;

  @GetMapping
  public ResponseEntity<CursorPage<ProductSummaryDTO>> list(
      @RequestParam(required = false) String q,
      @RequestParam(required = false) String category,
      @RequestParam(required = false) String petType,
      @RequestParam(required = false) BigDecimal minPrice,
      @RequestParam(required = false) BigDecimal maxPrice,
      @RequestParam(required = false) String cursor,
      @RequestParam(defaultValue = "20") int limit,
      HttpServletRequest request) {
    CursorPage<ProductSummaryDTO> page =
        catalogService.list(q, category, petType, minPrice, maxPrice, cursor, limit);
    return etag.withETag(page, request, CACHE);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductDTO> get(@PathVariable UUID id, HttpServletRequest request) {
    return etag.withETag(catalogService.getById(id), request, CACHE);
  }
}
