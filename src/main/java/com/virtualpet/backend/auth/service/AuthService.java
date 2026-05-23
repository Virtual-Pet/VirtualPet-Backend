package com.virtualpet.backend.auth.service;

import com.virtualpet.backend.auth.domain.UserEntity;
import com.virtualpet.backend.auth.domain.enums.UserRole;
import com.virtualpet.backend.auth.dto.AuthDTO.*;
import com.virtualpet.backend.auth.repository.UserRepository;
import com.virtualpet.backend.shared.exception.ApiException;
import com.virtualpet.backend.shared.security.JwtService;

import java.util.List;
import java.util.UUID;
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

  @Transactional
  public UserEntity createIdentity(String email, String rawPassword, UserRole role, boolean forcePasswordChange) {
    if (userRepository.existsByEmailIgnoreCase(email)) {
      throw new ApiException(HttpStatus.CONFLICT, "El email ya está registrado");
    }

    UserEntity user = UserEntity.builder()
            .email(email.toLowerCase())
            .passwordHash(passwordEncoder.encode(rawPassword))
            .role(role)
            .active(true)
            .emailVerified(false)
            .forcePasswordChange(forcePasswordChange)
            .build();

    return userRepository.save(user);
  }

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));

    UserEntity user = userRepository.findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));

    log.info("Login successful: {}", user.getEmail());

    String jwt = jwtService.generate(user.getId(), user.getEmail(), user.getRole().name());
    String refreshToken = refreshTokenService.createRefreshToken(user);

    UserResponse userResponse = new UserResponse(
            user.getId().toString(),
            user.getEmail(),
            user.getRole().name(),
            user.getEmailVerified(),
            user.getForcePasswordChange()
    );

    return new AuthResponse(jwt, refreshToken, userResponse);
  }

  public UserEntity getById(UUID id) {
    return userRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
  }

  public List<UserEntity> getAllByIds(List<UUID> ids) {
    return userRepository.findAllById(ids);
  }

  @Transactional
  public void changeInternalPassword(UUID userId, ChangePasswordRequest request) {
    UserEntity user = userRepository.findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    //  Validar que la nueva no sea igual a la vieja
    if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "La nueva contraseña no puede ser igual a la anterior.");
    }

    // Comparamos la clave que ingresó con el Hash guardado en la DB
    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new ApiException(HttpStatus.BAD_REQUEST, "La contraseña actual es incorrecta.");
    }

    // Actualizamos
    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    user.setForcePasswordChange(false); // Por si venía del flujo de primer ingreso

    userRepository.save(user);
    refreshTokenService.revokeAllUserTokens(userId);
  }
}