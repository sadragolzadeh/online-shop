package org.example.cli;

import java.util.List;
import org.example.service.AuthService;
import org.example.service.CartService;

/** The shopping cart of the logged-in customer. */
public class CartMenu extends Menu {

    private final EntityPrinter printer;
    private final AuthService authService;
    private final CartService cartService;

    public CartMenu(ConsoleIO io, EntityPrinter printer, AuthService authService, CartService cartService) {
        super(io);
        this.printer = printer;
        this.authService = authService;
        this.cartService = cartService;
    }

    @Override
    public void open() {
        loop("Shopping cart", List.of(
                new MenuItem("View cart", this::showCart),
                new MenuItem("Add product", this::addProduct),
                new MenuItem("Change product quantity", this::changeQuantity),
                new MenuItem("Remove product from cart", this::removeProduct),
                new MenuItem("Empty the cart", this::clearCart)));
    }

    private String userId() {
        return authService.requireCurrentUser().getId();
    }

    private void showCart() {
        printer.printCart(cartService.getCart(userId()));
    }

    private void addProduct() {
        String productId = io.readLine("Product ID");
        int quantity = io.readInt("Quantity");
        printer.printCart(cartService.addProduct(userId(), productId, quantity));
    }

    private void changeQuantity() {
        String productId = io.readLine("Product ID");
        int quantity = io.readInt("New quantity");
        printer.printCart(cartService.changeQuantity(userId(), productId, quantity));
    }

    private void removeProduct() {
        String productId = io.readLine("Product ID");
        printer.printCart(cartService.removeProduct(userId(), productId));
    }

    private void clearCart() {
        if (io.confirm("Remove all products from the cart?")) {
            printer.printCart(cartService.clear(userId()));
        }
    }
}
