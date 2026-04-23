package com.erp.core.auth;

import com.erp.core.auth.dto.LoginRequest;
import com.erp.core.auth.dto.LoginResponse;
import com.erp.core.security.JwtService;
import com.erp.core.users.AppUser;
import com.erp.core.users.UserRepository;
import com.erp.shared.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        AppUser user = userRepository.findByOrgIdAndEmail(request.orgId(), email)
                .orElseThrow(() -> new BusinessException("Invalid credentials"));
        if (!user.isEnabled()) {
            throw new BusinessException("User disabled");
        }
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BusinessException("Invalid credentials");
        }
        String token = jwtService.generateToken(user.getId(), user.getOrgId(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getOrgId(), user.getRole());
    }

    @Transactional
    public void changePassword(UUID userId, UUID orgId, String currentPassword, String newPassword) {
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found"));
        if (!user.getOrgId().equals(orgId)) {
            throw new BusinessException("User not found");
        }
        if (!user.isEnabled()) {
            throw new BusinessException("User disabled");
        }
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResetTokenRepository.deleteAllByUserId(userId);
    }
}
