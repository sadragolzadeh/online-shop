package org.example.model.exception;

/** Base type of all business-rule violations raised by the domain. */
public class DomainException extends RuntimeException {

    public DomainException(String message) {
        super(message);
    }
}
