package com.virtualpet.backend.auth.service.registerOrchestrator;

import com.virtualpet.backend.auth.domain.EmployeeEntity;
import com.virtualpet.backend.auth.domain.UserEntity;
import com.virtualpet.backend.auth.domain.enums.UserRole;
import com.virtualpet.backend.auth.dto.EmployeeDTO.EmployeeResponse;
import com.virtualpet.backend.auth.dto.EmployeeDTO.RegisterEmployeeRequest;
import com.virtualpet.backend.auth.service.AuthService;
import com.virtualpet.backend.auth.service.EmployeeProfileService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EmployeeOrchestrator {

    private final AuthService authService;
    private final EmployeeProfileService employeeProfileService;

    @Transactional
    public void executeRegister(RegisterEmployeeRequest request) {

        String formatRole = request.role().toUpperCase().startsWith("ROLE_")
                ? request.role().toUpperCase()
                : "ROLE_" + request.role().toUpperCase();

        // 1. Crear Credenciales (Fuerza de cambio de clave = TRUE, es un empleado nuevo)
        UserEntity user = authService.createIdentity(
                request.email(),
                request.temporaryPassword(),
                UserRole.valueOf(formatRole),
                true
        );

        // 2. Crear Perfil de Empleado
        employeeProfileService.createProfile(user.getId(), request);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse executeMe(UUID userId) {
        UserEntity user = authService.getById(userId);
        EmployeeEntity profile = employeeProfileService.getByUserId(userId);

        return new EmployeeResponse(
                user.getId(),
                user.getEmail(),
                profile.getName(),
                profile.getLastname(),
                profile.getLegajo(),
                user.getRole().name(),
                profile.getWarehouseId(),
                user.getActive(),
                profile.getCreatedAt()
        );
    }
}
