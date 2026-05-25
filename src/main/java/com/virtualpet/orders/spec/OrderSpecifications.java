package com.virtualpet.orders.spec;

import com.virtualpet.common.pagination.Cursor;
import com.virtualpet.orders.domain.OrderEntity;
import com.virtualpet.orders.domain.OrderStatus;
import jakarta.persistence.criteria.Predicate;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecifications {

  private OrderSpecifications() {}

  public static Specification<OrderEntity> byUser(UUID userId) {
    if (userId == null) {
      return null;
    }
    return (root, query, cb) -> cb.equal(root.get("userId"), userId);
  }

  public static Specification<OrderEntity> byStatus(OrderStatus status) {
    if (status == null) {
      return null;
    }
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }

  /** Keyset predicate: rows older than the cursor, or same instant with a smaller id. */
  public static Specification<OrderEntity> afterCursor(Cursor cursor) {
    if (cursor == null) {
      return null;
    }
    return (root, query, cb) -> {
      var createdAt = root.<java.time.Instant>get("createdAt");
      var id = root.<UUID>get("id");
      Predicate olderInstant = cb.lessThan(createdAt, cursor.sortKeyAsInstant());
      Predicate sameInstantSmallerId =
          cb.and(cb.equal(createdAt, cursor.sortKeyAsInstant()), cb.lessThan(id, cursor.id()));
      return cb.or(olderInstant, sameInstantSmallerId);
    };
  }
}
