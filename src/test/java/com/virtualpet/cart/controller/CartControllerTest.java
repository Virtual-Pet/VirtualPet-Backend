package com.virtualpet.cart.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.virtualpet.TestRedisConfiguration;
import com.virtualpet.catalog.domain.CategoryEntity;
import com.virtualpet.catalog.domain.ProductEntity;
import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.catalog.repository.ProductRepository;
import com.virtualpet.catalog.repository.ProductVariantRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.servlet.http.Cookie;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestRedisConfiguration.class)
@Transactional
class CartControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ProductRepository productRepository;
  @Autowired private ProductVariantRepository variantRepository;

  @PersistenceContext private EntityManager em;

  private String accessToken;
  private UUID skuId;
  private String email;
  private String password;

  @BeforeEach
  void setup() throws Exception {
    em.createNativeQuery("DELETE FROM shipments.shipment_status").executeUpdate();
    em.createNativeQuery("DELETE FROM shipments.shipments").executeUpdate();
    em.createQuery("DELETE FROM PaymentEntity").executeUpdate();
    em.createQuery("DELETE FROM OrderItemEntity").executeUpdate();
    em.createQuery("DELETE FROM OrderEntity").executeUpdate();
    em.createQuery("DELETE FROM ProductVariantEntity").executeUpdate();
    em.createQuery("DELETE FROM ProductEntity").executeUpdate();
    em.createQuery("DELETE FROM CategoryEntity").executeUpdate();
    em.flush();

    CategoryEntity category = new CategoryEntity();
    category.setName("Food");
    category.setSlug("food-" + UUID.randomUUID());
    em.persist(category);

    ProductEntity product = new ProductEntity();
    product.setName("Kibble");
    product.setDescription("Dog food");
    product.setBrand("BrandA");
    product.setPetType("DOG");
    product.setCategory(category);
    product.setActive(true);
    product.setCreatedAt(Instant.now());
    em.persist(product);

    ProductVariantEntity variant = new ProductVariantEntity();
    variant.setProduct(product);
    variant.setSku("KBL-" + UUID.randomUUID().toString().substring(0, 8));
    variant.setAttributes("{}");
    variant.setPrice(new BigDecimal("1500.00"));
    variant.setStock(50);
    em.persist(variant);
    em.flush();
    em.clear();

    skuId = variant.getId();

    // Register and login a customer
    email = "buyer-" + UUID.randomUUID() + "@example.com";
    password = "Strong-pass1";
    mockMvc
        .perform(
            post("/api/v1/auth/register/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    { "email": "%s", "password": "%s", "firstName": "Buyer", "lastName": "X" }
                    """
                        .formatted(email, password)))
        .andExpect(status().isCreated());

    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        { "email": "%s", "password": "%s" }
                        """
                            .formatted(email, password)))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode tokens = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    accessToken = tokens.get("accessToken").asText();
  }

  @Test
  void getOnEmptyCartReturnsEmptyShape() throws Exception {
    mockMvc
        .perform(get("/api/v1/cart").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(0))
        .andExpect(jsonPath("$.totals.items").value("0.00"))
        .andExpect(jsonPath("$.totals.shipping").value("0.00"))
        .andExpect(jsonPath("$.totals.grandTotal").value("0.00"))
        .andExpect(jsonPath("$.currency").value("ARS"));
  }

  @Test
  void putItemReturnsSkuAndQuantity() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/cart/items/" + skuId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":3}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.skuId").value(skuId.toString()))
        .andExpect(jsonPath("$.quantity").value(3));
  }

  @Test
  void putItemForUnknownSkuReturnsProblem404() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/cart/items/" + UUID.randomUUID())
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":1}"))
        .andExpect(status().isNotFound())
        .andExpect(header().string("Content-Type", "application/problem+json"));
  }

  @Test
  void putItemRejectsZeroQuantityWith422() throws Exception {
    mockMvc
        .perform(
            put("/api/v1/cart/items/" + skuId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":0}"))
        .andExpect(status().isUnprocessableEntity());
  }

  @Test
  void deleteItemIsIdempotent() throws Exception {
    mockMvc
        .perform(
            delete("/api/v1/cart/items/" + skuId).header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());

    // calling again still 204
    mockMvc
        .perform(
            delete("/api/v1/cart/items/" + skuId).header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isNoContent());
  }

  @Test
  void guestPutIssuesSessionCookieAndGetReturnsItem() throws Exception {
    MvcResult putResult =
        mockMvc
            .perform(
                put("/api/v1/cart/items/" + skuId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"quantity\":2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.quantity").value(2))
            .andReturn();

    Cookie sessionCookie = putResult.getResponse().getCookie("CART_SESSION");
    org.assertj.core.api.Assertions.assertThat(sessionCookie).isNotNull();
    org.assertj.core.api.Assertions.assertThat(sessionCookie.getValue()).isNotBlank();

    // GET with the cookie returns the guest cart
    mockMvc
        .perform(get("/api/v1/cart").cookie(sessionCookie))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].skuId").value(skuId.toString()))
        .andExpect(jsonPath("$.items[0].quantity").value(2));
  }

  @Test
  void guestWithoutCookieGetsEmptyCart() throws Exception {
    mockMvc
        .perform(get("/api/v1/cart"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(0))
        .andExpect(jsonPath("$.totals.grandTotal").value("0.00"));
  }

  @Test
  void loginMergesGuestCartAndClearsCookie() throws Exception {
    // Guest adds an item and gets a session cookie
    MvcResult putResult =
        mockMvc
            .perform(
                put("/api/v1/cart/items/" + skuId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"quantity\":4}"))
            .andExpect(status().isOk())
            .andReturn();
    Cookie sessionCookie = putResult.getResponse().getCookie("CART_SESSION");

    // Login carrying the cookie -> merge + clear cookie
    MvcResult loginResult =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .cookie(sessionCookie)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        { "email": "%s", "password": "%s" }
                        """
                            .formatted(email, password)))
            .andExpect(status().isOk())
            .andReturn();

    Cookie cleared = loginResult.getResponse().getCookie("CART_SESSION");
    org.assertj.core.api.Assertions.assertThat(cleared).isNotNull();
    org.assertj.core.api.Assertions.assertThat(cleared.getMaxAge()).isZero();
    String mergedToken =
        objectMapper
            .readTree(loginResult.getResponse().getContentAsString())
            .get("accessToken")
            .asText();

    // Authenticated cart now holds the merged item
    mockMvc
        .perform(get("/api/v1/cart").header("Authorization", "Bearer " + mergedToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items.length()").value(1))
        .andExpect(jsonPath("$.items[0].skuId").value(skuId.toString()))
        .andExpect(jsonPath("$.items[0].quantity").value(4));
  }
}
