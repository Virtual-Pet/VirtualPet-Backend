package com.virtualpet.orders.controller;

import static org.assertj.core.api.Assertions.assertThat;
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
import com.virtualpet.catalog.repository.ProductVariantRepository;
import com.virtualpet.orders.domain.OrderEntity;
import com.virtualpet.orders.domain.OrderStatus;
import com.virtualpet.orders.repository.OrderRepository;
import com.virtualpet.orders.repository.PaymentRepository;
import com.virtualpet.shipments.domain.ShipmentEntity;
import com.virtualpet.shipments.domain.ShipmentStatus;
import com.virtualpet.shipments.repository.ShipmentRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestRedisConfiguration.class)
class OrderControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PlatformTransactionManager txManager;
  @Autowired private OrderRepository orderRepository;
  @Autowired private ShipmentRepository shipmentRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private ProductVariantRepository variantRepository;

  @PersistenceContext private EntityManager em;

  private String accessToken;
  private UUID skuId;
  private UUID orderId;
  private UUID userId;

  private static final String ADDRESS_BODY =
      """
      {
        "addressLine": "Av. Test 100",
        "city": "MDP",
        "postalCode": "7600"
      }
      """;

  @BeforeEach
  void setup() throws Exception {
    new TransactionTemplate(txManager)
        .executeWithoutResult(
            tx -> {
              em.createQuery("DELETE FROM PaymentEntity").executeUpdate();
              em.createNativeQuery("DELETE FROM shipments.shipments").executeUpdate();
              em.createQuery("DELETE FROM OrderItemEntity").executeUpdate();
              em.createQuery("DELETE FROM OrderEntity").executeUpdate();
              em.createQuery("DELETE FROM ProductVariantEntity").executeUpdate();
              em.createQuery("DELETE FROM ProductEntity").executeUpdate();
              em.createQuery("DELETE FROM CategoryEntity").executeUpdate();
              em.flush();
            });

    skuId =
        new TransactionTemplate(txManager)
            .execute(
                tx -> {
                  CategoryEntity category = new CategoryEntity();
                  category.setName("Cat");
                  category.setSlug("cat-" + UUID.randomUUID());
                  em.persist(category);

                  ProductEntity product = new ProductEntity();
                  product.setName("Item");
                  product.setCategory(category);
                  product.setActive(true);
                  product.setCreatedAt(Instant.now());
                  em.persist(product);

                  ProductVariantEntity variant = new ProductVariantEntity();
                  variant.setProduct(product);
                  variant.setSku("SKU-" + UUID.randomUUID().toString().substring(0, 8));
                  variant.setAttributes("{}");
                  variant.setPrice(new BigDecimal("100.00"));
                  variant.setStock(10);
                  em.persist(variant);
                  em.flush();
                  return variant.getId();
                });

    // Register a customer and run the full checkout to leave a confirmed order in DB
    String email = "buyer-" + UUID.randomUUID() + "@example.com";
    String password = "Strong-pass1";
    mockMvc
        .perform(
            post("/api/v1/auth/register/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    { "email": "%s", "password": "%s", "firstName": "B", "lastName": "X" }
                    """
                        .formatted(email, password)))
        .andExpect(status().isCreated());

    MvcResult login =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        { "email": "%s", "password": "%s" }
                        """
                            .formatted(email, password)))
            .andReturn();
    JsonNode loginJson = objectMapper.readTree(login.getResponse().getContentAsString());
    accessToken = loginJson.get("accessToken").asString();
    userId = UUID.fromString(loginJson.get("user").get("id").asString());

    mockMvc
        .perform(
            put("/api/v1/cart/items/" + skuId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":2}"))
        .andExpect(status().isOk());

    MvcResult start =
        mockMvc
            .perform(post("/api/v1/cart/checkout").header("Authorization", "Bearer " + accessToken))
            .andReturn();
    String sessionId =
        objectMapper
            .readTree(start.getResponse().getContentAsString())
            .get("checkoutSessionId")
            .asString();

    mockMvc
        .perform(
            put("/api/v1/checkout/sessions/" + sessionId + "/shipping-address")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ADDRESS_BODY))
        .andExpect(status().isOk());

    MvcResult intent =
        mockMvc
            .perform(
                post("/api/v1/checkout/sessions/" + sessionId + "/payment-intents")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Idempotency-Key", UUID.randomUUID().toString()))
            .andReturn();
    String providerPaymentId =
        objectMapper
            .readTree(intent.getResponse().getContentAsString())
            .get("providerPaymentId")
            .asString();

    mockMvc
        .perform(post("/api/v1/fake-provider/payments/" + providerPaymentId + "/approve"))
        .andExpect(status().isOk());

    MvcResult confirm =
        mockMvc
            .perform(
                post("/api/v1/checkout/sessions/" + sessionId + "/confirm")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Idempotency-Key", UUID.randomUUID().toString()))
            .andExpect(status().isCreated())
            .andReturn();
    orderId =
        UUID.fromString(
            objectMapper
                .readTree(confirm.getResponse().getContentAsString())
                .get("orderId")
                .asString());
  }

  @Test
  void listReturnsCustomerOrdersWithCursorEnvelope() throws Exception {
    mockMvc
        .perform(get("/api/v1/orders").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].orderId").value(orderId.toString()))
        .andExpect(jsonPath("$.data[0].status").value("CONFIRMED"))
        .andExpect(jsonPath("$.data[0].total").value("200.00"))
        .andExpect(jsonPath("$.data[0].currency").value("ARS"))
        .andExpect(jsonPath("$.data[0].shipmentId").exists())
        .andExpect(jsonPath("$.hasMore").value(false));
  }

  @Test
  void getReturnsOrderWithEmbeddedShipment() throws Exception {
    mockMvc
        .perform(get("/api/v1/orders/" + orderId).header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(orderId.toString()))
        .andExpect(jsonPath("$.customerId").value(userId.toString()))
        .andExpect(jsonPath("$.status").value("CONFIRMED"))
        .andExpect(jsonPath("$.lineItems.length()").value(1))
        .andExpect(jsonPath("$.totals.grandTotal").value("200.00"))
        .andExpect(jsonPath("$.currency").value("ARS"))
        .andExpect(jsonPath("$.shippingAddress.addressLine").value("Av. Test 100"))
        .andExpect(jsonPath("$.shipment.status").value("CONFIRMED"));
  }

  @Test
  void cancelHappyPathCancelsOrderShipmentAndRefundsPayment() throws Exception {
    int stockBefore = variantRepository.findById(skuId).orElseThrow().getStock();

    mockMvc
        .perform(
            post("/api/v1/orders/" + orderId + "/cancel")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"reason\":\"changed mind\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").value(orderId.toString()))
        .andExpect(jsonPath("$.status").value("CANCELLED"))
        .andExpect(jsonPath("$.shipment.status").value("CANCELLED"))
        .andExpect(jsonPath("$.refund.status").value("REFUNDED"));

    OrderEntity order = orderRepository.findById(orderId).orElseThrow();
    assertThat(order.getStatus()).isEqualTo(OrderStatus.CANCELLED);

    ShipmentEntity shipment = shipmentRepository.findByOrderId(orderId).orElseThrow();
    assertThat(shipment.getStatus()).isEqualTo(ShipmentStatus.CANCELLED);

    int stockAfter = variantRepository.findById(skuId).orElseThrow().getStock();
    assertThat(stockAfter).isEqualTo(stockBefore + 2);

    assertThat(paymentRepository.findByOrderId(orderId).orElseThrow().getStatus())
        .isEqualTo(com.virtualpet.orders.domain.PaymentStatus.REFUNDED);
  }

  @Test
  void cancelIsIdempotent() throws Exception {
    mockMvc
        .perform(
            post("/api/v1/orders/" + orderId + "/cancel")
                .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());

    // Second cancel returns same envelope, no 409
    mockMvc
        .perform(
            post("/api/v1/orders/" + orderId + "/cancel")
                .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CANCELLED"));
  }

  @Test
  void cancelConflictsWhenShipmentAdvancedBeyondPrepared() throws Exception {
    // Advance the shipment past PREPARED directly in DB
    new TransactionTemplate(txManager)
        .executeWithoutResult(
            tx -> {
              ShipmentEntity s = shipmentRepository.findByOrderId(orderId).orElseThrow();
              s.setStatus(ShipmentStatus.ASSIGNED);
              shipmentRepository.save(s);
            });

    mockMvc
        .perform(
            post("/api/v1/orders/" + orderId + "/cancel")
                .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isConflict())
        .andExpect(header().string("Content-Type", "application/problem+json"));
  }

  @Test
  void anotherCustomerCannotSeeOrCancelTheOrder() throws Exception {
    String otherEmail = "other-" + UUID.randomUUID() + "@example.com";
    String otherPwd = "Strong-pass1";
    mockMvc
        .perform(
            post("/api/v1/auth/register/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    { "email": "%s", "password": "%s", "firstName": "O", "lastName": "X" }
                    """
                        .formatted(otherEmail, otherPwd)))
        .andExpect(status().isCreated());
    MvcResult login =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        { "email": "%s", "password": "%s" }
                        """
                            .formatted(otherEmail, otherPwd)))
            .andReturn();
    String otherToken =
        objectMapper
            .readTree(login.getResponse().getContentAsString())
            .get("accessToken")
            .asString();

    mockMvc
        .perform(get("/api/v1/orders/" + orderId).header("Authorization", "Bearer " + otherToken))
        .andExpect(status().isNotFound());

    mockMvc
        .perform(
            post("/api/v1/orders/" + orderId + "/cancel")
                .header("Authorization", "Bearer " + otherToken))
        .andExpect(status().isNotFound());
  }
}
