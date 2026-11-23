package org.example.cli;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import org.example.model.entity.Address;
import org.example.model.entity.Cart;
import org.example.model.entity.CartItem;
import org.example.model.entity.Category;
import org.example.model.entity.Order;
import org.example.model.entity.Payment;
import org.example.model.entity.Product;
import org.example.model.entity.Review;
import org.example.model.entity.StockChange;
import org.example.model.entity.User;
import org.example.model.valueobject.OrderItem;

/** Turns domain objects into readable terminal text. Sensitive data (passwords, cards) is never printed. */
public class EntityPrinter {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private final ConsoleIO io;

    public EntityPrinter(ConsoleIO io) {
        this.io = io;
    }

    public void printCategories(List<Category> categories) {
        if (categories.isEmpty()) {
            io.println("There are no active categories.");
            return;
        }
        categories.forEach(category -> io.println("[" + category.getId() + "] " + category.getName()));
    }

    public void printProducts(List<Product> products, String emptyMessage) {
        if (products.isEmpty()) {
            io.println(emptyMessage);
            return;
        }
        for (Product product : products) {
            io.println(String.format("[%s] %s | %s | %s | %s | Rating: %s", product.getId(), product.getName(),
                    product.getPrice(), product.getCategory().getName(),
                    product.isAvailable() ? "In stock" : "Out of stock", formatRating(product)));
        }
    }

    public void printProductDetails(Product product, List<Review> reviews) {
        io.println("Name: " + product.getName());
        io.println("Description: " + product.getDescription());
        io.println("Price: " + product.getPrice());
        io.println("Brand: " + product.getBrand());
        io.println("Category: " + product.getCategory().getName());
        io.println("Stock: " + product.getStockQuantity());
        io.println("Images: " + (product.getImageUrls().isEmpty() ? "-" : String.join(", ", product.getImageUrls())));
        io.println("Rating: " + formatRating(product) + " (based on " + product.getRatingCount() + " ratings)");
        if (!product.isActive()) {
            io.println("Note: this product is inactive and cannot be purchased.");
        }
        io.println("Customer reviews:");
        if (reviews.isEmpty()) {
            io.println("  No reviews yet.");
        }
        reviews.forEach(review -> io.println("  - " + review.getAuthorDisplayName() + ": " + review.getText()));
    }

    public void printCart(Cart cart) {
        if (cart.isEmpty()) {
            io.println("Your cart is empty.");
            return;
        }
        for (CartItem item : cart.getItems()) {
            io.println(String.format("[%s] %s x %d = %s", item.getProduct().getId(), item.getProduct().getName(),
                    item.getQuantity(), item.subtotal()));
        }
        io.println("Total quantity: " + cart.getTotalQuantity());
        io.println("Total amount: " + cart.getTotalAmount());
    }

    public void printAddresses(List<Address> addresses) {
        if (addresses.isEmpty()) {
            io.println("You have not saved any address yet.");
            return;
        }
        for (Address address : addresses) {
            io.println(String.format("[%s] %s%s | Recipient: %s | Phone: %s | %s", address.getId(), address.getTitle(),
                    address.isDefaultAddress() ? " (default)" : "", address.getDetails().recipientName(),
                    address.getDetails().phone(), address.getDetails().asSingleLine()));
        }
    }

    public void printProfile(User user) {
        io.println("Name: " + user.getFullName());
        io.println("Email: " + user.getEmail().map(Object::toString).orElse("-"));
        io.println("Mobile number: " + user.getPhone().map(Object::toString).orElse("-"));
        io.println("Member since: " + formatDate(user.getCreatedAt()));
    }

    public void printOrders(List<Order> orders) {
        if (orders.isEmpty()) {
            io.println("No orders found.");
            return;
        }
        for (Order order : orders) {
            io.println(String.format("[%s] %s | Status: %s | Total: %s", order.getId(),
                    formatDate(order.getCreatedAt()), order.getStatus().getLabel(), order.getTotalAmount()));
        }
    }

    public void printOrderDetails(Order order) {
        io.println("Order " + order.getId() + " - Status: " + order.getStatus().getLabel());
        io.println("Placed on: " + formatDate(order.getCreatedAt()));
        for (OrderItem item : order.getItems()) {
            io.println(String.format("  %s x %d (%s each) = %s", item.productName(), item.quantity(),
                    item.unitPrice(), item.totalPrice()));
        }
        io.println("Shipping address: " + order.getShippingAddress().recipientName() + " - "
                + order.getShippingAddress().asSingleLine());
        io.println("Final amount: " + order.getTotalAmount());
    }

    public void printPayments(List<Payment> payments) {
        payments.forEach(payment -> io.println(String.format("[%s] %s | Amount: %s | Status: %s", payment.getId(),
                formatDate(payment.getCreatedAt()), payment.getAmount(), payment.getStatus().getLabel())));
    }

    public void printInventory(List<Product> products) {
        if (products.isEmpty()) {
            io.println("No products registered.");
            return;
        }
        products.forEach(product -> io.println(String.format("[%s] %s | Stock: %d | %s", product.getId(),
                product.getName(), product.getStockQuantity(), product.isActive() ? "Active" : "Inactive")));
    }

    public void printStockChanges(List<StockChange> changes) {
        if (changes.isEmpty()) {
            io.println("No stock changes recorded for this product.");
            return;
        }
        changes.forEach(change -> io.println(String.format("%s | %d -> %d (%+d) | %s | by: %s",
                formatDate(change.getChangedAt()), change.getPreviousQuantity(), change.getNewQuantity(),
                change.getDelta(), change.getReason().getLabel(), change.getChangedBy())));
    }

    private static String formatRating(Product product) {
        return product.getRatingCount() == 0 ? "-" : String.format(Locale.ROOT, "%.1f", product.getAverageRating());
    }

    private static String formatDate(LocalDateTime dateTime) {
        return DATE_FORMAT.format(dateTime);
    }
}
