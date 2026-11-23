package org.example.model.entity;

import java.time.LocalDateTime;
import java.util.Optional;
import org.example.model.enums.PaymentStatus;
import org.example.model.exception.BusinessRuleException;
import org.example.model.valueobject.Money;

/** One payment attempt for an order. Card data is never stored. */
public class Payment {

    private final String id;
    private final String orderId;
    private final Money amount;
    private PaymentStatus status;
    private String transactionRef;
    private String failureReason;
    private final LocalDateTime createdAt;

    public Payment(String id, String orderId, Money amount) {
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.status = PaymentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void markSuccessful(String gatewayTransactionRef) {
        ensurePending();
        this.status = PaymentStatus.SUCCESSFUL;
        this.transactionRef = gatewayTransactionRef;
    }

    public void markFailed(String reason) {
        ensurePending();
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
    }

    private void ensurePending() {
        if (status != PaymentStatus.PENDING) {
            throw new BusinessRuleException("The status of this payment has already been determined.");
        }
    }

    public String getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Money getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public Optional<String> getTransactionRef() {
        return Optional.ofNullable(transactionRef);
    }

    public Optional<String> getFailureReason() {
        return Optional.ofNullable(failureReason);
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
