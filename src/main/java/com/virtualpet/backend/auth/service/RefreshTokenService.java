package com.virtualpet.backend.auth.service;

import com.virtualpet.backend.auth.domain.RefreshTokenEntity;
import com.virtualpet.backend.auth.domain.UserEntity;
import com.virtualpet.backend.auth.repository.RefreshTokenRepository;
// import com.virtualpet.backend.auth.repository.UserRepository;
import com.virtualpet.backend.shared.exception.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

  private final RefreshTokenRepository refreshTokenRepository;

  // private final UserRepository userRepository;

  @Transactional
  public String createRefreshToken(UserEntity user) {
    // Revocamos sesiones anteriores (Otra opc: se puede dejar múltiples sesiones activas)
    refreshTokenRepository.revokeAllUserTokens(user.getId());

    // Generamos un token aleatorio en texto plano
    String plainToken = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();

    RefreshTokenEntity refreshToken =
        RefreshTokenEntity.builder()
            .user(user)
            .tokenHash(hashToken(plainToken)) // Guardamos el hash en DB
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .revoked(false)
            .build();

    refreshTokenRepository.save(refreshToken);
    log.debug("Refresh token created for user: {}", user.getId());

    // Devolvemos el token en texto plano al usuario
    return plainToken;
  }

  @Transactional(readOnly = true)
  public UserEntity verifyExpiration(String plainToken) {
    RefreshTokenEntity tokenEntity =
        refreshTokenRepository
            .findByTokenHash(hashToken(plainToken))
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token inválido"));

    if (tokenEntity.getRevoked() || tokenEntity.getExpiresAt().isBefore(Instant.now())) {
      log.warn(
          "Refresh token rejected — revoked or expired for user: {}",
          tokenEntity.getUser().getId());
      throw new ApiException(
          HttpStatus.UNAUTHORIZED, "Sesión expirada. Por favor inicie sesión nuevamente.");
    }

    log.info("Session refreshed for user: {}", tokenEntity.getUser().getId());
    return tokenEntity.getUser();
  }

  // Hashea el token con SHA-256
  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error interno de seguridad", e);
    }
  }
}
