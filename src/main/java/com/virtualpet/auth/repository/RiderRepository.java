package com.virtualpet.auth.repository;

import com.virtualpet.auth.domain.RiderEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RiderRepository extends JpaRepository<RiderEntity, UUID> {}
