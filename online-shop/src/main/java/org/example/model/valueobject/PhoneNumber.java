package org.example.model.valueobject;

import java.util.regex.Pattern;
import org.example.model.exception.ValidationException;

/** An Iranian mobile number normalised to the local form 09XXXXXXXXX. */
public record PhoneNumber(String value) {

    private static final Pattern FORMAT = Pattern.compile("^09\\d{9}$");

    public PhoneNumber {
        if (value == null || value.isBlank()) {
            throw new ValidationException("Mobile number must not be empty.");
        }
        value = normalise(value);
        if (!FORMAT.matcher(value).matches()) {
            throw new ValidationException("Mobile number must look like 09123456789.");
        }
    }

    private static String normalise(String raw) {
        String digits = raw.replaceAll("[\\s-]", "");
        if (digits.startsWith("+98")) {
            return "0" + digits.substring(3);
        }
        if (digits.startsWith("0098")) {
            return "0" + digits.substring(4);
        }
        return digits;
    }

    @Override
    public String toString() {
        return value;
    }
}
