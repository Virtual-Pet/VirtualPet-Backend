package com.virtualpet.orders.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.virtualpet.TestRedisConfiguration;
import com.virtualpet.catalog.domain.CategoryEntity;
import com.virtualpet.catalog.domain.ProductEntity;
import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.catalog.repository.ProductRepository;
import com.virtualpet.catalog.repository.ProductVariantRepository;
import com.virtualpet.orders.repository.CheckoutSessionRepository;
import com.virtualpet.orders.repository.OrderRepository;
import com.virtualpet.orders.repository.PaymentRepository;
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
class CheckoutFlowTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ProductRepository productRepository;
  @Autowired private ProductVariantRepository variantRepository;
  @Autowired private CheckoutSessionRepository sessionRepository;
  @Autowired private PaymentRepository paymentRepository;
  @Autowired private OrderRepository orderRepository;
  @Autowired private ShipmentRepository shipmentRepository;

  @PersistenceContext private EntityManager em;

  private String accessToken;
  private UUID skuId;
  private int initialStock = 10;

  private static final String ADDRESS_BODY =
      """
      {
        "addressLine": "Av. Independencia 1234",
        "city": "Mar del Plata",
        "state": "Buenos Aires",
        "country": "AR",
        "postalCode": "7600"
      }
      """;

  @Autowired private PlatformTransactionManager txManager;

  @BeforeEach
  void setup() throws Exception {
    new TransactionTemplate(txManager)
        .executeWithoutResult(
            tx -> {
              em.createQuery("DELETE FROM PaymentEntity").executeUpdate();
              em.createNativeQuery("DELETE FROM logistics.shipments").executeUpdate();
              em.createQuery("DELETE FROM OrderItemEntity").executeUpdate();
              em.createQuery("DELETE FROM OrderEntity").executeUpdate();
              em.createQuery("DELETE FROM CheckoutSessionEntity").executeUpdate();
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
                  category.setName("Food");
                  category.setSlug("food-" + UUID.randomUUID());
                  em.persist(category);

                  ProductEntity product = new ProductEntity();
                  product.setName("Premium Kibble");
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
                  variant.setStock(initialStock);
                  em.persist(variant);
                  em.flush();
                  return variant.getId();
                });

    accessToken = registerAndLogin();

    // Seed cart with quantity 2
    mockMvc
        .perform(
            put("/api/v1/cart/items/" + skuId)
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":2}"))
        .andExpect(status().isOk());
  }

  @Test
  void fullCheckoutHappyPathWithApproveWebhookAndConfirm() throws Exception {
    // 1. Start checkout → 201 with PENDING session
    MvcResult startResult =
        mockMvc
            .perform(post("/api/v1/cart/checkout").header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andExpect(jsonPath("$.lineItems.length()").value(1))
            .andExpect(jsonPath("$.totals.grandTotal").value("3000.00"))
            .andExpect(jsonPath("$.currency").value("ARS"))
            .andReturn();
    String sessionId = jsonField(startResult, "checkoutSessionId");

    // 2. Re-call /cart/checkout → 200 (reuse)
    mockMvc
        .perform(post("/api/v1/cart/checkout").header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.checkoutSessionId").value(sessionId));

    // 3. Set shipping address
    mockMvc
        .perform(
            put("/api/v1/checkout/sessions/" + sessionId + "/shipping-address")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ADDRESS_BODY))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shippingAddress.addressLine").value("Av. Independencia 1234"));

    // 4. Create payment intent (Idempotency-Key required)
    String intentKey = UUID.randomUUID().toString();
    MvcResult intentResult =
        mockMvc
            .perform(
                post("/api/v1/checkout/sessions/" + sessionId + "/payment-intents")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Idempotency-Key", intentKey))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.paymentId").exists())
            .andExpect(jsonPath("$.providerPaymentId").exists())
            .andExpect(jsonPath("$.checkoutUrl").exists())
            .andExpect(jsonPath("$.amount").value("3000.00"))
            .andExpect(jsonPath("$.status").value("PENDING"))
            .andReturn();
    String providerPaymentId = jsonField(intentResult, "providerPaymentId");

    // Session should have advanced to AWAITING_PAYMENT
    mockMvc
        .perform(
            get("/api/v1/checkout/sessions/" + sessionId)
                .header("Authorization", "Bearer " + accessToken))
        .andExpect(jsonPath("$.status").value("AWAITING_PAYMENT"));

    // 5. Approve via fake provider → webhook fires synchronously → session CONFIRMED + order
    // created
    mockMvc
        .perform(post("/api/v1/fake-provider/payments/" + providerPaymentId + "/approve"))
        .andExpect(status().isOk());

    // 6. Confirm endpoint (idempotent — converges with webhook)
    String confirmKey = UUID.randomUUID().toString();
    MvcResult confirmResult =
        mockMvc
            .perform(
                post("/api/v1/checkout/sessions/" + sessionId + "/confirm")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Idempotency-Key", confirmKey))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("CONFIRMED"))
            .andExpect(jsonPath("$.orderId").exists())
            .andExpect(jsonPath("$.shipmentId").exists())
            .andReturn();
    String orderId = jsonField(confirmResult, "orderId");

    // 7. GET /orders/{orderId} → CONFIRMED order
    mockMvc
        .perform(get("/api/v1/orders/" + orderId).header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("CONFIRMED"))
        .andExpect(jsonPath("$.totals.grandTotal").value("3000.00"))
        .andExpect(jsonPath("$.lineItems.length()").value(1))
        .andExpect(jsonPath("$.shipment.shipmentId").exists());

    // 8. Stock was decremented from 10 to 8
    assertThat(variantRepository.findById(skuId).orElseThrow().getStock())
        .isEqualTo(initialStock - 2);

    // 9. Replay confirm with a new idempotency key → still CONFIRMED, same orderId (orchestrator
    // idempotent)
    MvcResult replayResult =
        mockMvc
            .perform(
                post("/api/v1/checkout/sessions/" + sessionId + "/confirm")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Idempotency-Key", UUID.randomUUID().toString()))
            .andExpect(status().isCreated())
            .andReturn();
    assertThat(jsonField(replayResult, "orderId")).isEqualTo(orderId);
  }

  @Test
  void rejectWebhookFailsTheSessionAndConfirmReturns402() throws Exception {
    String sessionId = startSessionWithAddress();
    String providerPaymentId = createIntentAndReturnProviderId(sessionId);

    mockMvc
        .perform(post("/api/v1/fake-provider/payments/" + providerPaymentId + "/reject"))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            get("/api/v1/checkout/sessions/" + sessionId)
                .header("Authorization", "Bearer " + accessToken))
        .andExpect(jsonPath("$.status").value("FAILED"));

    mockMvc
        .perform(
            post("/api/v1/checkout/sessions/" + sessionId + "/confirm")
                .header("Authorization", "Bearer " + accessToken)
                .header("Idempotency-Key", UUID.randomUUID().toString()))
        .andExpect(status().isPaymentRequired());
  }

  @Test
  void processingWebhookKeepsSessionAwaitingAndConfirmReturns202() throws Exception {
    String sessionId = startSessionWithAddress();
    String providerPaymentId = createIntentAndReturnProviderId(sessionId);

    mockMvc
        .perform(post("/api/v1/fake-provider/payments/" + providerPaymentId + "/processing"))
        .andExpect(status().isOk());

    mockMvc
        .perform(
            post("/api/v1/checkout/sessions/" + sessionId + "/confirm")
                .header("Authorization", "Bearer " + accessToken)
                .header("Idempotency-Key", UUID.randomUUID().toString()))
        .andExpect(status().isAccepted());
  }

  @Test
  void paymentIntentWithoutShippingAddressRejectsWith409() throws Exception {
    MvcResult startResult =
        mockMvc
            .perform(post("/api/v1/cart/checkout").header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isCreated())
            .andReturn();
    String sessionId = jsonField(startResult, "checkoutSessionId");

    mockMvc
        .perform(
            post("/api/v1/checkout/sessions/" + sessionId + "/payment-intents")
                .header("Authorization", "Bearer " + accessToken)
                .header("Idempotency-Key", UUID.randomUUID().toString()))
        .andExpect(status().isConflict());
  }

  @Test
  void webhookDeliveredTwiceIsIdempotent() throws Exception {
    String sessionId = startSessionWithAddress();
    String providerPaymentId = createIntentAndReturnProviderId(sessionId);

    mockMvc
        .perform(
            post("/api/v1/payments/webhook/fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    { "providerPaymentId": "%s", "status": "PAID" }
                    """
                        .formatted(providerPaymentId)))
        .andExpect(status().isOk());

    // Second delivery: should NOT throw and should not double-create the order
    mockMvc
        .perform(
            post("/api/v1/payments/webhook/fake")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    { "providerPaymentId": "%s", "status": "PAID" }
                    """
                        .formatted(providerPaymentId)))
        .andExpect(status().isOk());

    long orderCount = orderRepository.count();
    assertThat(orderCount).isEqualTo(1);
  }

  /* ---------- helpers ---------- */

  private String registerAndLogin() throws Exception {
    String email = "buyer-" + UUID.randomUUID() + "@example.com";
    String password = "Strong-pass1";
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
            .andReturn();
    JsonNode tokens = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    return tokens.get("accessToken").asText();
  }

  private String startSessionWithAddress() throws Exception {
    MvcResult startResult =
        mockMvc
            .perform(post("/api/v1/cart/checkout").header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isCreated())
            .andReturn();
    String sessionId = jsonField(startResult, "checkoutSessionId");
    mockMvc
        .perform(
            put("/api/v1/checkout/sessions/" + sessionId + "/shipping-address")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ADDRESS_BODY))
        .andExpect(status().isOk());
    return sessionId;
  }

  private String createIntentAndReturnProviderId(String sessionId) throws Exception {
    MvcResult intentResult =
        mockMvc
            .perform(
                post("/api/v1/checkout/sessions/" + sessionId + "/payment-intents")
                    .header("Authorization", "Bearer " + accessToken)
                    .header("Idempotency-Key", UUID.randomUUID().toString()))
            .andExpect(status().isCreated())
            .andReturn();
    return jsonField(intentResult, "providerPaymentId");
  }

  private String jsonField(MvcResult result, String field) throws Exception {
    JsonNode tree = objectMapper.readTree(result.getResponse().getContentAsString());
    return tree.get(field).asText();
  }
}
