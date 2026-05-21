package com.virtualpet.backend.auth.api;

import com.virtualpet.backend.auth.dto.AuthDtos.*;
import com.virtualpet.backend.auth.service.AuthService;
import com.virtualpet.backend.auth.service.PasswordResetService;
import com.virtualpet.backend.auth.service.RefreshTokenService;
import com.virtualpet.backend.shared.security.JwtService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final PasswordResetService passwordResetService;
  private final RefreshTokenService refreshTokenService;
  private final JwtService jwtService;

  @PostMapping("/register")
  public ResponseEntity<AuthResponse> register(
      @Valid @RequestBody RegisterCustomerRequest request) {
    return ResponseEntity.ok(authService.registerCustomer(request));
  }

  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @GetMapping("/me")
  public UserResponse me() {
    return authService.me();
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<MessageResponse> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequest request) {
    return ResponseEntity.ok(passwordResetService.requestReset(request));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<MessageResponse> resetPassword(
      @Valid @RequestBody ResetPasswordRequest request) {
    return ResponseEntity.ok(passwordResetService.resetPassword(request));
  }

  // Endpoint para obtener un nuevo JWT cuando se vence el actual
  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(
      @Valid @RequestBody RefreshTokenRequest request) {
    // Verificamos que el Refresh Token sea válido
    var user = refreshTokenService.verifyExpiration(request.refreshToken());

    // Generamos un nuevo JWT
    String newJwt = jwtService.generate(user.getId(), user.getEmail(), user.getRole().name());

    // Retornamos el nuevo JWT (manteniendo el mismo Refresh Token)
    return ResponseEntity.ok(new AuthResponse(newJwt, null));
  }
}
