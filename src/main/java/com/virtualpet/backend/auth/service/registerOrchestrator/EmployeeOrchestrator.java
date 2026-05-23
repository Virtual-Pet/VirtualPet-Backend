package com.virtualpet.backend.auth.service.registerOrchestrator;

import com.virtualpet.backend.auth.domain.EmployeeEntity;
import com.virtualpet.backend.auth.domain.UserEntity;
import com.virtualpet.backend.auth.domain.enums.UserRole;
import com.virtualpet.backend.auth.dto.EmployeeDTO.EmployeeResponse;
import com.virtualpet.backend.auth.dto.EmployeeDTO.RegisterEmployeeRequest;
import com.virtualpet.backend.auth.service.AuthService;
import com.virtualpet.backend.auth.service.EmployeeProfileService;
import com.virtualpet.backend.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

    @Transactional(readOnly = true)
    public List<EmployeeResponse> executeGetAll() {
        // 1. Buscamos todos los perfiles físicos de los empleados
        List<EmployeeEntity> profiles = employeeProfileService.getAllProfiles();

        // 2. Extraemos la lista de UUIDs de esos usuarios
        List<UUID> userIds = profiles.stream()
                .map(EmployeeEntity::getUserId)
                .toList();

        // 3. Traemos todas las identidades correspondientes desde Auth de un solo golpe
        List<UserEntity> users = authService.getAllByIds(userIds);

        // 4. Cruzamos la información en memoria para armar los DTOs de respuesta
        return profiles.stream().map(profile -> {
            UserEntity user = users.stream()
                    .filter(u -> u.getId().equals(profile.getUserId()))
                    .findFirst()
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                            "Error de integridad: No se encontró la identidad para el empleado " + profile.getLegajo()));

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
        }).toList();
    }
}
