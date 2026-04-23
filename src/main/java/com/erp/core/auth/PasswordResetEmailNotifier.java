package com.erp.core.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PasswordResetEmailNotifier {

    @Autowired(required = false)
    private JavaMailSender javaMailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.frontend-base-url}")
    private String frontendBaseUrl;

    public boolean canSend() {
        return javaMailSender != null;
    }

    public void sendResetLink(String toEmail, String rawToken) {
        if (javaMailSender == null) {
            log.warn(
                    "Self-service password reset was requested but JavaMailSender is not configured "
                            + "(set spring.mail.host and related properties). Ask an admin to set a new password.");
            return;
        }
        String base = frontendBaseUrl.endsWith("/") ? frontendBaseUrl.substring(0, frontendBaseUrl.length() - 1) : frontendBaseUrl;
        String link = base + "/reset-password?token=" + rawToken;
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setFrom(from);
        msg.setTo(toEmail);
        msg.setSubject("Reset your password");
        msg.setText(
                "You asked to reset your password for your organization account.\n\n"
                        + "Open this link within one hour:\n"
                        + link
                        + "\n\n"
                        + "If you did not request this, you can ignore this email.");
        javaMailSender.send(msg);
    }
}
