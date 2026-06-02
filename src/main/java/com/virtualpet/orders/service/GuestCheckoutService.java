package com.virtualpet.orders.service;

import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.catalog.repository.ProductVariantRepository;
import com.virtualpet.common.exception.ApiException;
import com.virtualpet.orders.domain.CheckoutSession;
import com.virtualpet.orders.domain.SessionLineItem;
import com.virtualpet.orders.domain.SessionTotals;
import com.virtualpet.orders.dto.CheckoutDTO.GuestCheckoutRequestDTO;
import com.virtualpet.orders.dto.CheckoutDTO.GuestLineItemDTO;
import com.virtualpet.orders.dto.CheckoutDTO.OrderConfirmationResponseDTO;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GuestCheckoutService {

  private static final BigDecimal SHIPPING = BigDecimal.ZERO;

  private final ProductVariantRepository variantRepository;
  private final ConfirmOrchestrator confirmOrchestrator;

  @Transactional
  public OrderConfirmationResponseDTO checkout(GuestCheckoutRequestDTO request, String cartSessionId) {
    Map<UUID, ProductVariantEntity> variants = fetchAndValidate(request.lineItems());

    List<SessionLineItem> lineItems =
        request.lineItems().stream()
            .map(
                dto -> {
                  BigDecimal unitPrice = variants.get(dto.skuId()).getPrice();
                  BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(dto.quantity()));
                  return new SessionLineItem(dto.skuId(), dto.quantity(), unitPrice, subtotal);
                })
            .toList();

    BigDecimal itemsTotal =
        lineItems.stream().map(SessionLineItem::subtotal).reduce(BigDecimal.ZERO, BigDecimal::add);

    CheckoutSession session =
        CheckoutSession.builder()
            .id(UUID.randomUUID())
            .userId(null)
            .lineItems(lineItems)
            .totals(new SessionTotals(itemsTotal, SHIPPING, itemsTotal.add(SHIPPING)))
            .currency("ARS")
            .shippingAddress(request.shippingAddress().toAddress())
            .build();

    return confirmOrchestrator.placeGuestOrder(session, request.guest(), cartSessionId);
  }

  private Map<UUID, ProductVariantEntity> fetchAndValidate(List<GuestLineItemDTO> items) {
    Map<UUID, ProductVariantEntity> byId = new HashMap<>();
    for (GuestLineItemDTO item : items) {
      ProductVariantEntity variant =
          variantRepository
              .findByIdWithProduct(item.skuId())
              .orElseThrow(
                  () ->
                      new ApiException(
                          HttpStatus.CONFLICT, "SKU no longer available: " + item.skuId()));
      if (variant.getStock() < item.quantity()) {
        throw new ApiException(
            HttpStatus.CONFLICT, "Insufficient stock for SKU: " + variant.getSku());
      }
      byId.put(item.skuId(), variant);
    }
    return byId;
  }
}
