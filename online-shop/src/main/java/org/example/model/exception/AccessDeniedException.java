package org.example.model.exception;

/** Raised when the caller is not allowed to perform an operation. */
public class AccessDeniedException extends DomainException {

    public AccessDeniedException(String message) {
        super(message);
    }
}
