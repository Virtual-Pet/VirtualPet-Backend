package com.virtualpet.catalog.config;

import com.virtualpet.catalog.domain.CategoryEntity;
import com.virtualpet.catalog.domain.ProductEntity;
import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.catalog.repository.CategoryRepository;
import com.virtualpet.catalog.repository.ProductRepository;
import com.virtualpet.catalog.repository.ProductVariantRepository;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@Profile("dev")
public class CatalogDataSeeder implements ApplicationRunner {

  private final CategoryRepository categoryRepository;
  private final ProductRepository productRepository;
  private final ProductVariantRepository productVariantRepository;

  @Override
  public void run(ApplicationArguments args) {
    if (categoryRepository.count() == 0) {
      log.info("Seeding catalog data...");
      seedCatalog();
      log.info("Catalog data ready.");
    }
  }

  private void seedCatalog() {
    // 1. Categories
    CategoryEntity cAlimentos = createCategory("Alimentos", "alimentos");
    CategoryEntity cJuguetes = createCategory("Juguetes", "juguetes");
    CategoryEntity cHigiene = createCategory("Higiene", "higiene");
    CategoryEntity cCamas = createCategory("Camas", "camas");

    // 2. Products and Variants
    createProductWithVariant(
        "Pro Plan Adulto Mordida Grande 15kg",
        "Alimento balanceado premium para perros adultos de raza grande. Fórmula optimizada con vitaminas y minerales para mantener músculos fuertes y pelaje sano.",
        "Pro Plan",
        "perro",
        cAlimentos,
        "PROPLAN-AD-15KG",
        new BigDecimal("45000.00"),
        25,
        "https://http2.mlstatic.com/D_NQ_NP_604733-MLA49791040316_042022-O.webp");

    createProductWithVariant(
        "Hueso de Goma Resistente Kong",
        "Juguete masticable de caucho natural ultrarresistente. Ideal para perros destructores y para mantener su higiene dental jugando.",
        "Kong",
        "perro",
        cJuguetes,
        "KONG-HUESO-L",
        new BigDecimal("12500.00"),
        15,
        "https://http2.mlstatic.com/D_NQ_NP_935639-MLA71077797746_082023-O.webp");

    createProductWithVariant(
        "Shampoo Osspret Hipoalergénico 250ml",
        "Shampoo dermatológico sin colorantes ni perfumes artificiales. Calma irritaciones y deja el pelo suave, especial para pieles sensibles.",
        "Osspret",
        "gato",
        cHigiene,
        "OSSPRET-SH-250",
        new BigDecimal("4800.00"),
        50,
        "https://http2.mlstatic.com/D_NQ_NP_629532-MLA51368140417_092022-O.webp");

    createProductWithVariant(
        "Cama Moises Premium Grande",
        "Cama súper acolchada con funda desmontable y lavable. Base impermeable y bordes elevados para mayor confort térmico y descanso.",
        "Mascotify",
        "perro",
        cCamas,
        "CAMA-MOISES-G",
        new BigDecimal("28000.00"),
        10,
        "https://http2.mlstatic.com/D_NQ_NP_890184-MLA72089408016_102023-O.webp");
  }

  private CategoryEntity createCategory(String name, String slug) {
    CategoryEntity cat = new CategoryEntity();
    cat.setName(name);
    cat.setSlug(slug);
    return categoryRepository.save(cat);
  }

  private void createProductWithVariant(
      String name,
      String description,
      String brand,
      String petType,
      CategoryEntity category,
      String sku,
      BigDecimal price,
      int stock,
      String imageUrl) {

    ProductEntity product = new ProductEntity();
    product.setName(name);
    product.setDescription(description);
    product.setBrand(brand);
    product.setPetType(petType);
    product.setCategory(category);
    product.setActive(true);
    product = productRepository.save(product);

    ProductVariantEntity variant = new ProductVariantEntity();
    variant.setProduct(product);
    variant.setSku(sku);
    variant.setAttributes("{}");
    variant.setPrice(price);
    variant.setStock(stock);
    variant.setImageUrl(imageUrl);
    productVariantRepository.save(variant);
  }
}
