package com.virtualpet.auth.service;

import com.virtualpet.auth.domain.CustomerEntity;
import com.virtualpet.auth.repository.CustomerRepository;
import com.virtualpet.common.exception.ApiException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

  private final CustomerRepository customerRepository;

  @Transactional
  public CustomerEntity createProfile(UUID userId, String firstName, String lastName) {
    CustomerEntity profile =
        CustomerEntity.builder().userId(userId).name(firstName).lastname(lastName).build();
    return customerRepository.save(profile);
  }

  @Transactional(readOnly = true)
  public CustomerEntity getByUserId(UUID userId) {
    return customerRepository
        .findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Customer profile not found"));
  }

  @Transactional
  public CustomerEntity updateName(UUID userId, String firstName, String lastName) {
    CustomerEntity profile = getByUserId(userId);
    if (firstName != null && !firstName.isBlank()) {
      profile.setName(firstName);
    }
    if (lastName != null && !lastName.isBlank()) {
      profile.setLastname(lastName);
    }
    return customerRepository.save(profile);
  }
}
