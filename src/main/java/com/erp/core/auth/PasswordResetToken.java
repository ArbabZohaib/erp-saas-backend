package com.erp.core.auth;

import com.erp.shared.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "password_reset_tokens",
        uniqueConstraints = @UniqueConstraint(name = "uk_password_reset_tokens_token", columnNames = "token")
)
@Getter
@Setter
public class PasswordResetToken extends BaseEntity {

    @Column(nullable = false, name = "user_id")
    private UUID userId;

    @Column(nullable = false, length = 64)
    private String token;

    @Column(nullable = false, name = "expires_at")
    private Instant expiresAt;
}
