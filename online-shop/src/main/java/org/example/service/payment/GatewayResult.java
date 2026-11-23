package org.example.service.payment;

import org.example.model.valueobject.Money;

/** What the payment gateway reports back after a charge attempt. */
public record GatewayResult(boolean approved, String transactionRef, Money paidAmount, String message) {
}
