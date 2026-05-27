package com.virtualpet.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.virtualpet.auth.domain.RefreshTokenEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.domain.enums.UserRole;
import com.virtualpet.auth.repository.RefreshTokenRepository;
import com.virtualpet.common.exception.ApiException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

  @Mock private RefreshTokenRepository repository;

  private RefreshTokenService service;
  private UserEntity user;

  @BeforeEach
  void setup() {
    service = new RefreshTokenService(repository);
    user =
        UserEntity.builder()
            .id(UUID.randomUUID())
            .email("test@example.com")
            .passwordHash("hash")
            .role(UserRole.ROLE_CUSTOMER)
            .build();
  }

  @Test
  void createIssuesNonEmptyTokenAndPersistsHashedEntity() {
    when(repository.save(any(RefreshTokenEntity.class))).thenAnswer(inv -> inv.getArgument(0));

    String plain = service.createRefreshToken(user);

    assertThat(plain).isNotBlank();
    verify(repository).revokeAllUserTokens(user.getId());
    ArgumentCaptor<RefreshTokenEntity> captor = ArgumentCaptor.forClass(RefreshTokenEntity.class);
    verify(repository).save(captor.capture());
    assertThat(captor.getValue().getTokenHash()).isNotEqualTo(plain);
    assertThat(captor.getValue().getUser()).isSameAs(user);
    assertThat(captor.getValue().getRevoked()).isFalse();
  }

  @Test
  void verifyReturnsUserForValidActiveToken() {
    String plain = "plain-token-value";
    RefreshTokenEntity entity =
        RefreshTokenEntity.builder()
            .user(user)
            .tokenHash(hashOf(plain))
            .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
            .revoked(false)
            .build();
    when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(entity));

    UserEntity result = service.verifyExpiration(plain);

    assertThat(result).isSameAs(user);
  }

  @Test
  void verifyRejectsUnknownTokenWith401() {
    when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());

    assertThatThrownBy(() -> service.verifyExpiration("anything"))
        .isInstanceOf(ApiException.class)
        .extracting(e -> ((ApiException) e).getStatus())
        .isEqualTo(HttpStatus.UNAUTHORIZED);
  }

  @Test
  void verifyRejectsExpiredOrRevokedToken() {
    RefreshTokenEntity expired =
        RefreshTokenEntity.builder()
            .user(user)
            .tokenHash("h")
            .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
            .revoked(false)
            .build();
    when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(expired));

    assertThatThrownBy(() -> service.verifyExpiration("anything")).isInstanceOf(ApiException.class);
  }

  @Test
  void revokeByPlainTokenMarksActiveTokenAsRevoked() {
    RefreshTokenEntity entity =
        RefreshTokenEntity.builder()
            .user(user)
            .tokenHash("h")
            .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
            .revoked(false)
            .build();
    when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(entity));

    service.revokeByPlainToken("anything");

    assertThat(entity.getRevoked()).isTrue();
    verify(repository, times(1)).save(entity);
  }

  @Test
  void revokeByPlainTokenIsIdempotentWhenAlreadyRevoked() {
    RefreshTokenEntity entity =
        RefreshTokenEntity.builder()
            .user(user)
            .tokenHash("h")
            .expiresAt(Instant.now().plus(1, ChronoUnit.DAYS))
            .revoked(true)
            .build();
    when(repository.findByTokenHash(anyString())).thenReturn(Optional.of(entity));

    service.revokeByPlainToken("anything");

    verify(repository, never()).save(any(RefreshTokenEntity.class));
  }

  @Test
  void revokeByPlainTokenIsSilentWhenTokenIsUnknown() {
    when(repository.findByTokenHash(anyString())).thenReturn(Optional.empty());

    service.revokeByPlainToken("anything");

    verify(repository, never()).save(any(RefreshTokenEntity.class));
  }

  private static String hashOf(String token) {
    try {
      java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
      byte[] h = md.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
      return java.util.Base64.getEncoder().encodeToString(h);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }
}
