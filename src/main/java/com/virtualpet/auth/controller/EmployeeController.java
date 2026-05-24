package com.virtualpet.auth.controller;

import com.virtualpet.auth.dto.EmployeeDTO.EmployeeResponse;
import com.virtualpet.auth.service.registerOrchestrator.EmployeeOrchestrator;
import com.virtualpet.common.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/employees") // Ruta personal / genérica
@RequiredArgsConstructor
public class EmployeeController {

  private final EmployeeOrchestrator orchestrator;

  @GetMapping("/me")
  @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
  public ResponseEntity<EmployeeResponse> getMyProfile(
      @AuthenticationPrincipal UserPrincipal currentUser) {
    return ResponseEntity.ok(orchestrator.executeMe(currentUser.getId()));
  }
}
