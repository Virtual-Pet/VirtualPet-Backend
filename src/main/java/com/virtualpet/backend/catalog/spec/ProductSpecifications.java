package com.virtualpet.backend.catalog.spec;

import com.virtualpet.backend.catalog.domain.ProductEntity;
import com.virtualpet.backend.catalog.domain.ProductVariantEntity;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

  private ProductSpecifications() {}

  public static Specification<ProductEntity> active() {
    return (root, query, cb) -> cb.isTrue(root.get("active"));
  }

  public static Specification<ProductEntity> search(String q) {
    if (q == null || q.isBlank()) {
      return Specification.where(null);
    }
    String pattern = "%" + q.toLowerCase() + "%";
    return (root, query, cb) ->
        cb.or(
            cb.like(cb.lower(root.get("name")), pattern),
            cb.like(cb.lower(root.get("description")), pattern));
  }

  public static Specification<ProductEntity> categorySlug(String slug) {
    if (slug == null || slug.isBlank()) {
      return Specification.where(null);
    }
    return (root, query, cb) -> {
      Join<Object, Object> category = root.join("category", JoinType.INNER);
      return cb.equal(cb.lower(category.get("slug")), slug.toLowerCase());
    };
  }

  public static Specification<ProductEntity> brand(String brand) {
    if (brand == null || brand.isBlank()) {
      return Specification.where(null);
    }
    return (root, query, cb) -> cb.equal(cb.lower(root.get("brand")), brand.toLowerCase());
  }

  public static Specification<ProductEntity> priceRange(BigDecimal min, BigDecimal max) {
    if (min == null && max == null) {
      return Specification.where(null);
    }
    return (root, query, cb) -> {
      Join<ProductEntity, ProductVariantEntity> variants = root.join("variants", JoinType.LEFT);
      query.distinct(true);
      if (min != null && max != null) {
        return cb.between(variants.get("price"), min, max);
      }
      if (min != null) {
        return cb.greaterThanOrEqualTo(variants.get("price"), min);
      }
      return cb.lessThanOrEqualTo(variants.get("price"), max);
    };
  }
}
