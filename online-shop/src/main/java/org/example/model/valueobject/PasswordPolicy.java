package org.example.model.valueobject;

import org.example.model.exception.ValidationException;

/** Business rule describing which raw passwords are acceptable. */
public final class PasswordPolicy {

    public static final int MIN_LENGTH = 8;

    private PasswordPolicy() {
    }

    public static void validate(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new ValidationException("Password must not be empty.");
        }
        if (rawPassword.length() < MIN_LENGTH) {
            throw new ValidationException("Password must be at least " + MIN_LENGTH + " characters.");
        }
    }
}
