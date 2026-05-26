package com.virtualpet.auth.service;

import com.virtualpet.auth.domain.EmployeeEntity;
import com.virtualpet.auth.dto.EmployeeDTO.RegisterEmployeeRequest;
import com.virtualpet.auth.dto.EmployeeDTO.UpdateEmployeeRequest;
import com.virtualpet.auth.repository.EmployeeRepository;
import com.virtualpet.common.exception.ApiException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestiona el perfil de datos del empleado (tabla auth.employees).
 * Solo conoce EmployeeEntity — no sabe nada de UserEntity ni JWT.
 */
@Service
@RequiredArgsConstructor
public class EmployeeProfileService {

    private final EmployeeRepository employeeRepository;

    @Transactional
    public void createProfile(UUID userId, RegisterEmployeeRequest request) {
        EmployeeEntity profile = EmployeeEntity.builder()
            .userId(userId)
            .name(request.name().trim())
            .lastname(request.lastname().trim())
            .legajo(request.legajo())
            .warehouseId(request.warehouseId())
            .build();

        employeeRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public EmployeeEntity getByUserId(UUID userId) {
        return employeeRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "Perfil de empleado no encontrado."
            ));
    }

    @Transactional(readOnly = true)
    public List<EmployeeEntity> getAllProfiles() {
        return employeeRepository.findAll();
    }

    /**
     * Actualiza los datos de perfil del empleado.
     * Solo modifica campos de la tabla employees — nunca toca users.
     */
    @Transactional
    public EmployeeEntity update(UUID userId, UpdateEmployeeRequest request) {
        EmployeeEntity profile = getByUserId(userId);

        profile.setName(request.name().trim());
        profile.setLastname(request.lastname().trim());
        profile.setWarehouseId(request.warehouseId());

        return employeeRepository.save(profile);
    }
}
