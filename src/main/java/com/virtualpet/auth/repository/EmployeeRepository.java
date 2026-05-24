package com.virtualpet.auth.repository;

import com.virtualpet.auth.domain.EmployeeEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeRepository extends JpaRepository<EmployeeEntity, UUID> {
  Optional<EmployeeEntity> findByLegajo(String legajo);
}
