package org.example.model.exception;

/** Raised when an input value is empty, malformed or out of range. */
public class ValidationException extends DomainException {

    public ValidationException(String message) {
        super(message);
    }
}
