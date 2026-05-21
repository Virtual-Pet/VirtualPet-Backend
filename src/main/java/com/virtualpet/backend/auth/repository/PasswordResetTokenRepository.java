package com.virtualpet.backend.auth.repository;

import com.virtualpet.backend.auth.domain.PasswordResetTokenEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository
    extends JpaRepository<PasswordResetTokenEntity, UUID> {
  Optional<PasswordResetTokenEntity> findByTokenAndUsedAtIsNull(String token);

  List<PasswordResetTokenEntity> findByUserIdAndUsedAtIsNull(UUID userId);
}
