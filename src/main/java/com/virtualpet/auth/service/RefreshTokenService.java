package com.virtualpet.auth.service;

import com.virtualpet.auth.domain.RefreshTokenEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.dto.AuthDTO.AuthResponse;
import com.virtualpet.auth.dto.AuthDTO.UserResponse;
import com.virtualpet.auth.repository.RefreshTokenRepository;
import com.virtualpet.common.exception.ApiException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

import com.virtualpet.common.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de gestión de refresh tokens.
 * Responsabilidades:
 * - Crear y almacenar refresh tokens hasheados
 * - Validar tokens (expiración, revocación)
 * - Revocar tokens (individual o en lote)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final int EXPIRATION_DAYS = 7;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    /**
    * Crea un nuevo refresh token para un usuario.
    * Revoca automáticamente todos los tokens anteriores (una sesión activa por usuario).
    */
    @Transactional
    public String createRefreshToken(UserEntity user) {
        refreshTokenRepository.revokeAllUserTokens(user.getId());

        String plainToken = generateUniqueToken();
        RefreshTokenEntity refreshToken = RefreshTokenEntity.builder()
            .user(user)
            .tokenHash(hashToken(plainToken))
            .expiresAt(Instant.now().plus(EXPIRATION_DAYS, ChronoUnit.DAYS))
            .revoked(false)
            .build();

        refreshTokenRepository.save(refreshToken);
        return plainToken;
    }

    /**
     * Refresca el JWT usando un refresh token válido.
     * Valida el token, genera nuevo JWT y construye respuesta.
     */
    @Transactional
    public AuthResponse refreshAccessToken(String plainRefreshToken) {
        // 1. Verify token and obtain token entity (ensures not expired/revoked)
        RefreshTokenEntity oldToken = findValidToken(plainRefreshToken);
        UserEntity user = oldToken.getUser();

        // 2. Revoke the used token immediately (defense-in-depth)
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);

        // 3. Create a new refresh token and revoke other active tokens
        String newRefreshPlain = createRefreshToken(user);

        // 4. Generate a new JWT including the forcePasswordChange flag
        String newJwt = jwtService.generate(user.getId(), user.getEmail(), user.getRole().name(), user.getForcePasswordChange());
        UserResponse userResponse = buildUserResponse(user);
        return new AuthResponse(newJwt, newRefreshPlain, userResponse);
    }

    /**
     * Valida un refresh token y retorna el usuario asociado.
     */
    @Transactional(readOnly = true)
    public UserEntity verifyAndGetUser(String plainToken) {
        RefreshTokenEntity tokenEntity = findValidToken(plainToken);
        return tokenEntity.getUser();
    }

    /**
     * Valida un refresh token y retorna el usuario asociado.
     * Método helper para compatibilidad con pruebas.
     */
    @Transactional(readOnly = true)
    public UserEntity verifyExpiration(String plainToken) {
        return verifyAndGetUser(plainToken);
    }

    /**
     * Revoca un refresh token específico en caso de estar activo.
     * No falla si el token ya fue revocado o es desconocido.
     */
    @Transactional
    public void revokeByPlainToken(String plainToken) {
        String tokenHash = hashToken(plainToken);
        refreshTokenRepository
            .findByTokenHash(tokenHash)
            .ifPresent(token -> {
                if (!token.getRevoked()) {
                    token.setRevoked(true);
                    refreshTokenRepository.save(token);
                }
            });
    }

    /**
     * Revoca todos los refresh tokens activos de un usuario.
     * Se usa cuando:
     * - El usuario cambia contraseña
     * - Se detecta actividad sospechosa
     * - El usuario hace logout
     *
     * @param userId ID del usuario
     */
    @Transactional
    public void revokeAllUserTokens(UUID userId) {
        log.info("[REFRESH-TOKEN] Revoking all tokens for user: {}", userId);
        refreshTokenRepository.revokeAllUserTokens(userId);
    }

    private RefreshTokenEntity findValidToken(String plainToken) {
        String tokenHash = hashToken(plainToken);
        RefreshTokenEntity tokenEntity = refreshTokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Refresh token inválido"));

        if (tokenEntity.getRevoked()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Token revocado. Inicie sesión nuevamente.");
        }

        if (tokenEntity.getExpiresAt().isBefore(Instant.now())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Sesión expirada. Inicie sesión nuevamente.");
        }

        return tokenEntity;
    }

    private String generateUniqueToken() {
        return UUID.randomUUID().toString().replace("-", "") +
            UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Hashea un token con SHA-256 para almacenamiento seguro.
     *
     * @param token Token en texto plano
     * @return Token hasheado en Base64
     * @throws RuntimeException si falla el algoritmo SHA-256
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            log.error("[REFRESH-TOKEN] SHA-256 algorithm not available", e);
            throw new RuntimeException("Error interno de seguridad", e);
        }
    }

    /**
     * Construye un UserResponse a partir de una entidad UserEntity.
     * Mapea únicamente datos públicos del usuario, sin exponer
     * información sensible como password hash.
     * Centraliza la lógica de mapeo para evitar duplicación
     * en diferentes endpoints que retornan UserResponse.
     *
     * @param user Entidad de usuario desde la BD
     * @return DTO con datos públicos del usuario
     */
    private UserResponse buildUserResponse(UserEntity user) {
        return new UserResponse(
            user.getId().toString(),
            user.getEmail(),
            user.getRole().name(),
            user.getEmailVerified(),
            user.getForcePasswordChange());
    }

}
