package org.example.model.entity;

import java.time.LocalDateTime;
import org.example.model.valueobject.Rating;

/** The score one customer gave to one product (at most one per customer and product). */
public class ProductRating {

    private final String productId;
    private final String userId;
    private Rating rating;
    private LocalDateTime ratedAt;

    public ProductRating(String productId, String userId, Rating rating) {
        this.productId = productId;
        this.userId = userId;
        this.rating = rating;
        this.ratedAt = LocalDateTime.now();
    }

    public void changeTo(Rating newRating) {
        this.rating = newRating;
        this.ratedAt = LocalDateTime.now();
    }

    public String getProductId() {
        return productId;
    }

    public String getUserId() {
        return userId;
    }

    public Rating getRating() {
        return rating;
    }

    public LocalDateTime getRatedAt() {
        return ratedAt;
    }
}
