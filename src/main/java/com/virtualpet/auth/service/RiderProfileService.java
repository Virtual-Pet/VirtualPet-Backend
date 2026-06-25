package com.virtualpet.auth.service;

import com.virtualpet.auth.repository.RiderRepository;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.virtualpet.auth.domain.RiderEntity;
import com.virtualpet.common.exception.ApiException;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RiderProfileService {

    private final RiderRepository riderRepository;

    @Transactional(readOnly = true)
  public RiderEntity getByUserId(UUID userId) {
    return riderRepository
        .findById(userId)
        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Rider profile not found"));
  }

  @Transactional
  public RiderEntity updateName(UUID userId, String firstName, String lastName) {
    RiderEntity profile = getByUserId(userId);
    if (firstName != null && !firstName.isBlank()) {
      profile.setName(firstName);
    }
    if (lastName != null && !lastName.isBlank()) {
      profile.setLastname(lastName);
    }
    return riderRepository.save(profile);
  }
}
