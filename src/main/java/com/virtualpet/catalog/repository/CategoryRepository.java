package com.virtualpet.catalog.repository;

import com.virtualpet.catalog.domain.CategoryEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID> {
  Optional<CategoryEntity> findBySlug(String slug);
}
