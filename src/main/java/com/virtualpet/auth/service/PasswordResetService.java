package com.virtualpet.auth.service;

import com.virtualpet.auth.domain.PasswordResetTokenEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.dto.AuthDTO.ForgotPasswordRequest;
import com.virtualpet.auth.dto.AuthDTO.ResetPasswordRequest;
import com.virtualpet.auth.repository.PasswordResetTokenRepository;
import com.virtualpet.auth.repository.UserRepository;
import com.virtualpet.common.config.VirtualPetProperties;
import com.virtualpet.common.exception.ApiException;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository tokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final PasswordResetMailService mailService;
  private final RefreshTokenService refreshTokenService;
  private final VirtualPetProperties properties;

  @Transactional
  public void requestReset(ForgotPasswordRequest request) {
    String email = request.email().toLowerCase();
    log.info("Password reset requested for: {}", email);
    userRepository.findByEmailIgnoreCase(email).ifPresent(this::createAndSendToken);
  }

  @Transactional
  public void resetPassword(ResetPasswordRequest request) {
    PasswordResetTokenEntity tokenEntity =
        tokenRepository
            .findByTokenAndUsedAtIsNull(request.token())
            .orElseThrow(
                () -> {
                  log.warn("Password reset failed — token not found or already used");
                  return new ApiException(
                      HttpStatus.BAD_REQUEST, "Invalid or already used reset token");
                });

    if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
      log.warn(
          "Password reset failed — token expired for user: {}", tokenEntity.getUser().getEmail());
      throw new ApiException(HttpStatus.BAD_REQUEST, "Reset token expired");
    }

    UserEntity user = tokenEntity.getUser();
    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    tokenEntity.setUsedAt(Instant.now());
    invalidateActiveTokens(user.getId());
    refreshTokenService.revokeAllUserTokens(user.getId());
    log.info("Password reset successfully for: {}", user.getEmail());
  }

  private void createAndSendToken(UserEntity user) {
    invalidateActiveTokens(user.getId());

    String tokenValue = UUID.randomUUID().toString().replace("-", "");
    PasswordResetTokenEntity token = new PasswordResetTokenEntity();
    token.setUser(user);
    token.setToken(tokenValue);
    token.setExpiresAt(Instant.now().plusSeconds(3600));
    tokenRepository.save(token);

    String resetUrl = properties.getApp().getFrontendUrl() + "/reset-password?token=" + tokenValue;
    log.debug("Sending password reset link to: {}", user.getEmail());
    mailService.sendResetLink(user.getEmail(), resetUrl);
  }

  private void invalidateActiveTokens(UUID userId) {
    Instant now = Instant.now();
    for (PasswordResetTokenEntity active : tokenRepository.findByUserIdAndUsedAtIsNull(userId)) {
      active.setUsedAt(now);
    }
  }
}
