package com.virtualpet.backend.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

  private AuthDtos() {}

  public record RegisterCustomerRequest(
      @Email @NotBlank String email,
      @NotBlank @Size(min = 8, max = 100) String password,
      @NotBlank String name,
      @NotBlank String lastname,
      @NotBlank @Pattern(regexp = "^\\d{7,8}$", message = "DNI inválido") String dni,
      String phone) {}

  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

  public record ForgotPasswordRequest(@Email @NotBlank String email) {}

  public record ResetPasswordRequest(
      @NotBlank String token, @NotBlank @Size(min = 6, max = 100) String password) {}

  public record MessageResponse(String message) {}

  public record RefreshTokenRequest(@NotBlank String refreshToken) {}

  public record AuthResponse(String token, UserResponse user) {}

  public record UserResponse(
      String id, String email, String role, String name, String lastname, boolean emailVerified) {}
}
