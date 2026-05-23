package com.virtualpet.backend.auth.api;

import com.virtualpet.backend.auth.dto.AuthDTO.*;
import com.virtualpet.backend.auth.service.AuthService;
import com.virtualpet.backend.auth.service.PasswordResetService;
import com.virtualpet.backend.auth.service.RefreshTokenService;
import com.virtualpet.backend.shared.security.JwtService;
import com.virtualpet.backend.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

  // --- FLUJO COMPARTIDO (Clientes y Empleados) ---
  @PostMapping("/login")
  public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    var user = refreshTokenService.verifyExpiration(request.refreshToken());
    var userResponse = new UserResponse(
            user.getId().toString(),
            user.getEmail(),
            user.getRole().toString(),
            user.getEmailVerified(),
            user.getForcePasswordChange()
    );
    String newJwt = jwtService.generate(user.getId(), user.getEmail(), user.getRole().name());
    return ResponseEntity.ok(new AuthResponse(newJwt, null,userResponse));
  }

  // --- SEGURIDAD Y CLAVES ---
  @PostMapping("/forgot-password")
  public ResponseEntity<MessageResponse> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    return ResponseEntity.ok(passwordResetService.requestReset(request));
  }

  @PostMapping("/reset-password")
  public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    return ResponseEntity.ok(passwordResetService.resetPassword(request));
  }

  @PostMapping("/change-password")
  public ResponseEntity<MessageResponse> changePassword(
          @Valid @RequestBody ChangePasswordRequest request,
          @AuthenticationPrincipal UserPrincipal currentUser) {

    authService.changeInternalPassword(currentUser.getId(), request.newPassword());
    return ResponseEntity.ok(new MessageResponse("Contraseña actualizada con éxito."));
  }
}
