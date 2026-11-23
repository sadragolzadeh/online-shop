package org.example.model.entity;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.NotFoundException;
import org.example.model.validation.Validations;
import org.example.model.valueobject.Money;

/** The shopping cart of one customer. The total is always calculated by the system. */
public class Cart {

    private final String userId;
    private final Map<String, CartItem> items = new LinkedHashMap<>();

    public Cart(String userId) {
        this.userId = Validations.requireNotBlank(userId, "User ID");
    }

    /** Adds a product; if it is already in the cart its quantity is increased. */
    public void addProduct(Product product, int quantity) {
        CartItem existing = items.get(product.getId());
        if (existing == null) {
            items.put(product.getId(), new CartItem(product, quantity));
        } else {
            existing.increaseBy(quantity);
        }
    }

    public void changeQuantity(String productId, int quantity) {
        findItem(productId).changeQuantity(quantity);
    }

    public void removeProduct(String productId) {
        findItem(productId);
        items.remove(productId);
    }

    public void clear() {
        items.clear();
    }

    /** Verifies the cart can become an order: not empty and every product still purchasable. */
    public void ensureReadyForCheckout() {
        if (items.isEmpty()) {
            throw new BusinessRuleException("Your cart is empty.");
        }
        items.values().forEach(item -> item.getProduct().ensureCanBePurchased(item.getQuantity()));
    }

    private CartItem findItem(String productId) {
        CartItem item = items.get(productId);
        if (item == null) {
            throw new NotFoundException("This product is not in the cart.");
        }
        return item;
    }

    public Money getTotalAmount() {
        return items.values().stream().map(CartItem::subtotal).reduce(Money.ZERO, Money::add);
    }

    public int getTotalQuantity() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public String getUserId() {
        return userId;
    }

    public List<CartItem> getItems() {
        return List.copyOf(new ArrayList<>(items.values()));
    }
}
