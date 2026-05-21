package com.virtualpet.backend.orders.service;

import com.virtualpet.backend.cart.domain.Cart;
import com.virtualpet.backend.cart.domain.CartItem;
import com.virtualpet.backend.cart.service.CartService;
import com.virtualpet.backend.catalog.repository.ProductVariantRepository;
import com.virtualpet.backend.orders.domain.OrderEntity;
import com.virtualpet.backend.orders.domain.OrderItemEntity;
import com.virtualpet.backend.orders.domain.OrderStatus;
import com.virtualpet.backend.orders.dto.OrderDTO.CheckoutRequest;
import com.virtualpet.backend.orders.dto.OrderDTO.CheckoutResponse;
import com.virtualpet.backend.logistics.service.LogisticsService;
import com.virtualpet.backend.orders.repository.OrderRepository;
import com.virtualpet.backend.shared.exception.ApiException;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final CartService cartService;
    private final OrderRepository orderRepository;
    private final ProductVariantRepository variantRepository;
    private final LogisticsService logisticsService;

    @Transactional
    public CheckoutResponse checkout(UUID userId, String cartSessionId, CheckoutRequest req) {
        // 1. Load cart
        Cart cart = cartService.getRawCart(cartSessionId);
        if (cart.getItems().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "El carrito está vacío");
        }

        // 2. Build order
        OrderEntity order =
                OrderEntity.builder()
                        .userId(userId)
                        .contactName(req.contactName())
                        .contactLastname(req.contactLastname())
                        .contactEmail(req.contactEmail())
                        .contactPhone(req.contactPhone())
                        .shippingAddress(req.shippingAddress())
                        .status(OrderStatus.PAID)
                        .shippingAttempts((short) 0)
                        .build();

        // 3. Process items — validate stock and decrement atomically
        BigDecimal grandTotal = BigDecimal.ZERO;
        for (CartItem cartItem : cart.getItems()) {
            UUID variantId = UUID.fromString(cartItem.getVariantId());
            decrementStock(variantId, cartItem.getQuantity());

            BigDecimal subtotal =
                    cartItem.getUnitPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            grandTotal = grandTotal.add(subtotal);

            order.addItem(
                    OrderItemEntity.builder()
                            .productVariantId(variantId)
                            .skuSnapshot(cartItem.getSku())
                            .nameSnapshot(cartItem.getProductName())
                            .unitPrice(cartItem.getUnitPrice())
                            .quantity(cartItem.getQuantity())
                            .subtotal(subtotal)
                            .build());
        }
        order.setTotal(grandTotal);

        // 4. Save order
        OrderEntity saved = orderRepository.save(order);
        log.info(
                "Order created: id={}, userId={}, total={}", saved.getId(), userId, grandTotal);

        // 5. Create shipment record for logistics
        logisticsService.createShipmentForOrder(saved.getId());

        // 6. Clear cart
        cartService.clearCart(cartSessionId);

        return new CheckoutResponse(
                saved.getId().toString(),
                saved.getStatus().name(),
                saved.getTotal(),
                "/checkout/mock?orderId=" + saved.getId());
    }

    /**
     * Atomically decrements stock using a JPQL UPDATE with a stock guard ({@code WHERE stock >=
     * qty}). Returns the number of rows updated; throws {@link ApiException} with 409 CONFLICT if
     * stock is insufficient (0 rows updated).
     */
    private void decrementStock(UUID variantId, int quantity) {
        int updated = variantRepository.decrementStock(variantId, quantity);
        if (updated == 0) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Stock insuficiente para el producto con variantId=" + variantId);
        }
        log.debug("Stock decremented: variantId={}, qty={}", variantId, quantity);
    }
}
