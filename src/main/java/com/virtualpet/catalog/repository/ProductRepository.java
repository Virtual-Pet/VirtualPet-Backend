package com.virtualpet.catalog.repository;

import com.virtualpet.catalog.domain.ProductEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository
    extends JpaRepository<ProductEntity, UUID>, JpaSpecificationExecutor<ProductEntity> {

  @Query(
      """
      SELECT DISTINCT p FROM ProductEntity p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.variants
       WHERE p.id = :id AND p.active = true
      """)
  Optional<ProductEntity> findByIdWithVariantsAndCategory(@Param("id") UUID id);
}
