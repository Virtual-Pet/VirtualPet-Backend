package com.virtualpet.catalog.spec;

import com.virtualpet.catalog.domain.ProductEntity;
import com.virtualpet.catalog.domain.ProductVariantEntity;
import com.virtualpet.common.pagination.Cursor;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecifications {

  private ProductSpecifications() {}

  public static Specification<ProductEntity> active() {
    return (root, query, cb) -> cb.isTrue(root.get("active"));
  }

  public static Specification<ProductEntity> search(String q) {
    if (q == null || q.isBlank()) {
      return null;
    }
    String pattern = "%" + q.toLowerCase() + "%";
    return (root, query, cb) ->
        cb.or(
            cb.like(cb.lower(root.get("name")), pattern),
            cb.like(cb.lower(cb.coalesce(root.get("description"), "")), pattern));
  }

  public static Specification<ProductEntity> category(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return (root, query, cb) -> {
      Join<Object, Object> category = root.join("category", JoinType.INNER);
      String lower = value.toLowerCase();
      return cb.or(
          cb.equal(cb.lower(category.get("slug")), lower),
          cb.equal(cb.lower(category.get("name")), lower));
    };
  }

  public static Specification<ProductEntity> petType(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return (root, query, cb) -> cb.equal(cb.lower(root.get("petType")), value.toLowerCase());
  }

  public static Specification<ProductEntity> priceRange(BigDecimal min, BigDecimal max) {
    if (min == null && max == null) {
      return null;
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

  /**
   * Keyset predicate for the cursor: rows older than the cursor instant, or same instant with a
   * smaller id (lexicographic on UUID). The list is ordered by (createdAt DESC, id DESC) so this
   * predicate yields the next page deterministically.
   */
  public static Specification<ProductEntity> afterCursor(Cursor cursor) {
    if (cursor == null) {
      return null;
    }
    return (root, query, cb) -> {
      var createdAt = root.<java.time.Instant>get("createdAt");
      var id = root.<java.util.UUID>get("id");
      Predicate olderInstant = cb.lessThan(createdAt, cursor.sortKeyAsInstant());
      Predicate sameInstantSmallerId =
          cb.and(cb.equal(createdAt, cursor.sortKeyAsInstant()), cb.lessThan(id, cursor.id()));
      return cb.or(olderInstant, sameInstantSmallerId);
    };
  }
}
