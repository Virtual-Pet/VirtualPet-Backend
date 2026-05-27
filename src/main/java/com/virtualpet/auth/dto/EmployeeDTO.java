package com.virtualpet.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public final class EmployeeDTO {

  public record RegisterEmployeeRequest(
      @NotBlank(message = "El email es obligatorio")
          @Email(message = "El formato del email no es válido")
          String email,
      @NotBlank(message = "El nombre es obligatorio") String name,
      @NotBlank(message = "El apellido es obligatorio") String lastname,
      @NotBlank(message = "El legajo es obligatorio") String legajo,
      @NotBlank(message = "El rol es obligatorio (EMPLOYEE o ADMIN)") String role,
      @NotNull(message = "El ID del depósito es obligatorio") Integer warehouseId,
      @NotBlank(message = "La contraseña temporal es obligatoria")
          @Size(min = 6, message = "La contraseña temporal debe tener al menos 6 caracteres")
          String temporaryPassword) {}

  public record EmployeeResponse(
      UUID id, // El ID del usuario (Identity)
      String email,
      String name,
      String lastname,
      String legajo,
      String role,
      Integer warehouseId,
      Boolean active, // Para saber si el empleado sigue trabajando o fue suspendido
      Instant createdAt) {}

  public record UpdateEmployeeRequest(
      @NotBlank(message = "El nombre no puede estar vacío") String name,
      @NotBlank(message = "El apellido no puede estar vacío") String lastname,
      @NotNull(message = "El ID del depósito es obligatorio") Integer warehouseId,
      @NotBlank(message = "El rol es obligatorio") String role,
      @NotNull(message = "El estado (activo/inactivo) es obligatorio") Boolean active) {}
}