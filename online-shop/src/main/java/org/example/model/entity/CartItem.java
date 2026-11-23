package org.example.model.entity;

import org.example.model.validation.Validations;
import org.example.model.valueobject.Money;

public class CartItem {

    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = validQuantity(quantity);
    }

    public void changeQuantity(int newQuantity) {
        this.quantity = validQuantity(newQuantity);
    }

    public void increaseBy(int amount) {
        this.quantity = validQuantity(quantity + amount);
    }

    private int validQuantity(int candidate) {
        Validations.requirePositive(candidate, "Quantity");
        product.ensureCanBePurchased(candidate);
        return candidate;
    }

    public Money subtotal() {
        return product.getPrice().multiply(quantity);
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }
}
