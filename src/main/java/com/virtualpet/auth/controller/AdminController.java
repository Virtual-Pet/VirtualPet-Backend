package com.virtualpet.auth.controller;

import com.virtualpet.auth.dto.AuthDTO.MessageResponse;
import com.virtualpet.auth.dto.EmployeeDTO.EmployeeResponse;
import com.virtualpet.auth.dto.EmployeeDTO.RegisterEmployeeRequest;
import com.virtualpet.auth.dto.EmployeeDTO.UpdateEmployeeRequest;
import com.virtualpet.auth.service.registerOrchestrator.EmployeeOrchestrator;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestión de empleados. Solo accesible por ADMIN.
 * El admin también usa BackofficeAuthController para login y /me.
 * Rutas:
 *   POST   /api/v1/backoffice/employees          → dar de alta empleado
 *   GET    /api/v1/backoffice/employees           → listar todos
 *   GET    /api/v1/backoffice/employees/{id}      → ver uno específico
 *   PUT    /api/v1/backoffice/employees/{id}      → actualizar datos
 *   DELETE /api/v1/backoffice/employees/{id}      → soft delete (active = false)
 */
@RestController
@RequestMapping("/api/v1/backoffice/employees")
@PreAuthorize("hasRole('ADMIN')")   // aplica a todos los métodos de este controller
@RequiredArgsConstructor
public class AdminController {

    private final EmployeeOrchestrator orchestrator;

    /**
     * Da de alta un empleado.
     * Crea las credenciales con forcePasswordChange = true:
     * el empleado deberá cambiar su clave en el primer ingreso.
     * Retorna 201 Created.
     */
    @PostMapping
    public ResponseEntity<MessageResponse> register(
        @Valid @RequestBody RegisterEmployeeRequest request) {
        orchestrator.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new MessageResponse("Empleado dado de alta. Deberá cambiar su contraseña al primer ingreso."));
    }

    /** Lista todos los empleados activos e inactivos. */
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> listAll() {
        return ResponseEntity.ok(orchestrator.listAll());
    }

    /** Devuelve el perfil completo de un empleado específico por su UUID. */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(orchestrator.getProfile(id));
    }

    /**
     * Actualiza datos del empleado: nombre, apellido, legajo, depósito asignado.
     * No permite cambiar el email ni la contraseña desde aquí.
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> update(
        @PathVariable UUID id,
        @Valid @RequestBody UpdateEmployeeRequest request) {
        return ResponseEntity.ok(orchestrator.update(id, request));
    }

    /**
     * Soft delete: marca al empleado como inactive (active = false).
     * No elimina la fila de la BD para preservar el historial de pedidos asociados.
     * Retorna 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        orchestrator.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}