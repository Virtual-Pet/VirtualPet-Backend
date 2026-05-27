package com.virtualpet.auth.service;

import com.virtualpet.auth.domain.CustomerEntity;
import com.virtualpet.auth.dto.CustomerDTO.RegisterCustomerRequest;
import com.virtualpet.auth.repository.CustomerRepository;
import com.virtualpet.common.exception.ApiException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestiona el perfil de datos del cliente (tabla auth.customers).
 * Solo conoce CustomerEntity — no sabe nada de UserEntity ni JWT.
 * El acoplamiento con auth.users ocurre en CustomerOrchestrator.
 */
@Service
@RequiredArgsConstructor
public class CustomerProfileService {

    private final CustomerRepository customerRepository;

    @Transactional
    public void createProfile(UUID userId, RegisterCustomerRequest request) {
        CustomerEntity profile = CustomerEntity.builder()
            .userId(userId)
            .name(request.name().trim())
            .lastname(request.lastname().trim())
            .dni(request.dni())
            .phone(request.phone())
            .build();

        customerRepository.save(profile);
    }

    @Transactional(readOnly = true)
    public CustomerEntity getByUserId(UUID userId) {
        return customerRepository
            .findById(userId)
            .orElseThrow(() -> new ApiException(
                HttpStatus.NOT_FOUND,
                "Perfil de cliente no encontrado."
            ));
    }
}