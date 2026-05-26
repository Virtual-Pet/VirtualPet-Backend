package com.virtualpet.auth.service.registerOrchestrator;

import com.virtualpet.auth.domain.CustomerEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.domain.enums.UserRole;
import com.virtualpet.auth.dto.AuthDTO.AuthResponse;
import com.virtualpet.auth.dto.AuthDTO.LoginRequest;
import com.virtualpet.auth.dto.CustomerDTO.CustomerProfileResponse;
import com.virtualpet.auth.dto.CustomerDTO.RegisterCustomerRequest;
import com.virtualpet.auth.service.AuthService;
import com.virtualpet.auth.service.CustomerProfileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquesta las operaciones del cliente del marketplace que involucran
 * tanto credenciales (AuthService) como perfil (CustomerProfileService).
 * Regla: los controllers nunca llaman a AuthService o CustomerProfileService
 * directamente. Todo pasa por este orquestador.
 */
@Service
@RequiredArgsConstructor
public class CustomerOrchestrator {

    private final AuthService authService;
    private final CustomerProfileService customerProfileService;

    /**
     * Registra un cliente nuevo de forma atómica:
     * si falla la creación del perfil, hace rollback de las credenciales.
     */
    @Transactional
    public void register(RegisterCustomerRequest request) {
        UserEntity user = authService.createIdentity(
            request.email(),
            request.password(),
            UserRole.ROLE_CUSTOMER,
            false   // los clientes no tienen forcePasswordChange
        );
        customerProfileService.createProfile(user.getId(), request);
    }

    /**
     * Login del marketplace. Delega la autenticación y generación de tokens
     * completamente a AuthService — el orquestador no conoce JWT ni bcrypt.
     */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        return authService.login(request, UserRole.ROLE_CUSTOMER);
    }

    /**
     * Devuelve el perfil completo del cliente autenticado.
     * Combina datos de auth (email, rol) con datos de perfil (nombre, dni, teléfono).
     */
    @Transactional(readOnly = true)
    public CustomerProfileResponse getProfile(UUID userId) {
        UserEntity user = authService.getById(userId);
        CustomerEntity profile = customerProfileService.getByUserId(userId);

        return new CustomerProfileResponse(
            user.getId(),
            user.getEmail(),
            profile.getName(),
            profile.getLastname(),
            profile.getDni(),
            profile.getPhone(),
            user.getRole().name()
        );
    }
}
