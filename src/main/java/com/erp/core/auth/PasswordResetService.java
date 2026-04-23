package com.erp.core.auth;

import com.erp.core.users.AppUser;
import com.erp.core.users.UserRepository;
import com.erp.shared.exceptions.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetEmailNotifier emailNotifier;

    /**
     * Always safe to call: does not reveal whether the email exists. Sends mail only when SMTP is configured.
     */
    @Transactional
    public void requestReset(UUID orgId, String email) {
        if (!emailNotifier.canSend()) {
            return;
        }
        String normalized = email.trim().toLowerCase();
        AppUser user = userRepository.findByOrgIdAndEmail(orgId, normalized).orElse(null);
        if (user == null || !user.isEnabled()) {
            return;
        }
        tokenRepository.deleteAllByUserId(user.getId());
        PasswordResetToken row = new PasswordResetToken();
        row.setOrgId(user.getOrgId());
        row.setUserId(user.getId());
        row.setToken(newRawToken());
        row.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));
        tokenRepository.save(row);
        emailNotifier.sendResetLink(user.getEmail(), row.getToken());
    }

    @Transactional
    public void resetWithToken(String rawToken, String newPassword) {
        PasswordResetToken row = tokenRepository.findByToken(rawToken.trim())
                .orElseThrow(() -> new BusinessException("Invalid or expired reset link"));
        if (row.getExpiresAt().isBefore(Instant.now())) {
            tokenRepository.delete(row);
            throw new BusinessException("Invalid or expired reset link");
        }
        AppUser user = userRepository.findById(row.getUserId())
                .orElseThrow(() -> new BusinessException("Invalid or expired reset link"));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        tokenRepository.deleteAllByUserId(user.getId());
    }

    private static String newRawToken() {
        byte[] buf = new byte[32];
        RANDOM.nextBytes(buf);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
    }
}
