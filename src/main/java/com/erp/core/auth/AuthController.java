package com.erp.core.auth;

import com.erp.core.auth.dto.ChangePasswordRequest;
import com.erp.core.auth.dto.ForgotPasswordRequest;
import com.erp.core.auth.dto.LoginRequest;
import com.erp.core.auth.dto.LoginResponse;
import com.erp.core.auth.dto.MessageResponse;
import com.erp.core.auth.dto.ResetPasswordRequest;
import com.erp.core.security.JwtPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@SecurityRequirements
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final PasswordResetService passwordResetService;

    private static final String FORGOT_MSG =
            "If this organization and email match an account and email delivery is configured, "
                    + "you will receive reset instructions shortly.";

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/forgot-password")
    public MessageResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        passwordResetService.requestReset(request.orgId(), request.email());
        return new MessageResponse(FORGOT_MSG);
    }

    @PostMapping("/reset-password")
    public MessageResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordResetService.resetWithToken(request.token(), request.newPassword());
        return new MessageResponse("Password updated. You can sign in with your new password.");
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public MessageResponse changePassword(
            @AuthenticationPrincipal JwtPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(
                principal.userId(), principal.orgId(), request.currentPassword(), request.newPassword());
        return new MessageResponse("Password updated.");
    }
}
