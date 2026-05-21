package com.virtualpet.backend.auth.repository;

import com.virtualpet.backend.auth.domain.CustomerEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<CustomerEntity, UUID> {
  Optional<CustomerEntity> findByDni(String dni);
}
