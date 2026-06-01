package com.virtualpet.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.virtualpet.cart.dto.CartDTO;
import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.catalog.repository.ProductVariantRepository;
import com.virtualpet.common.config.VirtualPetProperties;
import com.virtualpet.common.exception.ApiException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CartServiceTest {

  @Mock private StringRedisTemplate redis;
  @Mock private ValueOperations<String, String> ops;
  @Mock private ProductVariantRepository variantRepository;

  private CartService service;

  private final Map<String, String> redisStore = new HashMap<>();
  private final Map<UUID, ProductVariantEntity> variants = new HashMap<>();
  private final ObjectMapper json = JsonMapper.builder().build();

  private UUID userId;

  @BeforeEach
  void setup() {
    userId = UUID.randomUUID();

    when(redis.opsForValue()).thenReturn(ops);
    when(ops.get(any())).thenAnswer(inv -> redisStore.get((String) inv.getArgument(0)));
    when(redis.delete(any(String.class)))
        .thenAnswer(
            inv -> {
              boolean removed = redisStore.remove(inv.getArgument(0)) != null;
              return removed;
            });
    org.mockito.Mockito.doAnswer(
            inv -> {
              redisStore.put(inv.getArgument(0), inv.getArgument(1));
              return null;
            })
        .when(ops)
        .set(any(String.class), any(String.class), anyLong(), eq(TimeUnit.HOURS));

    when(variantRepository.findByIdWithProduct(any(UUID.class)))
        .thenAnswer(inv -> Optional.ofNullable(variants.get(inv.<UUID>getArgument(0))));
    when(variantRepository.findAllByIdInWithProduct(any()))
        .thenAnswer(
            inv -> {
              Iterable<UUID> ids = inv.getArgument(0);
              List<ProductVariantEntity> result = new ArrayList<>();
              for (UUID id : ids) {
                ProductVariantEntity v = variants.get(id);
                if (v != null) {
                  result.add(v);
                }
              }
              return result;
            });

    service = new CartService(redis, json, variantRepository, new VirtualPetProperties());
  }

  @Test
  void getCartLazilyReturnsEmptyCartWhenNoneStored() {
    CartDTO.CartViewDTO cart = service.getCart(userId);

    assertThat(cart.items()).isEmpty();
    assertThat(cart.totals().items()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(cart.totals().grandTotal()).isEqualByComparingTo(BigDecimal.ZERO);
    assertThat(cart.currency()).isEqualTo("ARS");
  }

  @Test
  void putItemAddsLineAndComputesTotals() {
    UUID skuA = registerVariant("100.00");

    CartDTO.CartItemQuantityDTO result = service.putItem(userId, skuA, 3);
    assertThat(result.skuId()).isEqualTo(skuA);
    assertThat(result.quantity()).isEqualTo(3);

    CartDTO.CartViewDTO cart = service.getCart(userId);
    assertThat(cart.items()).hasSize(1);
    CartDTO.CartItemDTO line = cart.items().getFirst();
    assertThat(line.skuId()).isEqualTo(skuA);
    assertThat(line.quantity()).isEqualTo(3);
    assertThat(line.unitPrice()).isEqualByComparingTo("100.00");
    assertThat(line.subtotal()).isEqualByComparingTo("300.00");
    assertThat(cart.totals().items()).isEqualByComparingTo("300.00");
    assertThat(cart.totals().grandTotal()).isEqualByComparingTo("300.00");
  }

  @Test
  void putItemIsUpsertNotIncrement() {
    UUID skuA = registerVariant("50.00");

    service.putItem(userId, skuA, 2);
    service.putItem(userId, skuA, 5);

    CartDTO.CartViewDTO cart = service.getCart(userId);
    assertThat(cart.items()).hasSize(1);
    assertThat(cart.items().getFirst().quantity()).isEqualTo(5);
  }

  @Test
  void putItemSupportsMultipleSkus() {
    UUID skuA = registerVariant("100.00");
    UUID skuB = registerVariant("200.00");

    service.putItem(userId, skuA, 2);
    service.putItem(userId, skuB, 1);

    CartDTO.CartViewDTO cart = service.getCart(userId);
    assertThat(cart.items()).hasSize(2);
    assertThat(cart.totals().items()).isEqualByComparingTo("400.00");
  }

  @Test
  void putItemRejectsUnknownSkuWith404() {
    UUID phantom = UUID.randomUUID();

    assertThatThrownBy(() -> service.putItem(userId, phantom, 1))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getStatus())
        .isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void putItemRejectsZeroOrNegativeQuantity() {
    UUID skuA = UUID.randomUUID();
    assertThatThrownBy(() -> service.putItem(userId, skuA, 0))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getStatus())
        .isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
  }

  @Test
  void removeItemDeletesLineAndIsIdempotent() {
    UUID skuA = registerVariant("10.00");
    service.putItem(userId, skuA, 1);

    service.removeItem(userId, skuA);
    assertThat(service.getCart(userId).items()).isEmpty();

    // calling again on an empty cart is a no-op
    service.removeItem(userId, skuA);
    assertThat(service.getCart(userId).items()).isEmpty();
  }

  @Test
  void clearCartRemovesAllItems() {
    UUID skuA = registerVariant("10.00");
    service.putItem(userId, skuA, 2);

    service.clearCart(userId);
    assertThat(service.getCart(userId).items()).isEmpty();
  }

  private UUID registerVariant(String price) {
    UUID id = UUID.randomUUID();
    ProductVariantEntity v = new ProductVariantEntity();
    v.setId(id);
    v.setPrice(new BigDecimal(price));
    variants.put(id, v);
    return id;
  }
}
