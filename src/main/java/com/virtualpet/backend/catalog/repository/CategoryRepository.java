package com.virtualpet.backend.catalog.repository;

import com.virtualpet.backend.catalog.domain.CategoryEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {

  @Query(
      """
            SELECT c.id, c.name, c.slug, COUNT(p)
            FROM CategoryEntity c
            LEFT JOIN ProductEntity p ON p.category = c AND p.active = true
            GROUP BY c.id, c.name, c.slug
            ORDER BY c.name
            """)
  List<Object[]> findAllWithProductCounts();
}
