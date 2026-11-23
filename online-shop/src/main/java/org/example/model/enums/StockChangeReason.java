package org.example.model.enums;

public enum StockChangeReason {
    MANUAL_ADJUSTMENT("Manual adjustment by admin"),
    ORDER_PAID("Sale (paid order)");

    private final String label;

    StockChangeReason(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
