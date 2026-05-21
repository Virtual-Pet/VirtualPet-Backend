package com.virtualpet.backend.catalog.repository;

import com.virtualpet.backend.catalog.domain.ProductVariantEntity;
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

  long countByProductId(UUID productId);

  @Query("SELECT v FROM ProductVariantEntity v JOIN FETCH v.product WHERE v.id = :id")
  Optional<ProductVariantEntity> findByIdWithProduct(@Param("id") UUID id);

  @Query(
      """
            SELECT v FROM ProductVariantEntity v
            JOIN FETCH v.product p
            JOIN FETCH p.category
            WHERE UPPER(v.sku) = UPPER(:sku) AND p.active = true
            """)
  Optional<ProductVariantEntity> findBySkuWithProduct(@Param("sku") String sku);

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query(
      "UPDATE ProductVariantEntity v SET v.stock = v.stock - :qty WHERE v.id = :id AND v.stock >= :qty")
  int decrementStock(@Param("id") UUID id, @Param("qty") int qty);
}
