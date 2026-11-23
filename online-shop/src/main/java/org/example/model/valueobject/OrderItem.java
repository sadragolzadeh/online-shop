package org.example.model.valueobject;

import org.example.model.validation.Validations;

/** A snapshot of one purchased product; later price changes never alter past orders. */
public record OrderItem(String productId, String productName, Money unitPrice, int quantity) {

    public OrderItem {
        productId = Validations.requireNotBlank(productId, "Product ID");
        productName = Validations.requireNotBlank(productName, "Product name");
        Validations.requirePositive(quantity, "Quantity");
    }

    public Money totalPrice() {
        return unitPrice.multiply(quantity);
    }
}
