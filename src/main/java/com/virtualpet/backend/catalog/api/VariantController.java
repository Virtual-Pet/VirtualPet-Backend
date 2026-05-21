package com.virtualpet.backend.catalog.api;

import com.virtualpet.backend.catalog.dto.CatalogDtos.VariantBySkuResponse;
import com.virtualpet.backend.catalog.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
public class VariantController {

  private final CatalogService catalogService;

  @GetMapping("/by-sku/{sku}")
  public VariantBySkuResponse getBySku(@PathVariable String sku) {
    return catalogService.getVariantBySku(sku);
  }
}
