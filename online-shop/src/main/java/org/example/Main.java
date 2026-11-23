package org.example;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.example.cli.AdminMenu;
import org.example.cli.AuthMenu;
import org.example.cli.CartMenu;
import org.example.cli.CatalogMenu;
import org.example.cli.CliApplication;
import org.example.cli.ConsoleIO;
import org.example.cli.EntityPrinter;
import org.example.cli.InputClosedException;
import org.example.cli.OrderMenu;
import org.example.cli.ProfileMenu;
import org.example.service.AddressService;
import org.example.service.AuthService;
import org.example.service.CartService;
import org.example.service.CategoryService;
import org.example.service.InventoryService;
import org.example.service.OrderService;
import org.example.service.Pbkdf2PasswordHasher;
import org.example.service.PasswordHasher;
import org.example.service.PaymentService;
import org.example.service.ProductService;
import org.example.service.ReviewService;
import org.example.service.SampleDataLoader;
import org.example.service.UserService;
import org.example.service.payment.SimulatedPaymentGateway;

/** Composition root: wires the layers together and starts the terminal application. */
public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        ConsoleIO io = new ConsoleIO(System.in, new PrintStream(System.out, true, StandardCharsets.UTF_8));
        EntityPrinter printer = new EntityPrinter(io);

        PasswordHasher passwordHasher = new Pbkdf2PasswordHasher();
        UserService userService = new UserService();
        AuthService authService = new AuthService(userService, passwordHasher);
        AddressService addressService = new AddressService(userService);
        CategoryService categoryService = new CategoryService();
        ProductService productService = new ProductService(categoryService);
        InventoryService inventoryService = new InventoryService(productService);
        CartService cartService = new CartService(productService);
        OrderService orderService = new OrderService(cartService, userService, productService, inventoryService);
        PaymentService paymentService = new PaymentService(orderService, new SimulatedPaymentGateway());
        ReviewService reviewService = new ReviewService(productService, orderService, userService);

        new SampleDataLoader(userService, passwordHasher, categoryService, productService).load();

        CliApplication application = new CliApplication(io, authService,
                new AuthMenu(io, authService),
                new CatalogMenu(io, printer, authService, productService, categoryService, reviewService, cartService),
                new ProfileMenu(io, printer, authService, userService, addressService),
                new CartMenu(io, printer, authService, cartService),
                new OrderMenu(io, printer, authService, addressService, orderService, paymentService),
                new AdminMenu(io, printer, authService, productService, categoryService, inventoryService, orderService));
        try {
            application.open();
        } catch (InputClosedException e) {
            io.println();
            io.println("Input closed; the program has ended.");
        }
    }
}
