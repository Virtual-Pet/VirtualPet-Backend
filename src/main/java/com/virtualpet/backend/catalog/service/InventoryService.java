package com.virtualpet.backend.catalog.service;

import com.virtualpet.backend.catalog.domain.ProductVariantEntity;
import com.virtualpet.backend.catalog.repository.ProductVariantRepository;
import com.virtualpet.backend.shared.exception.ApiException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

  private final ProductVariantRepository variantRepository;

  @Transactional
  public void decrementStock(Map<UUID, Integer> lines) {
    log.debug("Decrementing stock for {} variant(s)", lines.size());
    for (Map.Entry<UUID, Integer> entry : lines.entrySet()) {
      int updated = variantRepository.decrementStock(entry.getKey(), entry.getValue());
      if (updated == 0) {
        ProductVariantEntity variant =
            variantRepository
                .findById(entry.getKey())
                .orElseThrow(
                    () -> new ApiException(HttpStatus.NOT_FOUND, "Variante no encontrada"));
        log.warn(
            "Insufficient stock for SKU {} (requested: {})", variant.getSku(), entry.getValue());
        throw new ApiException(HttpStatus.CONFLICT, "Stock insuficiente para " + variant.getSku());
      }
      log.info("Stock decremented: variantId={}, qty={}", entry.getKey(), entry.getValue());
    }
  }
}
