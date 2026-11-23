package org.example.model.valueobject;

import java.util.regex.Pattern;
import org.example.model.exception.ValidationException;

/** A postal code made of 5 to 10 digits. */
public record PostalCode(String value) {

    private static final Pattern FORMAT = Pattern.compile("^\\d{5,10}$");

    public PostalCode {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Postal code must not be empty.");
        }
        value = value.trim();
        if (!FORMAT.matcher(value).matches()) {
            throw new ValidationException("Postal code must be between 5 and 10 digits.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
