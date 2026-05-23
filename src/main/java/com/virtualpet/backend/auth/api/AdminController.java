package com.virtualpet.backend.auth.api;

import com.virtualpet.backend.auth.dto.EmployeeDTO.EmployeeResponse;
import com.virtualpet.backend.auth.dto.EmployeeDTO.RegisterEmployeeRequest;
import com.virtualpet.backend.auth.dto.AuthDTO.MessageResponse;
import com.virtualpet.backend.auth.service.registerOrchestrator.EmployeeOrchestrator;
import com.virtualpet.backend.shared.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/employees")
@RequiredArgsConstructor
public class AdminController {

    private final EmployeeOrchestrator orchestrator;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> registerEmployee(@Valid @RequestBody RegisterEmployeeRequest request) {
        orchestrator.executeRegister(request);
        return ResponseEntity.ok(new MessageResponse("Empleado aprovisionado con éxito."));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('EMPLOYEE', 'ADMIN')")
    public ResponseEntity<EmployeeResponse> getMyProfile(@AuthenticationPrincipal UserPrincipal currentUser) {
        return ResponseEntity.ok(orchestrator.executeMe(currentUser.getId()));
    }

    //@DeleteMapping
    //Elimiar un empleado (active = false) softdelete
}
