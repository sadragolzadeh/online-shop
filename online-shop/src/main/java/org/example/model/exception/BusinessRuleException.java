package org.example.model.exception;

/** Raised when an operation conflicts with the current state of the domain. */
public class BusinessRuleException extends DomainException {

    public BusinessRuleException(String message) {
        super(message);
    }
}
