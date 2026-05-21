package com.virtualpet.backend.auth.repository;

import com.virtualpet.backend.auth.domain.RefreshTokenEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {
  Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

  // Para revocar todos los tokens viejos cuando el usuario hace login
  @Modifying
  @Query(
      "UPDATE RefreshTokenEntity r SET r.revoked = true WHERE r.user.id = :userId AND r.revoked = false")
  void revokeAllUserTokens(UUID userId);
}
