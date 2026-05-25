package com.virtualpet.auth.service;

import com.virtualpet.auth.domain.EmployeeEntity;
import com.virtualpet.auth.repository.EmployeeRepository;
import com.virtualpet.common.exception.ApiException;
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
  public EmployeeEntity createProfile(UUID userId, String firstName, String lastName) {
    EmployeeEntity profile =
        EmployeeEntity.builder().userId(userId).name(firstName).lastname(lastName).build();
    return employeeRepository.save(profile);
  }

  @Transactional(readOnly = true)
  public EmployeeEntity getByUserId(UUID userId) {
    return employeeRepository
        .findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Employee profile not found"));
  }

  @Transactional
  public EmployeeEntity updateName(UUID userId, String firstName, String lastName) {
    EmployeeEntity profile = getByUserId(userId);
    if (firstName != null && !firstName.isBlank()) {
      profile.setName(firstName);
    }
    if (lastName != null && !lastName.isBlank()) {
      profile.setLastname(lastName);
    }
    return employeeRepository.save(profile);
  }
}
