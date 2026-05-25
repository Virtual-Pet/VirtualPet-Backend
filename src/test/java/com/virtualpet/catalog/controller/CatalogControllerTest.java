package com.virtualpet.catalog.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestRedisConfiguration.class)
@Transactional
class CatalogControllerTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private ProductRepository productRepository;
  @Autowired private ProductVariantRepository variantRepository;

  @PersistenceContext private EntityManager em;

  private CategoryEntity foodCategory;
  private CategoryEntity toyCategory;
  private UUID dogFoodId;

  @BeforeEach
  void seed() {
    variantRepository.deleteAll();
    productRepository.deleteAll();
    em.createQuery("DELETE FROM CategoryEntity").executeUpdate();

    foodCategory = newCategory("Food", "food");
    toyCategory = newCategory("Toys", "toys");
    em.persist(foodCategory);
    em.persist(toyCategory);

    dogFoodId =
        persistProduct(
            "Premium Dog Kibble",
            "High-protein kibble for adult dogs",
            "BrandA",
            "DOG",
            foodCategory,
            Instant.now().minus(2, ChronoUnit.HOURS),
            new BigDecimal("1500.00"),
            "{\"size\":\"5kg\"}",
            "https://cdn.example.com/dog-kibble.png");

    persistProduct(
        "Feather Wand Cat Toy",
        "Interactive toy for cats",
        "BrandB",
        "CAT",
        toyCategory,
        Instant.now().minus(1, ChronoUnit.HOURS),
        new BigDecimal("499.99"),
        "{\"color\":\"red\"}",
        "https://cdn.example.com/feather-wand.png");

    em.flush();
    em.clear();
  }

  @Test
  void listReturnsAllActiveProducts() throws Exception {
    mockMvc
        .perform(get("/api/v1/products"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(2))
        .andExpect(jsonPath("$.limit").value(20))
        .andExpect(jsonPath("$.hasMore").value(false))
        .andExpect(header().exists("ETag"))
        .andExpect(
            header().string("Cache-Control", org.hamcrest.Matchers.containsString("max-age=300")));
  }

  @Test
  void listFiltersByPetType() throws Exception {
    mockMvc
        .perform(get("/api/v1/products").param("petType", "DOG"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].name").value("Premium Dog Kibble"))
        .andExpect(jsonPath("$.data[0].petType").value("DOG"))
        .andExpect(jsonPath("$.data[0].basePrice").value("1500.00"));
  }

  @Test
  void listFiltersByCategorySlugOrName() throws Exception {
    mockMvc
        .perform(get("/api/v1/products").param("category", "toys"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].name").value("Feather Wand Cat Toy"));
  }

  @Test
  void listFiltersByFullTextSearch() throws Exception {
    mockMvc
        .perform(get("/api/v1/products").param("q", "kibble"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.data[0].name").value("Premium Dog Kibble"));
  }

  @Test
  void listPaginatesViaCursor() throws Exception {
    MvcResult firstPage =
        mockMvc
            .perform(get("/api/v1/products").param("limit", "1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.hasMore").value(true))
            .andExpect(jsonPath("$.nextCursor").isNotEmpty())
            .andReturn();

    JsonNode body = objectMapper.readTree(firstPage.getResponse().getContentAsString());
    String cursor = body.get("nextCursor").asText();

    mockMvc
        .perform(get("/api/v1/products").param("limit", "1").param("cursor", cursor))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.length()").value(1))
        .andExpect(jsonPath("$.hasMore").value(false))
        .andExpect(jsonPath("$.nextCursor").doesNotExist());
  }

  @Test
  void getReturnsProductWithSkusAndETag() throws Exception {
    MvcResult result =
        mockMvc
            .perform(get("/api/v1/products/" + dogFoodId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(dogFoodId.toString()))
            .andExpect(jsonPath("$.name").value("Premium Dog Kibble"))
            .andExpect(jsonPath("$.petType").value("DOG"))
            .andExpect(jsonPath("$.category").value("Food"))
            .andExpect(jsonPath("$.images.length()").value(1))
            .andExpect(jsonPath("$.skus.length()").value(1))
            .andExpect(jsonPath("$.skus[0].attributes.size").value("5kg"))
            .andExpect(jsonPath("$.skus[0].price").value("1500.00"))
            .andExpect(jsonPath("$.skus[0].available").value(true))
            .andExpect(header().exists("ETag"))
            .andReturn();

    String etag = result.getResponse().getHeader("ETag");
    assertThat(etag).isNotBlank();

    mockMvc
        .perform(get("/api/v1/products/" + dogFoodId).header("If-None-Match", etag))
        .andExpect(status().isNotModified())
        .andExpect(header().string("ETag", etag))
        .andExpect(content().string(""));
  }

  @Test
  void getMissingProductReturnsProblemDetail404() throws Exception {
    mockMvc
        .perform(get("/api/v1/products/" + UUID.randomUUID()))
        .andExpect(status().isNotFound())
        .andExpect(header().string("Content-Type", "application/problem+json"));
  }

  @Test
  void invalidCursorReturnsProblemDetail400() throws Exception {
    mockMvc
        .perform(get("/api/v1/products").param("cursor", "not-a-real-cursor"))
        .andExpect(status().isBadRequest())
        .andExpect(header().string("Content-Type", "application/problem+json"));
  }

  /* ---------- helpers ---------- */

  private CategoryEntity newCategory(String name, String slug) {
    CategoryEntity c = new CategoryEntity();
    c.setName(name);
    c.setSlug(slug);
    return c;
  }

  private UUID persistProduct(
      String name,
      String description,
      String brand,
      String petType,
      CategoryEntity category,
      Instant createdAt,
      BigDecimal price,
      String attributesJson,
      String imageUrl) {
    ProductEntity product = new ProductEntity();
    product.setName(name);
    product.setDescription(description);
    product.setBrand(brand);
    product.setPetType(petType);
    product.setCategory(category);
    product.setActive(true);
    product.setCreatedAt(createdAt);
    em.persist(product);

    ProductVariantEntity variant = new ProductVariantEntity();
    variant.setProduct(product);
    variant.setSku("SKU-" + product.getId().toString().substring(0, 8));
    variant.setAttributes(attributesJson);
    variant.setPrice(price);
    variant.setStock(10);
    variant.setImageUrl(imageUrl);
    em.persist(variant);

    return product.getId();
  }
}
