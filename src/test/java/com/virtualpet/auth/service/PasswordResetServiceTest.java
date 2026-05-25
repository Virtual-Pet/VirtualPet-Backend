package com.virtualpet.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.virtualpet.auth.domain.PasswordResetTokenEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.domain.enums.UserRole;
import com.virtualpet.auth.dto.AuthDTO.ForgotPasswordRequest;
import com.virtualpet.auth.dto.AuthDTO.ResetPasswordRequest;
import com.virtualpet.auth.repository.PasswordResetTokenRepository;
import com.virtualpet.auth.repository.UserRepository;
import com.virtualpet.common.config.VirtualPetProperties;
import com.virtualpet.common.exception.ApiException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

  @Mock private UserRepository userRepository;
  @Mock private PasswordResetTokenRepository tokenRepository;
  @Mock private PasswordEncoder passwordEncoder;
  @Mock private PasswordResetMailService mailService;
  @Mock private RefreshTokenService refreshTokenService;

  private PasswordResetService service;
  private UserEntity user;

  @BeforeEach
  void setup() {
    VirtualPetProperties properties = new VirtualPetProperties();
    properties.getApp().setFrontendUrl("http://localhost:3000");
    service =
        new PasswordResetService(
            userRepository,
            tokenRepository,
            passwordEncoder,
            mailService,
            refreshTokenService,
            properties);
    user =
        UserEntity.builder()
            .id(UUID.randomUUID())
            .email("user@example.com")
            .passwordHash("hash")
            .role(UserRole.ROLE_CUSTOMER)
            .build();
  }

  @Test
  void requestResetSendsMailWhenUserExists() {
    when(userRepository.findByEmailIgnoreCase("user@example.com")).thenReturn(Optional.of(user));
    when(tokenRepository.findByUserIdAndUsedAtIsNull(user.getId())).thenReturn(List.of());

    service.requestReset(new ForgotPasswordRequest("USER@example.com"));

    verify(tokenRepository).save(any(PasswordResetTokenEntity.class));
    verify(mailService).sendResetLink(eq("user@example.com"), anyString());
  }

  @Test
  void requestResetIsSilentWhenUserMissing() {
    when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.empty());

    service.requestReset(new ForgotPasswordRequest("ghost@example.com"));

    verify(tokenRepository, never()).save(any());
    verify(mailService, never()).sendResetLink(anyString(), anyString());
  }

  @Test
  void resetPasswordPersistsNewHashAndMarksTokenUsedAndKillsSessions() {
    PasswordResetTokenEntity token =
        PasswordResetTokenEntity.builder()
            .user(user)
            .token("t1")
            .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
            .build();
    when(tokenRepository.findByTokenAndUsedAtIsNull("t1")).thenReturn(Optional.of(token));
    when(tokenRepository.findByUserIdAndUsedAtIsNull(user.getId())).thenReturn(List.of());
    when(passwordEncoder.encode("newPass123")).thenReturn("new-hash");

    service.resetPassword(new ResetPasswordRequest("t1", "newPass123"));

    assertThat(user.getPasswordHash()).isEqualTo("new-hash");
    assertThat(token.getUsedAt()).isNotNull();
    verify(refreshTokenService, times(1)).revokeAllUserTokens(user.getId());
  }

  @Test
  void resetPasswordRejectsInvalidToken() {
    when(tokenRepository.findByTokenAndUsedAtIsNull("nope")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.resetPassword(new ResetPasswordRequest("nope", "newPass123")))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getStatus())
        .isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void resetPasswordRejectsExpiredToken() {
    PasswordResetTokenEntity token =
        PasswordResetTokenEntity.builder()
            .user(user)
            .token("t1")
            .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
            .build();
    when(tokenRepository.findByTokenAndUsedAtIsNull("t1")).thenReturn(Optional.of(token));

    assertThatThrownBy(() -> service.resetPassword(new ResetPasswordRequest("t1", "newPass123")))
        .isInstanceOf(ApiException.class);

    verify(refreshTokenService, never()).revokeAllUserTokens(any());
  }
}
