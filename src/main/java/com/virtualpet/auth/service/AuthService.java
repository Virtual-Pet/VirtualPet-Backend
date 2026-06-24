package com.virtualpet.auth.service;

import com.virtualpet.auth.domain.CustomerEntity;
import com.virtualpet.auth.domain.EmployeeEntity;
import com.virtualpet.auth.domain.RiderEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.domain.enums.UserRole;
import com.virtualpet.auth.dto.AuthDTO.AuthTokensDTO;
import com.virtualpet.auth.dto.AuthDTO.ChangePasswordRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.LoginRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.RefreshResponseDTO;
import com.virtualpet.auth.dto.AuthDTO.RegisterCustomerRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.RegisterEmployeeRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.UpdateMeRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.UserDTO;
import com.virtualpet.auth.dto.AuthDTO.UserSummaryDTO;
import com.virtualpet.auth.repository.UserRepository;
import com.virtualpet.cart.service.CartService;
import com.virtualpet.common.config.VirtualPetProperties;
import com.virtualpet.common.exception.ApiException;
import com.virtualpet.common.security.JwtService;
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

  private static final String BEARER = "Bearer";

  private final UserRepository userRepository;
  private final CustomerProfileService customerProfileService;
  private final EmployeeProfileService employeeProfileService;
  private final RiderProfileService riderProfileService;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;
  private final RefreshTokenService refreshTokenService;
  private final VirtualPetProperties properties;
  private final CartService cartService;

  /* ---------- Registration ---------- */

  @Transactional
  public UserSummaryDTO registerCustomer(RegisterCustomerRequestDTO request) {
    UserEntity user = createIdentity(request.email(), request.password(), UserRole.ROLE_CUSTOMER);
    customerProfileService.createProfile(user.getId(), request.firstName(), request.lastName());
    return new UserSummaryDTO(user.getId(), user.getEmail(), user.getRole());
  }

  @Transactional
  public UserSummaryDTO registerEmployee(RegisterEmployeeRequestDTO request) {
    UserEntity user = createIdentity(request.email(), request.password(), UserRole.ROLE_EMPLOYEE);
    employeeProfileService.createProfile(user.getId(), request.firstName(), request.lastName());
    return new UserSummaryDTO(user.getId(), user.getEmail(), user.getRole());
  }

  /* ---------- Login / logout / refresh ---------- */

  @Transactional
  public AuthTokensDTO login(LoginRequestDTO request, String cartSessionId) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password()));

    UserEntity user =
        userRepository
            .findByEmailIgnoreCase(request.email())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

    log.info("Login successful: {}", user.getEmail());

    if (cartSessionId != null && !cartSessionId.isBlank()) {
      cartService.mergeAnonCartIntoUser(cartSessionId, user.getId());
    }

    String accessToken = jwtService.generate(user.getId(), user.getEmail(), user.getRole().name());
    String refreshToken = refreshTokenService.createRefreshToken(user);
    return new AuthTokensDTO(
        accessToken,
        refreshToken,
        BEARER,
        expiresInSeconds(),
        new UserSummaryDTO(user.getId(), user.getEmail(), user.getRole()));
  }

  public void logout(String refreshToken) {
    refreshTokenService.revokeByPlainToken(refreshToken);
  }

  @Transactional
  public RefreshResponseDTO refresh(String refreshToken) {
    UserEntity user = refreshTokenService.verifyExpiration(refreshToken);
    String accessToken = jwtService.generate(user.getId(), user.getEmail(), user.getRole().name());
    return new RefreshResponseDTO(accessToken, BEARER, expiresInSeconds());
  }

  /* ---------- /auth/me ---------- */

  @Transactional(readOnly = true)
  public UserDTO getMe(UUID userId) {
    UserEntity user = getById(userId);
    return assembleUser(user);
  }

  @Transactional
  public UserDTO updateMe(UUID userId, UpdateMeRequestDTO request) {
    UserEntity user = getById(userId);
    switch (user.getRole()) {
      case ROLE_CUSTOMER ->
          customerProfileService.updateName(userId, request.firstName(), request.lastName());
      case ROLE_EMPLOYEE, ROLE_ADMIN ->
          employeeProfileService.updateName(userId, request.firstName(), request.lastName());
      case ROLE_RIDER -> {
          riderProfileService.updateName(userId, request.firstName(), request.lastName());
      }
        }
    return assembleUser(user);
  }

  /* ---------- Passwords ---------- */

  @Transactional
  public void changePassword(UUID userId, ChangePasswordRequestDTO request) {
    UserEntity user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

    if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
      throw new ApiException(HttpStatus.UNAUTHORIZED, "Current password is incorrect");
    }
    if (passwordEncoder.matches(request.newPassword(), user.getPasswordHash())) {
      throw new ApiException(
          HttpStatus.UNPROCESSABLE_CONTENT, "New password must differ from the current one");
    }

    user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
    userRepository.save(user);
    refreshTokenService.revokeAllUserTokens(userId);
  }

  /* ---------- Internal ---------- */

  @Transactional
  protected UserEntity createIdentity(String email, String rawPassword, UserRole role) {
    if (userRepository.existsByEmailIgnoreCase(email)) {
      throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
    }
    UserEntity user =
        UserEntity.builder()
            .email(email.toLowerCase())
            .passwordHash(passwordEncoder.encode(rawPassword))
            .role(role)
            .active(true)
            .build();
    return userRepository.save(user);
  }

  public UserEntity getById(UUID id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
  }

  private UserDTO assembleUser(UserEntity user) {
    String firstName = null;
    String lastName = null;
    switch (user.getRole()) {
      case ROLE_CUSTOMER -> {
        CustomerEntity profile = customerProfileService.getByUserId(user.getId());
        firstName = profile.getName();
        lastName = profile.getLastname();
      }
      case ROLE_EMPLOYEE, ROLE_ADMIN -> {
        EmployeeEntity profile = employeeProfileService.getByUserId(user.getId());
        firstName = profile.getName();
        lastName = profile.getLastname();
      }
      case ROLE_RIDER -> {
        RiderEntity profile = riderProfileService.getByUserId(user.getId());
        firstName = profile.getName();
        lastName = profile.getLastname();
      }
    }
    return new UserDTO(user.getId(), user.getEmail(), firstName, lastName, user.getRole());
  }

  private long expiresInSeconds() {
    return properties.getJwt().getExpirationMs() / 1000L;
  }
}
