package com.virtualpet.shipments.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.virtualpet.TestRedisConfiguration;
import com.virtualpet.auth.domain.EmployeeEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.domain.enums.UserRole;
import com.virtualpet.auth.repository.EmployeeRepository;
import com.virtualpet.auth.repository.UserRepository;
import com.virtualpet.catalog.domain.CategoryEntity;
import com.virtualpet.catalog.domain.ProductEntity;
import com.virtualpet.catalog.domain.ProductVariantEntity;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestRedisConfiguration.class)
class ShipmentControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private PlatformTransactionManager txManager;
  @Autowired private ShipmentRepository shipmentRepository;
  @Autowired private UserRepository userRepository;
  @Autowired private EmployeeRepository employeeRepository;
  @Autowired private PasswordEncoder passwordEncoder;

  @PersistenceContext private EntityManager em;

  private String customerToken;
  private String employeeToken;
  private UUID shipmentId;

  private static final String ADDRESS_BODY =
      """
      { "addressLine": "Av. Test 200", "city": "MDP", "postalCode": "7600" }
      """;

  @BeforeEach
  void setup() throws Exception {
    cleanup();
    UUID skuId = seedCatalog();
    customerToken = registerCustomerAndLogin();
    employeeToken = createEmployeeAndLogin();
    runCheckout(skuId);
    shipmentId =
        shipmentRepository.findAll().stream()
            .findFirst()
            .orElseThrow(() -> new AssertionError("No shipment seeded"))
            .getId();
  }

  /* ---------- list ---------- */

  @Test
  void customerSeesOwnShipmentsWithCursorEnvelope() throws Exception {
    mockMvc
        .perform(get("/api/v1/shipments").header("Authorization", "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].shipmentId").value(shipmentId.toString()))
        .andExpect(jsonPath("$.data[0].status").value("CONFIRMED"))
        .andExpect(jsonPath("$.hasMore").value(false));
  }

  @Test
  void customerCannotSeeAnotherUsersShipments() throws Exception {
    String otherEmail = "other-" + UUID.randomUUID() + "@example.com";
    String otherPwd = "Strong-pass1";
    registerCustomer(otherEmail, otherPwd);
    String otherToken = login(otherEmail, otherPwd);

    mockMvc
        .perform(get("/api/v1/shipments").header("Authorization", "Bearer " + otherToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  @Test
  void employeeCanFilterByStatus() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/shipments")
                .header("Authorization", "Bearer " + employeeToken)
                .param("status", "CONFIRMED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1));

    mockMvc
        .perform(
            get("/api/v1/shipments")
                .header("Authorization", "Bearer " + employeeToken)
                .param("status", "DELIVERED"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(0));
  }

  /* ---------- detail ---------- */

  @Test
  void getReturnsShipmentWithStatusHistory() throws Exception {
    mockMvc
        .perform(
            get("/api/v1/shipments/" + shipmentId)
                .header("Authorization", "Bearer " + customerToken))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.shipmentId").value(shipmentId.toString()))
        .andExpect(jsonPath("$.status").value("CONFIRMED"))
        .andExpect(jsonPath("$.shippingAddress.addressLine").value("Av. Test 200"))
        .andExpect(jsonPath("$.statusHistory.length()").value(1))
        .andExpect(jsonPath("$.statusHistory[0].status").value("CONFIRMED"));
  }

  /* ---------- advance ---------- */

  @Test
  void employeeAdvancesShipmentForward() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/shipments/" + shipmentId)
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PREPARED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("PREPARED"))
        .andExpect(jsonPath("$.statusHistory.length()").value(2));

    mockMvc
        .perform(
            patch("/api/v1/shipments/" + shipmentId)
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"IN_TRANSIT\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("IN_TRANSIT"));

    mockMvc
        .perform(
            patch("/api/v1/shipments/" + shipmentId)
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DELIVERED\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("DELIVERED"));

    assertThat(shipmentRepository.findById(shipmentId).orElseThrow().getStatus())
        .isEqualTo(ShipmentStatus.DELIVERED);
  }

  @Test
  void customerCannotAdvanceShipment() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/shipments/" + shipmentId)
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"PREPARED\"}"))
        .andExpect(status().isForbidden())
        .andExpect(header().string("Content-Type", "application/problem+json"));
  }

  @Test
  void cannotSkipForwardOrTargetCancelled() throws Exception {
    mockMvc
        .perform(
            patch("/api/v1/shipments/" + shipmentId)
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"IN_TRANSIT\"}"))
        .andExpect(status().isConflict());

    mockMvc
        .perform(
            patch("/api/v1/shipments/" + shipmentId)
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"CANCELLED\"}"))
        .andExpect(status().isUnprocessableContent());

    assertThat(shipmentRepository.findById(shipmentId).orElseThrow().getStatus())
        .isEqualTo(ShipmentStatus.CONFIRMED);
  }

  /* ---------- helpers ---------- */

  private void cleanup() {
    new TransactionTemplate(txManager)
        .executeWithoutResult(
            tx -> {
              em.createNativeQuery("DELETE FROM shipments.shipment_status").executeUpdate();
              em.createNativeQuery("DELETE FROM shipments.shipments").executeUpdate();
              em.createQuery("DELETE FROM PaymentEntity").executeUpdate();
              em.createQuery("DELETE FROM OrderItemEntity").executeUpdate();
              em.createQuery("DELETE FROM OrderEntity").executeUpdate();
              em.createQuery("DELETE FROM ProductVariantEntity").executeUpdate();
              em.createQuery("DELETE FROM ProductEntity").executeUpdate();
              em.createQuery("DELETE FROM CategoryEntity").executeUpdate();
              em.createQuery("DELETE FROM EmployeeEntity").executeUpdate();
              em.createQuery("DELETE FROM CustomerEntity").executeUpdate();
              em.createQuery("DELETE FROM RefreshTokenEntity").executeUpdate();
              em.createQuery("DELETE FROM UserEntity").executeUpdate();
              em.flush();
            });
  }

  private UUID seedCatalog() {
    return new TransactionTemplate(txManager)
        .execute(
            tx -> {
              CategoryEntity c = new CategoryEntity();
              c.setName("X");
              c.setSlug("x-" + UUID.randomUUID());
              em.persist(c);
              ProductEntity p = new ProductEntity();
              p.setName("X");
              p.setCategory(c);
              p.setActive(true);
              p.setCreatedAt(Instant.now());
              em.persist(p);
              ProductVariantEntity v = new ProductVariantEntity();
              v.setProduct(p);
              v.setSku("SKU-" + UUID.randomUUID().toString().substring(0, 8));
              v.setAttributes("{}");
              v.setPrice(new BigDecimal("100.00"));
              v.setStock(10);
              em.persist(v);
              em.flush();
              return v.getId();
            });
  }

  private String registerCustomerAndLogin() throws Exception {
    String email = "buyer-" + UUID.randomUUID() + "@example.com";
    String pwd = "Strong-pass1";
    registerCustomer(email, pwd);
    return login(email, pwd);
  }

  private void registerCustomer(String email, String pwd) throws Exception {
    mockMvc
        .perform(
            post("/api/v1/auth/register/customer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    { "email": "%s", "password": "%s", "firstName": "B", "lastName": "X" }
                    """
                        .formatted(email, pwd)))
        .andExpect(status().isCreated());
  }

  private String createEmployeeAndLogin() throws Exception {
    String email = "ops-" + UUID.randomUUID() + "@virtualpet.local";
    String pwd = "Strong-pass1";
    new TransactionTemplate(txManager)
        .executeWithoutResult(
            tx -> {
              UserEntity u =
                  userRepository.save(
                      UserEntity.builder()
                          .email(email)
                          .passwordHash(passwordEncoder.encode(pwd))
                          .role(UserRole.ROLE_EMPLOYEE)
                          .active(true)
                          .build());
              employeeRepository.save(
                  EmployeeEntity.builder().userId(u.getId()).name("Ops").lastname("X").build());
            });
    return login(email, pwd);
  }

  private String login(String email, String pwd) throws Exception {
    MvcResult result =
        mockMvc
            .perform(
                post("/api/v1/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                        { "email": "%s", "password": "%s" }
                        """
                            .formatted(email, pwd)))
            .andExpect(status().isOk())
            .andReturn();
    JsonNode json = objectMapper.readTree(result.getResponse().getContentAsString());
    return json.get("accessToken").asString();
  }

  private void runCheckout(UUID skuId) throws Exception {
    mockMvc
        .perform(
            put("/api/v1/cart/items/" + skuId)
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"quantity\":1}"))
        .andExpect(status().isOk());

    MvcResult start =
        mockMvc
            .perform(
                post("/api/v1/cart/checkout").header("Authorization", "Bearer " + customerToken))
            .andReturn();
    String sessionId =
        objectMapper
            .readTree(start.getResponse().getContentAsString())
            .get("checkoutSessionId")
            .asString();

    mockMvc
        .perform(
            put("/api/v1/checkout/sessions/" + sessionId + "/shipping-address")
                .header("Authorization", "Bearer " + customerToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ADDRESS_BODY))
        .andExpect(status().isOk());

    MvcResult intent =
        mockMvc
            .perform(
                post("/api/v1/checkout/sessions/" + sessionId + "/payment-intents")
                    .header("Authorization", "Bearer " + customerToken)
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

    mockMvc
        .perform(
            post("/api/v1/checkout/sessions/" + sessionId + "/confirm")
                .header("Authorization", "Bearer " + customerToken)
                .header("Idempotency-Key", UUID.randomUUID().toString()))
        .andExpect(status().isCreated());
  }

  @SuppressWarnings("unused")
  private ShipmentEntity reload() {
    return shipmentRepository.findById(shipmentId).orElseThrow();
  }
}
