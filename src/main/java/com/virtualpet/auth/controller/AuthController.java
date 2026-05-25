package com.virtualpet.auth.controller;

import com.virtualpet.auth.dto.AuthDTO.AuthTokens;
import com.virtualpet.auth.dto.AuthDTO.ChangePasswordRequest;
import com.virtualpet.auth.dto.AuthDTO.ForgotPasswordRequest;
import com.virtualpet.auth.dto.AuthDTO.LoginRequest;
import com.virtualpet.auth.dto.AuthDTO.LogoutRequest;
import com.virtualpet.auth.dto.AuthDTO.RefreshRequest;
import com.virtualpet.auth.dto.AuthDTO.RefreshResponse;
import com.virtualpet.auth.dto.AuthDTO.RegisterCustomerRequest;
import com.virtualpet.auth.dto.AuthDTO.RegisterEmployeeRequest;
import com.virtualpet.auth.dto.AuthDTO.ResetPasswordRequest;
import com.virtualpet.auth.dto.AuthDTO.UpdateMeRequest;
import com.virtualpet.auth.dto.AuthDTO.User;
import com.virtualpet.auth.dto.AuthDTO.UserSummary;
import com.virtualpet.auth.service.AuthService;
import com.virtualpet.auth.service.PasswordResetService;
import com.virtualpet.common.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;
  private final PasswordResetService passwordResetService;

  /* ---------- Session ---------- */

  @PostMapping("/login")
  public ResponseEntity<AuthTokens> login(@Valid @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.login(request));
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@Valid @RequestBody LogoutRequest request) {
    authService.logout(request.refreshToken());
  }

  @PostMapping("/refresh")
  public ResponseEntity<RefreshResponse> refresh(@Valid @RequestBody RefreshRequest request) {
    return ResponseEntity.ok(authService.refresh(request.refreshToken()));
  }

  /* ---------- Registration ---------- */

  @PostMapping("/register/customer")
  @ResponseStatus(HttpStatus.CREATED)
  public UserSummary registerCustomer(@Valid @RequestBody RegisterCustomerRequest request) {
    return authService.registerCustomer(request);
  }

  @PostMapping("/register/employee")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public UserSummary registerEmployee(@Valid @RequestBody RegisterEmployeeRequest request) {
    return authService.registerEmployee(request);
  }

  /* ---------- Profile ---------- */

  @GetMapping("/me")
  public User getMe(@AuthenticationPrincipal UserPrincipal currentUser) {
    return authService.getMe(currentUser.getId());
  }

  @PatchMapping("/me")
  public User updateMe(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @Valid @RequestBody UpdateMeRequest request) {
    return authService.updateMe(currentUser.getId(), request);
  }

  /* ---------- Passwords ---------- */

  @PostMapping("/password/forgot")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public void forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
    passwordResetService.requestReset(request);
  }

  @PostMapping("/password/reset")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
    passwordResetService.resetPassword(request);
  }

  @PostMapping("/password/change")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @Valid @RequestBody ChangePasswordRequest request) {
    authService.changePassword(currentUser.getId(), request);
  }
}
