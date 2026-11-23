package org.example.model.entity;

import java.time.LocalDateTime;
import java.util.List;
import org.example.model.enums.OrderStatus;
import org.example.model.exception.AccessDeniedException;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.ValidationException;
import org.example.model.validation.Validations;
import org.example.model.valueobject.Money;
import org.example.model.valueobject.OrderItem;
import org.example.model.valueobject.ShippingAddress;

public class Order {

    private final String id;
    private final String userId;
    private final List<OrderItem> items;
    private final ShippingAddress shippingAddress;
    private final Money totalAmount;
    private OrderStatus status;
    private final LocalDateTime createdAt;

    public Order(String id, String userId, List<OrderItem> items, ShippingAddress shippingAddress) {
        if (items == null || items.isEmpty()) {
            throw new ValidationException("An order must contain at least one product.");
        }
        this.id = Validations.requireNotBlank(id, "Order ID");
        this.userId = Validations.requireNotBlank(userId, "User ID");
        this.items = List.copyOf(items);
        this.shippingAddress = shippingAddress;
        this.totalAmount = this.items.stream().map(OrderItem::totalPrice).reduce(Money.ZERO, Money::add);
        this.status = OrderStatus.AWAITING_PAYMENT;
        this.createdAt = LocalDateTime.now();
    }

    public boolean belongsTo(String candidateUserId) {
        return userId.equals(candidateUserId);
    }

    public void ensureBelongsTo(String candidateUserId) {
        if (!belongsTo(candidateUserId)) {
            throw new AccessDeniedException("This order does not belong to you.");
        }
    }

    public boolean containsProduct(String productId) {
        return items.stream().anyMatch(item -> item.productId().equals(productId));
    }

    public void ensurePayable() {
        if (status.isPaidState()) {
            throw new BusinessRuleException("This order has already been paid.");
        }
        if (status != OrderStatus.AWAITING_PAYMENT) {
            throw new BusinessRuleException("This order is in status '" + status.getLabel() + "' and cannot be paid.");
        }
    }

    public void markAsPaid() {
        ensurePayable();
        status = OrderStatus.PAID;
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public Money getTotalAmount() {
        return totalAmount;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
