package com.virtualpet.auth.service;

import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.domain.enums.UserRole;
import com.virtualpet.auth.dto.AuthDTO.AuthResponse;
import com.virtualpet.auth.dto.AuthDTO.ChangePasswordRequest;
import com.virtualpet.auth.dto.AuthDTO.LoginRequest;
import com.virtualpet.auth.dto.AuthDTO.UserResponse;
import com.virtualpet.auth.repository.UserRepository;
import com.virtualpet.common.exception.ApiException;
import com.virtualpet.common.security.JwtService;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    /**
     * Crea las credenciales de un usuario nuevo.
     * Verifica unicidad de email antes de persistir.
     */
    @Transactional
    public UserEntity createIdentity(
        String email, String rawPassword, UserRole role, boolean forcePasswordChange) {

        String normalizedEmail = email.toLowerCase().trim();

        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "El email ya está registrado.");
        }

        UserEntity user = UserEntity.builder()
            .email(normalizedEmail)
            .passwordHash(passwordEncoder.encode(rawPassword))
            .role(role)
            .active(true)
            .emailVerified(false)
            .forcePasswordChange(forcePasswordChange)
            .build();

        return userRepository.save(user);
    }

    /**
     * Autentica al usuario y emite el par JWT + refresh token.
     *
     * @param request      credenciales ingresadas
     * @param allowedRoles roles que tienen acceso a este punto de entrada.
     *                     Impide que un cliente se loguee en el backoffice y viceversa.
     */
    public AuthResponse login(LoginRequest request, UserRole... allowedRoles) {
        String normalizedEmail = request.email().toLowerCase().trim();
        // 1. Spring Security valida email + password contra la BD
        log.debug("usuario: {}, {}", normalizedEmail,request.password());
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(normalizedEmail, request.password()));
        log.debug("Autenticación exitosa para email: {}", normalizedEmail);
        UserEntity user = userRepository
            .findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas."));
        log.debug("Usuario encontrado: {}", user.getEmail());
        // 2. Verificar que el rol del usuario corresponde al punto de entrada
        Set<UserRole> allowed = Arrays.stream(allowedRoles).collect(Collectors.toSet());
        if (!allowed.contains(user.getRole())) {
            log.warn("Login rechazado — rol {} no permitido en este endpoint: {}", user.getRole(), request.email());
            // Respuesta genérica para no revelar si el email existe en el otro portal
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas.");
        }
        log.debug("Rol permitido: {}. Continuando con login para {}", user.getRole(), user.getEmail());

        // 3. Verificar que la cuenta está activa
        if (!user.getActive()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "La cuenta está desactivada. Contactá al administrador.");
        }

        log.info("Login exitoso: {} ({})", user.getEmail(), user.getRole());

        String jwt = jwtService.generate(user.getId(), user.getEmail(), user.getRole().name(), user.getForcePasswordChange());
        String refreshToken = refreshTokenService.createRefreshToken(user);
        UserResponse userResponse = buildUserResponse(user);

        return new AuthResponse(jwt, refreshToken, userResponse);
    }

    public UserEntity getById(UUID id) {
        return userRepository
            .findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));
    }

    public List<UserEntity> getAllByIds(List<UUID> ids) {
        return userRepository.findAllById(ids);
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     * Orden de validaciones (importa para seguridad):
     * 1. Verificar contraseña actual → si falla, cortar aquí. No revelar más info.
     * 2. Verificar que la nueva no sea igual a la actual.
     * 3. Aplicar el cambio e invalidar todas las sesiones activas.
     */
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        UserEntity user = userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        // 1. Primero: verificar la contraseña actual (siempre primero)
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta.");
        }

        // 2. Después: validar que la nueva sea diferente a la actual
        if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "La nueva contraseña no puede ser igual a la anterior.");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setForcePasswordChange(false);
        userRepository.save(user);

        // 3. Invalidar todas las sesiones activas — el usuario debe volver a loguearse
        refreshTokenService.revokeAllUserTokens(userId);
    }

    /**
     * Soft delete: desactiva un usuario sin eliminarlo.
     * Preserva el historial de pedidos y referencias en otros schemas.
     */
    @Transactional
    public void deactivate(UUID userId) {
        UserEntity user = userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado."));

        user.setActive(false);
        userRepository.save(user);

        // Revocar sesiones activas del empleado desactivado
        refreshTokenService.revokeAllUserTokens(userId);
        log.info("Usuario desactivado: {}", user.getEmail());
    }

    private UserResponse buildUserResponse(UserEntity user) {
        return new UserResponse(
            user.getId().toString(),
            user.getEmail(),
            user.getRole().name(),
            user.getEmailVerified(),
            user.getForcePasswordChange());
    }
}

