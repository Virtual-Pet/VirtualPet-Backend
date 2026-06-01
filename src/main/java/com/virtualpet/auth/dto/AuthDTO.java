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

  public record LoginRequestDTO(@Email @NotBlank String email, @NotBlank String password) {}

  public record LogoutRequestDTO(@NotBlank String refreshToken) {}

  public record RefreshRequestDTO(@NotBlank String refreshToken) {}

  public record RegisterCustomerRequestDTO(
      @Email @NotBlank String email,
      @NotBlank @Size(min = 8, max = 100) String password,
      @NotBlank String firstName,
      @NotBlank String lastName) {}

  public record RegisterEmployeeRequestDTO(
      @Email @NotBlank String email,
      @NotBlank @Size(min = 8, max = 100) String password,
      @NotBlank String firstName,
      @NotBlank String lastName) {}

  public record UpdateMeRequestDTO(String firstName, String lastName) {}

  public record ChangePasswordRequestDTO(
      @NotBlank String currentPassword, @NotBlank @Size(min = 8, max = 100) String newPassword) {}

  /* ---------- Responses ---------- */

  public record AuthTokensDTO(
      String accessToken,
      String refreshToken,
      String tokenType,
      long expiresIn,
      UserSummaryDTO user) {}

  public record RefreshResponseDTO(String accessToken, String tokenType, long expiresIn) {}

  public record UserSummaryDTO(UUID id, String email, UserRole role) {}

  public record UserDTO(UUID id, String email, String firstName, String lastName, UserRole role) {}
}
