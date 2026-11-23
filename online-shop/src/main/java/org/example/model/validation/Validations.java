package org.example.model.validation;

import org.example.model.exception.ValidationException;

/** Small reusable guard clauses shared by entities and value objects. */
public final class Validations {

    private Validations() {
    }

    public static String requireNotBlank(String value, String fieldLabel) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldLabel + " must not be empty.");
        }
        return value.trim();
    }

    public static String requireMaxLength(String value, int maxLength, String fieldLabel) {
        if (value.length() > maxLength) {
            throw new ValidationException(fieldLabel + " must not exceed " + maxLength + " characters.");
        }
        return value;
    }

    public static int requireNonNegative(int value, String fieldLabel) {
        if (value < 0) {
            throw new ValidationException(fieldLabel + " must not be negative.");
        }
        return value;
    }

    public static int requirePositive(int value, String fieldLabel) {
        if (value < 1) {
            throw new ValidationException(fieldLabel + " must be at least 1.");
        }
        return value;
    }
}
