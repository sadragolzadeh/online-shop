package org.example.model.enums;

public enum OrderStatus {
    AWAITING_PAYMENT("Awaiting payment", false),
    PAID("Paid", true),
    PROCESSING("Processing", true),
    SHIPPED("Shipped", true),
    DELIVERED("Delivered", true),
    CANCELLED("Cancelled", false);

    private final String label;
    private final boolean paidState;

    OrderStatus(String label, boolean paidState) {
        this.label = label;
        this.paidState = paidState;
    }

    public String getLabel() {
        return label;
    }

    /** True when the order has been paid for (regardless of later fulfilment steps). */
    public boolean isPaidState() {
        return paidState;
    }
}
