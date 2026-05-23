package com.virtualpet.backend.auth.api;

import com.virtualpet.backend.auth.dto.AuthDTO.MessageResponse;
import com.virtualpet.backend.auth.dto.CustomerDTO.CustomerProfileResponse;
import com.virtualpet.backend.auth.dto.CustomerDTO.RegisterCustomerRequest;
import com.virtualpet.backend.auth.service.registerOrchestrator.CustomerOrchestrator;
import com.virtualpet.backend.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

  private final CustomerOrchestrator orchestrator;

  @PostMapping("/register") // REEMPLAZA AL /auth/register
  public ResponseEntity<MessageResponse> register(
      @Valid @RequestBody RegisterCustomerRequest request) {
    orchestrator.executeRegister(request);
    return ResponseEntity.ok(new MessageResponse("Cliente registrado con éxito."));
  }

  @GetMapping("/me")
  @PreAuthorize("hasRole('CUSTOMER')")
  public ResponseEntity<CustomerProfileResponse> getMyProfile(
      @AuthenticationPrincipal
          UserPrincipal currentUser) { // Spring extrae el UUID del JWT automáticamente

    return ResponseEntity.ok(orchestrator.executeMe(currentUser.getId()));
  }
}
