package com.virtualpet.backend.auth.service;

import com.virtualpet.backend.auth.domain.CustomerEntity;
import com.virtualpet.backend.auth.dto.CustomerDTO.RegisterCustomerRequest;
import com.virtualpet.backend.auth.repository.CustomerRepository;
import com.virtualpet.backend.shared.exception.ApiException;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerRepository customerRepository;

    @Transactional
    public void createProfile(UUID userId, RegisterCustomerRequest request) {
        CustomerEntity profile = CustomerEntity.builder()
                .userId(userId) // Soft reference (Relación lógica)
                .name(request.name())
                .lastname(request.lastname())
                .dni(request.dni())
                .phone(request.phone())
                .build();

        customerRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public CustomerEntity getByUserId(UUID userId) {
        return customerRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Perfil de cliente no encontrado para el identificador proporcionado"));
    }
}
