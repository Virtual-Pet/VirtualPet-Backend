package com.virtualpet.auth.controller;

import com.virtualpet.auth.dto.AuthDTO.AuthTokensDTO;
import com.virtualpet.auth.dto.AuthDTO.ChangePasswordRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.LoginRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.LogoutRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.RefreshRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.RefreshResponseDTO;
import com.virtualpet.auth.dto.AuthDTO.RegisterCustomerRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.RegisterEmployeeRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.UpdateMeRequestDTO;
import com.virtualpet.auth.dto.AuthDTO.UserDTO;
import com.virtualpet.auth.dto.AuthDTO.UserSummaryDTO;
import com.virtualpet.auth.service.AuthService;
import com.virtualpet.cart.web.CartSessionCookie;
import com.virtualpet.common.config.VirtualPetProperties;
import com.virtualpet.common.security.UserPrincipal;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
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
  private final VirtualPetProperties properties;

  /* ---------- Session ---------- */

  @PostMapping("/login")
  public ResponseEntity<AuthTokensDTO> login(
      @Valid @RequestBody LoginRequestDTO request,
      @CookieValue(name = CartSessionCookie.NAME, required = false) String cartSessionId,
      HttpServletResponse response) {
    AuthTokensDTO tokens = authService.login(request, cartSessionId);
    if (cartSessionId != null && !cartSessionId.isBlank()) {
      response.addHeader(
          HttpHeaders.SET_COOKIE, CartSessionCookie.clear(properties.getCart()).toString());
    }
    return ResponseEntity.ok(tokens);
  }

  @PostMapping("/logout")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void logout(@Valid @RequestBody LogoutRequestDTO request) {
    authService.logout(request.refreshToken());
  }

  @PostMapping("/refresh")
  public ResponseEntity<RefreshResponseDTO> refresh(@Valid @RequestBody RefreshRequestDTO request) {
    return ResponseEntity.ok(authService.refresh(request.refreshToken()));
  }

  /* ---------- Registration ---------- */

  @PostMapping("/register/customer")
  @ResponseStatus(HttpStatus.CREATED)
  public UserSummaryDTO registerCustomer(@Valid @RequestBody RegisterCustomerRequestDTO request) {
    return authService.registerCustomer(request);
  }

  @PostMapping("/register/employee")
  @PreAuthorize("hasRole('ADMIN')")
  @ResponseStatus(HttpStatus.CREATED)
  public UserSummaryDTO registerEmployee(@Valid @RequestBody RegisterEmployeeRequestDTO request) {
    return authService.registerEmployee(request);
  }

  /* ---------- Profile ---------- */

  @GetMapping("/me")
  public UserDTO getMe(@AuthenticationPrincipal UserPrincipal currentUser) {
    return authService.getMe(currentUser.getId());
  }

  @PatchMapping("/me")
  public UserDTO updateMe(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @Valid @RequestBody UpdateMeRequestDTO request) {
    return authService.updateMe(currentUser.getId(), request);
  }

  /* ---------- Passwords ---------- */

  @PostMapping("/password/change")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(
      @AuthenticationPrincipal UserPrincipal currentUser,
      @Valid @RequestBody ChangePasswordRequestDTO request) {
    authService.changePassword(currentUser.getId(), request);
  }
}
