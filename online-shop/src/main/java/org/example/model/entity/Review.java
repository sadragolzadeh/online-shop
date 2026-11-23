package org.example.model.entity;

import java.time.LocalDateTime;
import org.example.model.validation.Validations;

/** A written opinion about a product. Only a display name (never contact data) is exposed. */
public class Review {

    public static final int MAX_TEXT_LENGTH = 500;

    private final String id;
    private final String productId;
    private final String userId;
    private final String authorDisplayName;
    private final String text;
    private final LocalDateTime createdAt;

    public Review(String id, String productId, String userId, String authorDisplayName, String text) {
        String validText = Validations.requireNotBlank(text, "Review text");
        this.id = id;
        this.productId = productId;
        this.userId = userId;
        this.authorDisplayName = authorDisplayName;
        this.text = Validations.requireMaxLength(validText, MAX_TEXT_LENGTH, "Review text");
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getProductId() {
        return productId;
    }

    public String getUserId() {
        return userId;
    }

    public String getAuthorDisplayName() {
        return authorDisplayName;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
