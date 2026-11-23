package org.example.model.valueobject;

import java.time.LocalDateTime;

/** A one-time recovery code bound to a user and valid until {@code expiresAt}. */
public record PasswordResetToken(String userId, String code, LocalDateTime expiresAt) {

    public boolean isExpired(LocalDateTime now) {
        return now.isAfter(expiresAt);
    }

    public boolean matches(String candidateCode) {
        return code.equals(candidateCode);
    }
}
