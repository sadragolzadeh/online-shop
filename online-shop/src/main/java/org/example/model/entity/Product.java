package org.example.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.ValidationException;
import org.example.model.validation.Validations;
import org.example.model.valueobject.Money;
import org.example.model.valueobject.Rating;

public class Product {

    private final String id;
    private String name;
    private String description;
    private Money price;
    private int stockQuantity;
    private Category category;
    private String brand;
    private final List<String> imageUrls = new ArrayList<>();
    private boolean active;
    private final LocalDateTime createdAt;
    private int salesCount;
    private int ratingSum;
    private int ratingCount;

    public Product(String id, String name, String description, Money price, int stockQuantity,
                   Category category, String brand, List<String> imageUrls) {
        this.id = Validations.requireNotBlank(id, "Product ID");
        this.active = true;
        this.createdAt = LocalDateTime.now();
        applyDetails(name, description, price, stockQuantity, category, brand, imageUrls);
    }

    public void update(String newName, String newDescription, Money newPrice, int newStockQuantity,
                       Category newCategory, String newBrand, List<String> newImageUrls) {
        applyDetails(newName, newDescription, newPrice, newStockQuantity, newCategory, newBrand, newImageUrls);
    }

    private void applyDetails(String newName, String newDescription, Money newPrice, int newStockQuantity,
                              Category newCategory, String newBrand, List<String> newImageUrls) {
        String validName = Validations.requireNotBlank(newName, "Product name");
        if (newPrice == null) {
            throw new ValidationException("Product price must not be empty.");
        }
        if (newCategory == null) {
            throw new ValidationException("A product must have a category.");
        }
        Validations.requireNonNegative(newStockQuantity, "Product stock");
        this.name = validName;
        this.description = newDescription == null ? "" : newDescription.trim();
        this.price = newPrice;
        this.stockQuantity = newStockQuantity;
        this.category = newCategory;
        this.brand = newBrand == null ? "" : newBrand.trim();
        this.imageUrls.clear();
        if (newImageUrls != null) {
            this.imageUrls.addAll(newImageUrls);
        }
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    /** An active product with at least one unit in stock. */
    public boolean isAvailable() {
        return active && stockQuantity > 0;
    }

    public void ensureCanBePurchased(int quantity) {
        if (!active) {
            throw new BusinessRuleException("Product '" + name + "' is inactive and cannot be purchased.");
        }
        if (stockQuantity == 0) {
            throw new BusinessRuleException("Product '" + name + "' is out of stock.");
        }
        if (quantity > stockQuantity) {
            throw new BusinessRuleException(
                    "Requested quantity of '" + name + "' exceeds the available stock (available: " + stockQuantity + ").");
        }
    }

    /** Adds {@code delta} units to the stock (negative to remove); the stock can never go below zero. */
    public void changeStock(int delta) {
        int newQuantity = stockQuantity + delta;
        if (newQuantity < 0) {
            throw new BusinessRuleException("Stock cannot go below zero (current stock: " + stockQuantity + ").");
        }
        stockQuantity = newQuantity;
    }

    public void increaseSalesCount(int quantity) {
        salesCount += quantity;
    }

    public void addRating(Rating rating) {
        ratingSum += rating.value();
        ratingCount++;
    }

    public void replaceRating(Rating oldRating, Rating newRating) {
        ratingSum += newRating.value() - oldRating.value();
    }

    public double getAverageRating() {
        return ratingCount == 0 ? 0.0 : (double) ratingSum / ratingCount;
    }

    /** Case-insensitive match against name, description, brand and category name. */
    public boolean matchesKeyword(String keyword) {
        String needle = keyword.toLowerCase(Locale.ROOT);
        return contains(name, needle) || contains(description, needle)
                || contains(brand, needle) || contains(category.getName(), needle);
    }

    private static boolean contains(String text, String needle) {
        return text.toLowerCase(Locale.ROOT).contains(needle);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Money getPrice() {
        return price;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public Category getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }

    public List<String> getImageUrls() {
        return Collections.unmodifiableList(imageUrls);
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public int getSalesCount() {
        return salesCount;
    }

    public int getRatingCount() {
        return ratingCount;
    }
}
