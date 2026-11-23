package org.example.service.dto;

import java.math.BigDecimal;
import java.util.List;

/** Raw product data entered by an admin. */
public record ProductRequest(String name, String description, BigDecimal price, int stockQuantity,
                             String categoryId, String brand, List<String> imageUrls) {
}
