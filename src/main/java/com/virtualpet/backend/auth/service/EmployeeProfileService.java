package com.virtualpet.backend.auth.service;

import com.virtualpet.backend.auth.domain.EmployeeEntity;
import com.virtualpet.backend.auth.dto.EmployeeDTO.RegisterEmployeeRequest;
import com.virtualpet.backend.auth.repository.EmployeeRepository;
import com.virtualpet.backend.shared.exception.ApiException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmployeeProfileService {

  private final EmployeeRepository employeeRepository;

  @Transactional
  public void createProfile(UUID userId, RegisterEmployeeRequest request) {
    EmployeeEntity profile =
        EmployeeEntity.builder()
            .userId(userId)
            .name(request.name())
            .lastname(request.lastname())
            .legajo(request.legajo())
            .warehouseId(request.warehouseId())
            .build();

    employeeRepository.save(profile);
  }

  @Transactional(readOnly = true)
  public EmployeeEntity getByUserId(UUID userId) {
    return employeeRepository
        .findById(userId)
        .orElseThrow(
            () ->
                new ApiException(
                    HttpStatus.NOT_FOUND,
                    "Perfil de empleado no encontrado para el identificador proporcionado"));
  }

  @Transactional(readOnly = true)
  public List<EmployeeEntity> getAllProfiles() {
    return employeeRepository.findAll();
  }
}
