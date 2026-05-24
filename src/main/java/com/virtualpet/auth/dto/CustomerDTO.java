package com.virtualpet.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public class CustomerDTO {
  public record RegisterCustomerRequest(
      @Email @NotBlank String email,
      @NotBlank @Size(min = 8, max = 100) String password,
      @NotBlank String name,
      @NotBlank String lastname,
      @NotBlank @Pattern(regexp = "^\\d{7,8}$", message = "DNI inválido") String dni,
      String phone) {}

  public record CustomerProfileResponse(
      UUID id, String email, String name, String lastname, String dni, String phone, String role) {}
}
