package org.example.model.enums;

public enum PaymentStatus {
    PENDING("Pending confirmation"),
    SUCCESSFUL("Successful"),
    FAILED("Failed");

    private final String label;

    PaymentStatus(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
