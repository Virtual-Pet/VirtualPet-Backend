package com.virtualpet.catalog.repository;

import com.virtualpet.catalog.domain.ProductVariantEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface ProductVariantRepository extends JpaRepository<ProductVariantEntity, UUID> {

  List<ProductVariantEntity> findByProductId(UUID productId);

  List<ProductVariantEntity> findByProductIdIn(java.util.Collection<UUID> productIds);

  long countByProductId(UUID productId);

  @Query("SELECT v FROM ProductVariantEntity v JOIN FETCH v.product WHERE v.id = :id")
  Optional<ProductVariantEntity> findByIdWithProduct(@Param("id") UUID id);

  @Query("SELECT v FROM ProductVariantEntity v JOIN FETCH v.product WHERE v.id IN :ids")
  List<ProductVariantEntity> findAllByIdInWithProduct(@Param("ids") java.util.Collection<UUID> ids);

  @Query(
      """
            SELECT v FROM ProductVariantEntity v
            JOIN FETCH v.product p
            JOIN FETCH p.category
            WHERE UPPER(v.sku) = UPPER(:sku) AND p.active = true
            """)
  Optional<ProductVariantEntity> findBySkuWithProduct(@Param("sku") String sku);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Transactional
  @Query(
      "UPDATE ProductVariantEntity v SET v.stock = v.stock - :qty WHERE v.id = :id AND v.stock >= :qty")
  int decrementStock(@Param("id") UUID id, @Param("qty") int qty);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Transactional
  @Query("UPDATE ProductVariantEntity v SET v.stock = v.stock + :qty WHERE v.id = :id")
  int incrementStock(@Param("id") UUID id, @Param("qty") int qty);
}
