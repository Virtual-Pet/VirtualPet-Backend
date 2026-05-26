package com.virtualpet.cart.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.virtualpet.cart.domain.Cart;
import com.virtualpet.cart.domain.CartItem;
import com.virtualpet.cart.dto.CartDTO.AddItemRequest;
import com.virtualpet.cart.dto.CartDTO.CartItemResponse;
import com.virtualpet.cart.dto.CartDTO.CartResponse;
import com.virtualpet.common.exception.ApiException;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final String KEY_PREFIX = "cart:";
    private static final long TTL_HOURS = 24;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CartResponse getCart(String sessionId) {
        Cart cart = loadCart(sessionId);
        return toResponse(cart);
    }

    public CartResponse addItem(String sessionId, AddItemRequest req) {
        Cart cart = loadCart(sessionId);

        CartItem existing = cart.findItem(req.variantId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + req.quantity());
        } else {
            cart.getItems()
                .add(
                    CartItem.builder()
                        .variantId(req.variantId())
                        .productName(req.productName())
                        .sku(req.sku())
                        .attributes(req.attributes())
                        .quantity(req.quantity())
                        .unitPrice(req.unitPrice())
                        .imageUrl(req.imageUrl())
                        .build());
        }

        saveCart(sessionId, cart);
        return toResponse(cart);
    }

    public CartResponse updateItem(String sessionId, String variantId, int newQuantity) {
        Cart cart = loadCart(sessionId);
        CartItem item = cart.findItem(variantId);

        if (item == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Item no encontrado en el carrito");
        }

        if (newQuantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(newQuantity);
        }

        saveCart(sessionId, cart);
        return toResponse(cart);
    }

    public CartResponse removeItem(String sessionId, String variantId) {
        Cart cart = loadCart(sessionId);
        boolean removed = cart.getItems().removeIf(i -> i.getVariantId().equals(variantId));

        if (!removed) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Item no encontrado en el carrito");
        }

        saveCart(sessionId, cart);
        return toResponse(cart);
    }

    public void clearCart(String sessionId) {
        redisTemplate.delete(key(sessionId));
    }

    public Cart getRawCart(String sessionId) {
        return loadCart(sessionId);
    }

    private String key(String sessionId) {
        return KEY_PREFIX + sessionId;
    }

    private Cart loadCart(String sessionId) {
        String json = redisTemplate.opsForValue().get(key(sessionId));
        if (json == null) {
            return Cart.builder().build();
        }
        try {
            return objectMapper.readValue(json, Cart.class);
        } catch (JsonProcessingException e) {
            return Cart.builder().build();
        }
    }

    private void saveCart(String sessionId, Cart cart) throws ApiException {
        try {
            String json = objectMapper.writeValueAsString(cart);
            redisTemplate.opsForValue().set(key(sessionId), json, TTL_HOURS, TimeUnit.HOURS);
        } catch (JsonProcessingException e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Error al guardar el carrito");
        }
    }

    private CartResponse toResponse(Cart cart) {
        var items =
            cart.getItems().stream()
                .map(
                    i ->
                        new CartItemResponse(
                            i.getVariantId(),
                            i.getProductName(),
                            i.getSku(),
                            i.getAttributes(),
                            i.getQuantity(),
                            i.getUnitPrice(),
                            i.lineTotal(),
                            i.getImageUrl()))
                .toList();
        return new CartResponse(items, cart.subtotal(), cart.itemCount());
    }
}
