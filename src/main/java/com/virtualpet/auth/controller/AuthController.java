package com.virtualpet.auth.controller;

import com.virtualpet.auth.dto.AuthDTO.AuthResponse;
import com.virtualpet.auth.dto.AuthDTO.ChangePasswordRequest;
import com.virtualpet.auth.dto.AuthDTO.ForgotPasswordRequest;
import com.virtualpet.auth.dto.AuthDTO.MessageResponse;
import com.virtualpet.auth.dto.AuthDTO.RefreshTokenRequest;
import com.virtualpet.auth.dto.AuthDTO.ResetPasswordRequest;
import com.virtualpet.auth.service.AuthService;
import com.virtualpet.auth.service.PasswordResetService;
import com.virtualpet.auth.service.RefreshTokenService;
import com.virtualpet.common.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Operaciones de autenticación compartidas por todos los roles.
 * Login del marketplace vive en CustomerController.
 * Login del backoffice vive en BackofficeAuthController.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Rota el par access token + refresh token.
     * Revoca el refresh token usado y emite uno nuevo.
     * El cliente debe reemplazar ambos tokens.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
        @Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(refreshTokenService.refreshAccessToken(request.refreshToken()));
    }


    /** Envía el email con el link de reset. Siempre responde 200 para no exponer si el email existe. */
    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request);
        return ResponseEntity.ok(new MessageResponse("Si el email existe, recibirás un enlace para restablecer tu contraseña."));
    }

    /** Consume el token del email y aplica la nueva contraseña. */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(passwordResetService.resetPassword(request));
    }

    /**
     * Cambia la contraseña del usuario autenticado.
     * Requiere la contraseña actual como segunda verificación.
     * Invalida todas las sesiones activas al completarse.
     */
    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
        @Valid @RequestBody ChangePasswordRequest request,
        @AuthenticationPrincipal UserPrincipal currentUser) {
        authService.changePassword(currentUser.getId(), request);
        return ResponseEntity.ok(new MessageResponse("Contraseña actualizada. Por seguridad, iniciá sesión nuevamente."));
    }
}