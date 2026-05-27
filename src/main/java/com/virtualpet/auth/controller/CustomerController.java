package com.virtualpet.auth.controller;

import com.virtualpet.auth.dto.AuthDTO.AuthResponse;
import com.virtualpet.auth.dto.AuthDTO.LoginRequest;
import com.virtualpet.auth.dto.AuthDTO.MessageResponse;
import com.virtualpet.auth.dto.CustomerDTO.CustomerProfileResponse;
import com.virtualpet.auth.dto.CustomerDTO.RegisterCustomerRequest;
import com.virtualpet.auth.service.registerOrchestrator.CustomerOrchestrator;
import com.virtualpet.common.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints del Marketplace para clientes.
 * Accesible sin autenticación: /register y /login.
 * Requiere rol CUSTOMER: /me.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerOrchestrator orchestrator;

    /**
     * Registra un nuevo cliente en el marketplace.
     * No requiere autenticación.
     * Retorna 201 Created.
     */
    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
        @Valid @RequestBody RegisterCustomerRequest request) {
        orchestrator.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new MessageResponse("Cuenta creada con éxito. Revisá tu email para confirmarla."));
    }

    /**
     * Login exclusivo del marketplace para clientes.
     * No aplica al backoffice — los empleados usan /api/v1/backoffice/auth/login.
     * No requiere autenticación previa.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(orchestrator.login(request));
    }

    /**
     * Devuelve el perfil del cliente autenticado.
     * Spring inyecta el UUID del JWT en currentUser automáticamente.
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerProfileResponse> getMyProfile(
        @AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(orchestrator.getProfile(currentUser.getId()));
    }
}