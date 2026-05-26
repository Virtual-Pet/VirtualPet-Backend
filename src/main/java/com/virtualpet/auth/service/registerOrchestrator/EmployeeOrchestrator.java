package com.virtualpet.auth.service.registerOrchestrator;

import com.virtualpet.auth.domain.EmployeeEntity;
import com.virtualpet.auth.domain.UserEntity;
import com.virtualpet.auth.domain.enums.UserRole;
import com.virtualpet.auth.dto.AuthDTO.AuthResponse;
import com.virtualpet.auth.dto.AuthDTO.LoginRequest;
import com.virtualpet.auth.dto.EmployeeDTO.EmployeeResponse;
import com.virtualpet.auth.dto.EmployeeDTO.RegisterEmployeeRequest;
import com.virtualpet.auth.dto.EmployeeDTO.UpdateEmployeeRequest;
import com.virtualpet.auth.service.AuthService;
import com.virtualpet.auth.service.EmployeeProfileService;
import com.virtualpet.common.exception.ApiException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquesta las operaciones de empleados y admin del backoffice.
 * Cubre: login, perfil propio, alta, listado, actualización y baja lógica.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeOrchestrator {

    private final AuthService authService;
    private final EmployeeProfileService employeeProfileService;

    /**
     * Da de alta un empleado de forma atómica.
     * Siempre con forcePasswordChange = true: el empleado debe cambiar
     * su contraseña temporal en el primer ingreso al backoffice.
     * El rol se resuelve desde el enum — no se manipulan strings aquí.
     */
    @Transactional
    public void register(RegisterEmployeeRequest request) {
        UserRole role = resolveRole(request.role());

        UserEntity user = authService.createIdentity(
            request.email(),
            request.temporaryPassword(),
            role,
            true    // forcePasswordChange = true para empleados nuevos
        );
        employeeProfileService.createProfile(user.getId(), request);
    }

    /**
     * Login del backoffice. Solo permite roles EMPLOYEE y ADMIN.
     * Un cliente del marketplace con ROLE_CUSTOMER no puede loguearse aquí.
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        return authService.login(request, UserRole.ROLE_EMPLOYEE, UserRole.ROLE_ADMIN);
    }

    /**
     * Perfil del empleado autenticado (o de cualquier empleado por id, para el admin).
     * Mismo método para /me (empleado ve el suyo) y /{id} (admin ve cualquiera).
     */
    @Transactional(readOnly = true)
    public EmployeeResponse getProfile(UUID userId) {
        UserEntity user = authService.getById(userId);
        EmployeeEntity profile = employeeProfileService.getByUserId(userId);
        log.debug("se obtuvo perffil");
        return toResponse(user, profile);
    }

    /**
     * Lista todos los empleados.
     * Trae todos los perfiles de una vez y cruza con sus identidades en memoria
     * usando un Map para evitar N+1 queries (un findById por empleado).
     */
    @Transactional(readOnly = true)
    public List<EmployeeResponse> listAll() {
        List<EmployeeEntity> profiles = employeeProfileService.getAllProfiles();
        List<UUID> userIds = profiles.stream().map(EmployeeEntity::getUserId).toList();

        // Un solo findAllById en lugar de N findById individuales
        Map<UUID, UserEntity> usersById = authService.getAllByIds(userIds).stream()
            .collect(Collectors.toMap(UserEntity::getId, Function.identity()));

        return profiles.stream()
            .map(profile -> {
                UserEntity user = usersById.get(profile.getUserId());
                if (user == null) {
                    throw new ApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Inconsistencia de datos: sin identidad para el empleado con legajo " + profile.getLegajo()
                    );
                }
                return toResponse(user, profile);
            })
            .toList();
    }

    /**
     * Actualiza los datos de perfil del empleado.
     * No permite cambiar email ni contraseña desde aquí.
     */
    @Transactional
    public EmployeeResponse update(UUID userId, UpdateEmployeeRequest request) {
        EmployeeEntity profile = employeeProfileService.update(userId, request);
        UserEntity user = authService.getById(userId);
        return toResponse(user, profile);
    }

    /**
     * Soft delete: desactiva al empleado (active = false en users).
     * No elimina ninguna fila para preservar el historial.
     */
    @Transactional
    public void deactivate(UUID userId) {
        authService.deactivate(userId);
    }

    // ── Helpers privados ───────────────────────────────────────────────────────

    /**
     * Resuelve el rol desde el string del request al enum de dominio.
     * Acepta "EMPLOYEE", "ADMIN", "ROLE_EMPLOYEE", "ROLE_ADMIN" (case-insensitive).
     * La normalización vive acá, no dispersa en el código.
     */
    private UserRole resolveRole(String rawRole) {
        String normalized = rawRole.toUpperCase().trim();
        String withPrefix = normalized.startsWith("ROLE_") ? normalized : "ROLE_" + normalized;
        try {
            return UserRole.valueOf(withPrefix);
        } catch (IllegalArgumentException e) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                "Rol inválido: '" + rawRole + "'. Valores permitidos: EMPLOYEE, ADMIN");
        }
    }

    private EmployeeResponse toResponse(UserEntity user, EmployeeEntity profile) {
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
