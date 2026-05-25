package com.virtualpet.auth.dto;

import com.virtualpet.auth.domain.enums.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.UUID;

/**
 * DTOs for the /auth/* endpoints. Records map 1:1 to the schemas defined in
 * docs/api/virtualpet-openapi.yaml.
 */
public final class AuthDTO {

  private AuthDTO() {}

  /* ---------- Requests ---------- */

  public record LoginRequest(@Email @NotBlank String email, @NotBlank String password) {}

  public record LogoutRequest(@NotBlank String refreshToken) {}

  public record RefreshRequest(@NotBlank String refreshToken) {}

  public record RegisterCustomerRequest(
      @Email @NotBlank String email,
      @NotBlank @Size(min = 8, max = 100) String password,
      @NotBlank String firstName,
      @NotBlank String lastName) {}

  public record RegisterEmployeeRequest(
      @Email @NotBlank String email,
      @NotBlank @Size(min = 8, max = 100) String password,
      @NotBlank String firstName,
      @NotBlank String lastName) {}

  public record UpdateMeRequest(String firstName, String lastName) {}

  public record ForgotPasswordRequest(@Email @NotBlank String email) {}

  public record ResetPasswordRequest(
      @NotBlank String token, @NotBlank @Size(min = 8, max = 100) String newPassword) {}

  public record ChangePasswordRequest(
      @NotBlank String currentPassword, @NotBlank @Size(min = 8, max = 100) String newPassword) {}

  /* ---------- Responses ---------- */

  public record AuthTokens(
      String accessToken,
      String refreshToken,
      String tokenType,
      long expiresIn,
      UserSummary user) {}

  public record RefreshResponse(String accessToken, String tokenType, long expiresIn) {}

  public record UserSummary(UUID id, String email, UserRole role) {}

  public record User(UUID id, String email, String firstName, String lastName, UserRole role) {}
}
