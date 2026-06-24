package com.virtualpet.shipments.spec;

import com.virtualpet.common.pagination.Cursor;
import com.virtualpet.orders.domain.OrderEntity;
import com.virtualpet.shipments.domain.ShipmentEntity;
import com.virtualpet.shipments.domain.ShipmentStatus;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class ShipmentSpecifications {

  private ShipmentSpecifications() {}

  public static Specification<ShipmentEntity> byStatus(ShipmentStatus status) {
    if (status == null) {
      return null;
    }
    return (root, query, cb) -> cb.equal(root.get("status"), status);
  }

  /** Limits the result set to shipments whose order belongs to the given user. */
  public static Specification<ShipmentEntity> ownedByUser(UUID userId) {
    if (userId == null) {
      return null;
    }
    return (root, query, cb) -> {
      Subquery<UUID> orderIds = query.subquery(UUID.class);
      Root<OrderEntity> orderRoot = orderIds.from(OrderEntity.class);
      orderIds.select(orderRoot.get("id")).where(cb.equal(orderRoot.get("userId"), userId));
      return root.get("orderId").in(orderIds);
    };
  }

  /** Filters to shipments assigned to the given rider. */
  public static Specification<ShipmentEntity> byRiderId(UUID riderId) {
    if (riderId == null) {
      return null;
    }
    return (root, query, cb) -> cb.equal(root.get("riderId"), riderId);
  }

  /** Cursor keyset using the immutable createdAt + id tiebreaker. */
  public static Specification<ShipmentEntity> afterCursor(Cursor cursor) {
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
