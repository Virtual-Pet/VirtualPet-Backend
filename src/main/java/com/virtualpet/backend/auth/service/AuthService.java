package com.virtualpet.backend.auth.service;

import com.virtualpet.backend.auth.domain.CustomerEntity;
import com.virtualpet.backend.auth.domain.UserEntity;
import com.virtualpet.backend.auth.domain.UserRole;
import com.virtualpet.backend.auth.dto.AuthDtos.*;
import com.virtualpet.backend.auth.repository.UserRepository;
import com.virtualpet.backend.shared.exception.ApiException;
import com.virtualpet.backend.shared.security.JwtService;
import com.virtualpet.backend.shared.security.SecurityUtils;
import com.virtualpet.backend.shared.security.UserPrincipal;
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

  @Transactional
  public AuthResponse registerCustomer(RegisterCustomerRequest request) {
    if (userRepository.existsByEmailIgnoreCase(request.email())) {
      throw new ApiException(HttpStatus.CONFLICT, "El email ya está registrado");
    }

    // 1. Armamos las credenciales (Tabla: users)
    UserEntity user =
        UserEntity.builder()
            .email(request.email().toLowerCase())
            .passwordHash(passwordEncoder.encode(request.password()))
            .role(UserRole.ROLE_CUSTOMER)
            .active(true)
            .emailVerified(false)
            .build();

    // 2. Armamos el perfil físico (Tabla: customers)
    CustomerEntity customerProfile =
        CustomerEntity.builder()
            .name(request.name())
            .lastname(request.lastname())
            .dni(request.dni())
            .phone(request.phone())
            .build();

    // Vinculamos bidireccionalmente. JPA asignará el mismo ID automáticamente a customerProfile.
    user.setCustomerProfile(customerProfile);

    // Guardamos (impacta en users y customers por el Cascade)
    userRepository.save(user);
    log.info("Customer registered: {}", request.email().toLowerCase());
    return buildAuthResponse(user);
  }

  public AuthResponse login(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));
    UserEntity user =
        userRepository
            .findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Credenciales inválidas"));
    log.info("Login successful: {}", user.getEmail());
    return buildAuthResponse(user);
  }

  public UserResponse me() {
    UserPrincipal principal = SecurityUtils.currentUser();
    UserEntity user =
        userRepository
            .findById(principal.getId())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    return toUserResponse(user);
  }

  public UserEntity getById(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
  }

  private AuthResponse buildAuthResponse(UserEntity user) {
    String token = jwtService.generate(user.getId(), user.getEmail(), user.getRole().name());
    return new AuthResponse(token, toUserResponse(user));
  }

  private UserResponse toUserResponse(UserEntity user) {
    // Extraemos el nombre y apellido verificando qué perfil tiene adjunto
    String name = "";
    String lastname = "";

    if (user.getRole() == UserRole.ROLE_CUSTOMER && user.getCustomerProfile() != null) {
      name = user.getCustomerProfile().getName();
      lastname = user.getCustomerProfile().getLastname();
    } else if (user.getRole() == UserRole.ROLE_EMPLOYEE && user.getEmployeeProfile() != null) {
      name = user.getEmployeeProfile().getName();
      lastname = user.getEmployeeProfile().getLastname();
    }

    return new UserResponse(
        user.getId().toString(),
        user.getEmail(),
        user.getRole().name(),
        name,
        lastname,
        user.getEmailVerified());
  }
}
