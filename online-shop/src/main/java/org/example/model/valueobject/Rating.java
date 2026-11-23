package org.example.model.valueobject;

import org.example.model.exception.ValidationException;

/** A product score between 1 and 5. */
public record Rating(int value) {

    public static final int MIN = 1;
    public static final int MAX = 5;

    public Rating {
        if (value < MIN || value > MAX) {
            throw new ValidationException("Rating must be between " + MIN + " and " + MAX + ".");
        }
    }
}
