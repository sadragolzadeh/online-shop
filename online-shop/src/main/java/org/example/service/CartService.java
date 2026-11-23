package org.example.service;

import java.util.HashMap;
import java.util.Map;
import org.example.model.entity.Cart;
import org.example.model.entity.Product;

/** Each customer can only reach his own cart: carts are looked up by the logged-in user's id. */
public class CartService {

    private final Map<String, Cart> carts = new HashMap<>();
    private final ProductService productService;

    public CartService(ProductService productService) {
        this.productService = productService;
    }

    public Cart getCart(String userId) {
        return carts.computeIfAbsent(userId, Cart::new);
    }

    public Cart addProduct(String userId, String productId, int quantity) {
        Product product = productService.getProduct(productId);
        Cart cart = getCart(userId);
        cart.addProduct(product, quantity);
        return cart;
    }

    public Cart changeQuantity(String userId, String productId, int quantity) {
        Cart cart = getCart(userId);
        cart.changeQuantity(productId, quantity);
        return cart;
    }

    public Cart removeProduct(String userId, String productId) {
        Cart cart = getCart(userId);
        cart.removeProduct(productId);
        return cart;
    }

    public Cart clear(String userId) {
        Cart cart = getCart(userId);
        cart.clear();
        return cart;
    }
}
