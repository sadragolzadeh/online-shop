package org.example.model.exception;

/** Raised when a requested entity does not exist. */
public class NotFoundException extends DomainException {

    public NotFoundException(String message) {
        super(message);
    }
}
