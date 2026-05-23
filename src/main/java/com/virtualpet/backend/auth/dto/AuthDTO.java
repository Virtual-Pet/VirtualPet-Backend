package com.virtualpet.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDTO{

  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

  public record ForgotPasswordRequest(@Email @NotBlank String email) {}

  public record ResetPasswordRequest(
          @NotBlank String token, @NotBlank @Size(min = 6, max = 100) String password) {}

  public record ChangePasswordRequest(
          @NotBlank @Size(min = 6) String newPassword) {}

  public record MessageResponse(String message) {}

  public record RefreshTokenRequest(@NotBlank String refreshToken) {}

  public record AuthResponse(String token, String refreshToken, UserResponse user) {}

  public record UserResponse(
          String id, String email, String role, boolean emailVerified, boolean forcePasswordChange) {}
}
