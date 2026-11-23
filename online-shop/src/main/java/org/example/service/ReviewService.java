package org.example.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.model.entity.Product;
import org.example.model.entity.ProductRating;
import org.example.model.entity.Review;
import org.example.model.entity.User;
import org.example.model.exception.AccessDeniedException;
import org.example.model.valueobject.Rating;

/** Reviews and ratings; only customers who bought a product may review or rate it. */
public class ReviewService {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;
    private final List<Review> reviews = new ArrayList<>();
    private final Map<String, ProductRating> ratings = new HashMap<>();
    private final IdGenerator idGenerator = new IdGenerator("R");

    public ReviewService(ProductService productService, OrderService orderService, UserService userService) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
    }

    public Review addReview(String userId, String productId, String text) {
        User user = userService.getUser(userId);
        Product product = productService.getProduct(productId);
        ensurePurchased(userId, productId);
        Review review = new Review(idGenerator.next(), product.getId(), userId, firstNameOf(user), text);
        reviews.add(review);
        return review;
    }

    /** Records a score; a customer rating the same product again replaces the previous score. */
    public void rateProduct(String userId, String productId, int score) {
        Rating rating = new Rating(score);
        Product product = productService.getProduct(productId);
        ensurePurchased(userId, productId);
        String key = userId + "|" + productId;
        ProductRating existing = ratings.get(key);
        if (existing == null) {
            ratings.put(key, new ProductRating(productId, userId, rating));
            product.addRating(rating);
        } else {
            product.replaceRating(existing.getRating(), rating);
            existing.changeTo(rating);
        }
    }

    public List<Review> getReviews(String productId) {
        productService.getProduct(productId);
        return reviews.stream()
                .filter(review -> review.getProductId().equals(productId))
                .sorted(Comparator.comparing(Review::getCreatedAt).reversed())
                .toList();
    }

    private void ensurePurchased(String userId, String productId) {
        if (!orderService.hasUserPurchased(userId, productId)) {
            throw new AccessDeniedException("Only customers who bought this product can review or rate it.");
        }
    }

    /** Only the first name is shown publicly so personal data stays private. */
    private static String firstNameOf(User user) {
        return user.getFullName().split("\\s+")[0];
    }
}
