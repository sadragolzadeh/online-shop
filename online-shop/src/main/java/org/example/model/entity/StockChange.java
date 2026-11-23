package org.example.model.entity;

import java.time.LocalDateTime;
import org.example.model.enums.StockChangeReason;

/** An audit record of one change in a product's stock. */
public class StockChange {

    private final String id;
    private final String productId;
    private final int previousQuantity;
    private final int newQuantity;
    private final StockChangeReason reason;
    private final String changedBy;
    private final LocalDateTime changedAt;

    public StockChange(String id, String productId, int previousQuantity, int newQuantity,
                       StockChangeReason reason, String changedBy) {
        this.id = id;
        this.productId = productId;
        this.previousQuantity = previousQuantity;
        this.newQuantity = newQuantity;
        this.reason = reason;
        this.changedBy = changedBy;
        this.changedAt = LocalDateTime.now();
    }

    public int getDelta() {
        return newQuantity - previousQuantity;
    }

    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public int getPreviousQuantity() {
        return previousQuantity;
    }

    public int getNewQuantity() {
        return newQuantity;
    }

    public StockChangeReason getReason() {
        return reason;
    }

    public String getChangedBy() {
        return changedBy;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}
