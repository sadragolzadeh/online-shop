package org.example.model.valueobject;

import java.util.Locale;
import java.util.regex.Pattern;
import org.example.model.exception.ValidationException;

/** A syntactically valid, normalised (trimmed, lower-case) e-mail address. */
public record Email(String value) {

    private static final Pattern FORMAT =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    public Email {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Email must not be empty.");
        }
        value = value.trim().toLowerCase(Locale.ROOT);
        if (!FORMAT.matcher(value).matches()) {
            throw new ValidationException("Email format is invalid.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
