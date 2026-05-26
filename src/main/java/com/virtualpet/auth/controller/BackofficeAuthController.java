package com.virtualpet.auth.controller;

import com.virtualpet.auth.dto.AuthDTO.AuthResponse;
import com.virtualpet.auth.dto.AuthDTO.LoginRequest;
import com.virtualpet.auth.dto.EmployeeDTO.EmployeeResponse;
import com.virtualpet.auth.service.registerOrchestrator.EmployeeOrchestrator;
import com.virtualpet.common.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Auth del backoffice. Separado del marketplace intencionalmente:
 * - Rutas distintas → Security puede configurar filtros diferentes por prefijo.
 * - Los clientes del marketplace no pueden llegar a estos endpoints.
 * - Permite en el futuro agregar 2FA, IP allowlist, etc. solo para el backoffice.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/backoffice/auth")
@RequiredArgsConstructor
public class BackofficeAuthController {

    private final EmployeeOrchestrator orchestrator;

    /**
     * Login para empleados y administradores.
     * Si el usuario tiene forcePasswordChange = true, la respuesta lo indica
     * y el cliente del backoffice debe redirigir al flujo de cambio de clave.
     * No requiere autenticación previa.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(orchestrator.login(request));
    }

    /**
     * Devuelve el perfil del empleado o admin autenticado.
     * Accesible por ambos roles — cada uno ve solo su propio perfil.
     */
    @GetMapping("/me")
    public ResponseEntity<EmployeeResponse> getMyProfile(
        @AuthenticationPrincipal UserPrincipal currentUser) {
        log.debug("Entro al endpoint");
        return ResponseEntity.ok(orchestrator.getProfile(currentUser.getId()));
    }
}
