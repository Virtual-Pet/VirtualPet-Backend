package com.virtualpet.auth.service.registerOrchestrator;

import com.virtualpet.auth.domain.CustomerEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.domain.enums.UserRole;
import com.virtualpet.auth.dto.CustomerDTO.CustomerProfileResponse;
import com.virtualpet.auth.dto.CustomerDTO.RegisterCustomerRequest;
import com.virtualpet.auth.service.AuthService;
import com.virtualpet.auth.service.CustomerProfileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerOrchestrator {

  private final AuthService authService;
  private final CustomerProfileService customerProfileService;

  @Transactional
  public void executeRegister(RegisterCustomerRequest request) {
    // 1. Crear Credenciales (Fuerza de cambio de clave = False, es un cliente)
    UserEntity user =
        authService.createIdentity(
            request.email(), request.password(), UserRole.ROLE_CUSTOMER, false);

    // 2. Crear Perfil de Cliente
    customerProfileService.createProfile(user.getId(), request);
  }

  @Transactional(readOnly = true)
  public CustomerProfileResponse executeMe(UUID userId) {
    UserEntity user = authService.getById(userId);
    CustomerEntity profile = customerProfileService.getByUserId(userId);

    return new CustomerProfileResponse(
        user.getId(),
        user.getEmail(),
        profile.getName(),
        profile.getLastname(),
        profile.getDni(),
        profile.getPhone(),
        user.getRole().name());
  }
}
