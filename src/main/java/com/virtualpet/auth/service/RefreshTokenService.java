package com.virtualpet.auth.service;

import com.virtualpet.auth.domain.RefreshTokenEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.repository.RefreshTokenRepository;
import com.virtualpet.common.exception.ApiException;
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

  @Transactional
  public String createRefreshToken(UserEntity user) {
    refreshTokenRepository.revokeAllUserTokens(user.getId());

    String plainToken = UUID.randomUUID() + "-" + UUID.randomUUID();
    RefreshTokenEntity refreshToken =
        RefreshTokenEntity.builder()
            .user(user)
            .tokenHash(hashToken(plainToken))
            .expiresAt(Instant.now().plus(7, ChronoUnit.DAYS))
            .revoked(false)
            .build();

    refreshTokenRepository.save(refreshToken);
    log.debug("Refresh token created for user: {}", user.getId());
    return plainToken;
  }

  @Transactional(readOnly = true)
  public UserEntity verifyExpiration(String plainToken) {
    RefreshTokenEntity tokenEntity =
        refreshTokenRepository
            .findByTokenHash(hashToken(plainToken))
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));

    if (Boolean.TRUE.equals(tokenEntity.getRevoked())
        || tokenEntity.getExpiresAt().isBefore(Instant.now())) {
      log.warn(
          "Refresh token rejected — revoked or expired for user: {}",
          tokenEntity.getUser().getId());
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Session expired");
    }

    log.info("Session refreshed for user: {}", tokenEntity.getUser().getId());
    return tokenEntity.getUser();
  }

  @Transactional
  public void revokeByPlainToken(String plainToken) {
    refreshTokenRepository
        .findByTokenHash(hashToken(plainToken))
        .ifPresent(
            token -> {
              if (Boolean.FALSE.equals(token.getRevoked())) {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
                log.info("Refresh token revoked for user: {}", token.getUser().getId());
              }
            });
  }

  @Transactional
  public void revokeAllUserTokens(UUID userId) {
    refreshTokenRepository.revokeAllUserTokens(userId);
  }

  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
      return Base64.getEncoder().encodeToString(hash);
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
