package org.example.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.example.model.entity.Address;
import org.example.model.entity.Cart;
import org.example.model.entity.CartItem;
import org.example.model.entity.Order;
import org.example.model.entity.User;
import org.example.model.enums.OrderStatus;
import org.example.model.exception.NotFoundException;
import org.example.model.valueobject.OrderItem;

/** Checkout, order history and admin order management. */
public class OrderService {

    private final Map<String, Order> orders = new LinkedHashMap<>();
    private final IdGenerator idGenerator = new IdGenerator("O");
    private final CartService cartService;
    private final UserService userService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    public OrderService(CartService cartService, UserService userService,
                        ProductService productService, InventoryService inventoryService) {
        this.cartService = cartService;
        this.userService = userService;
        this.productService = productService;
        this.inventoryService = inventoryService;
    }

    /** Starts checkout: validates the cart and returns it so the final price can be shown. */
    public Cart startCheckout(String userId) {
        Cart cart = cartService.getCart(userId);
        cart.ensureReadyForCheckout();
        return cart;
    }

    /** Turns the cart into an order awaiting payment, shipped to one of the user's own addresses. */
    public Order placeOrder(String userId, String addressId) {
        Cart cart = startCheckout(userId);
        User user = userService.getUser(userId);
        Address address = user.getAddress(addressId);
        List<OrderItem> items = cart.getItems().stream().map(OrderService::toOrderItem).toList();
        Order order = new Order(idGenerator.next(), userId, items, address.getDetails());
        orders.put(order.getId(), order);
        cart.clear();
        return order;
    }

    public List<Order> getOrdersOfUser(String userId) {
        return orders.values().stream()
                .filter(order -> order.belongsTo(userId))
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .toList();
    }

    public Order getOrderOfUser(String userId, String orderId) {
        Order order = findOrder(orderId);
        order.ensureBelongsTo(userId);
        return order;
    }

    public boolean hasUserPurchased(String userId, String productId) {
        return orders.values().stream()
                .filter(order -> order.belongsTo(userId))
                .filter(order -> order.getStatus().isPaidState())
                .anyMatch(order -> order.containsProduct(productId));
    }

    /** Admin view of all orders, optionally restricted to one status ({@code null} = all). */
    public List<Order> getAllOrders(User admin, OrderStatus statusFilter) {
        admin.ensureAdmin();
        return orders.values().stream()
                .filter(order -> statusFilter == null || order.getStatus() == statusFilter)
                .sorted(Comparator.comparing(Order::getCreatedAt).reversed())
                .toList();
    }

    public Order getOrderForAdmin(User admin, String orderId) {
        admin.ensureAdmin();
        return findOrder(orderId);
    }

    /** Re-checks that every product of the order can still be bought (used right before payment). */
    public void ensureOrderItemsPurchasable(Order order) {
        order.getItems().forEach(item ->
                productService.getProduct(item.productId()).ensureCanBePurchased(item.quantity()));
    }

    /** Marks the order as paid and removes the sold quantities from the stock. */
    public void completePayment(Order order) {
        order.markAsPaid();
        order.getItems().forEach(item -> inventoryService.recordSale(item.productId(), item.quantity()));
    }

    private Order findOrder(String orderId) {
        Order order = orderId == null ? null : orders.get(orderId.trim());
        if (order == null) {
            throw new NotFoundException("The requested order does not exist.");
        }
        return order;
    }

    private static OrderItem toOrderItem(CartItem cartItem) {
        return new OrderItem(cartItem.getProduct().getId(), cartItem.getProduct().getName(),
                cartItem.getProduct().getPrice(), cartItem.getQuantity());
    }
}
