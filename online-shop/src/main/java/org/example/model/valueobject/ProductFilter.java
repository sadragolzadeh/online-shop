package org.example.model.valueobject;

import java.math.BigDecimal;
import java.util.Optional;
import org.example.model.entity.Product;
import org.example.model.exception.ValidationException;

/** Immutable set of optional criteria a customer can apply to the product list. */
public final class ProductFilter {

    private final String categoryId;
    private final Money minPrice;
    private final Money maxPrice;
    private final String brand;
    private final Rating minRating;
    private final boolean inStockOnly;

    private ProductFilter(Builder builder) {
        if (builder.minPrice != null && builder.maxPrice != null && builder.minPrice.compareTo(builder.maxPrice) > 0) {
            throw new ValidationException("Minimum price must not be greater than maximum price.");
        }
        this.categoryId = builder.categoryId;
        this.minPrice = builder.minPrice;
        this.maxPrice = builder.maxPrice;
        this.brand = builder.brand;
        this.minRating = builder.minRating;
        this.inStockOnly = builder.inStockOnly;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Optional<String> getCategoryId() {
        return Optional.ofNullable(categoryId);
    }

    public boolean matches(Product product) {
        return matchesCategory(product)
                && matchesPrice(product)
                && matchesBrand(product)
                && matchesRating(product)
                && (!inStockOnly || product.getStockQuantity() > 0);
    }

    private boolean matchesCategory(Product product) {
        return categoryId == null || product.getCategory().getId().equals(categoryId);
    }

    private boolean matchesPrice(Product product) {
        boolean aboveMin = minPrice == null || product.getPrice().compareTo(minPrice) >= 0;
        boolean belowMax = maxPrice == null || product.getPrice().compareTo(maxPrice) <= 0;
        return aboveMin && belowMax;
    }

    private boolean matchesBrand(Product product) {
        return brand == null || brand.equalsIgnoreCase(product.getBrand());
    }

    private boolean matchesRating(Product product) {
        return minRating == null || product.getAverageRating() >= minRating.value();
    }

    /** Fluent builder; validation happens in {@link #build()}. */
    public static final class Builder {
        private String categoryId;
        private Money minPrice;
        private Money maxPrice;
        private String brand;
        private Rating minRating;
        private boolean inStockOnly;

        public Builder categoryId(String categoryId) {
            this.categoryId = categoryId;
            return this;
        }

        public Builder minPrice(BigDecimal minPrice) {
            this.minPrice = minPrice == null ? null : new Money(minPrice);
            return this;
        }

        public Builder maxPrice(BigDecimal maxPrice) {
            this.maxPrice = maxPrice == null ? null : new Money(maxPrice);
            return this;
        }

        public Builder brand(String brand) {
            this.brand = brand == null || brand.isBlank() ? null : brand.trim();
            return this;
        }

        public Builder minRating(Integer minRating) {
            this.minRating = minRating == null ? null : new Rating(minRating);
            return this;
        }

        public Builder inStockOnly(boolean inStockOnly) {
            this.inStockOnly = inStockOnly;
            return this;
        }

        public ProductFilter build() {
            return new ProductFilter(this);
        }
    }
}
