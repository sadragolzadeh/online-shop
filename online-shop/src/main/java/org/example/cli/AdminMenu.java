package org.example.cli;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import org.example.model.entity.Product;
import org.example.model.entity.User;
import org.example.model.enums.OrderStatus;
import org.example.model.exception.ValidationException;
import org.example.service.AuthService;
import org.example.service.CategoryService;
import org.example.service.InventoryService;
import org.example.service.OrderService;
import org.example.service.ProductService;
import org.example.service.dto.ProductRequest;

/** Admin panel: products, inventory and all orders. */
public class AdminMenu extends Menu {

    private final EntityPrinter printer;
    private final AuthService authService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final InventoryService inventoryService;
    private final OrderService orderService;

    public AdminMenu(ConsoleIO io, EntityPrinter printer, AuthService authService, ProductService productService,
                     CategoryService categoryService, InventoryService inventoryService, OrderService orderService) {
        super(io);
        this.printer = printer;
        this.authService = authService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.inventoryService = inventoryService;
        this.orderService = orderService;
    }

    @Override
    public void open() {
        loop("Admin Panel", List.of(
                new MenuItem("Create category", this::createCategory),
                new MenuItem("Create product", this::createProduct),
                new MenuItem("Edit product", this::editProduct),
                new MenuItem("Deactivate product", this::deactivateProduct),
                new MenuItem("View product stock", this::showInventory),
                new MenuItem("Change product stock", this::adjustStock),
                new MenuItem("Stock change history", this::showStockHistory),
                new MenuItem("View orders (filter by status)", this::listOrders),
                new MenuItem("Order details", this::showOrderDetails)));
    }

    private User admin() {
        User user = authService.requireCurrentUser();
        user.ensureAdmin();
        return user;
    }

    private void createCategory() {
        categoryService.createCategory(admin(), io.readLine("Category name"));
        io.printSuccess("Category created.");
    }

    private void createProduct() {
        User admin = admin();
        printer.printCategories(categoryService.getActiveCategories());
        Product product = productService.createProduct(admin, readProductRequest(null));
        io.printSuccess("Product with ID " + product.getId() + " created.");
    }

    private void editProduct() {
        User admin = admin();
        Product current = productService.getProduct(io.readLine("Product ID"));
        printer.printCategories(categoryService.getActiveCategories());
        productService.updateProduct(admin, current.getId(), readProductRequest(current));
        io.printSuccess("Product updated.");
    }

    private void deactivateProduct() {
        User admin = admin();
        Product product = productService.deactivateProduct(admin, io.readLine("Product ID"));
        io.printSuccess("Product '" + product.getName() + "' was deactivated; previous orders are kept.");
    }

    private void showInventory() {
        printer.printInventory(inventoryService.getInventory(admin()));
    }

    private void adjustStock() {
        User admin = admin();
        String productId = io.readLine("Product ID");
        int amount = io.readInt("Change amount (positive = increase, negative = decrease)");
        var change = inventoryService.adjustStock(admin, productId, amount);
        io.printSuccess("Stock changed from " + change.getPreviousQuantity() + " to " + change.getNewQuantity() + ".");
    }

    private void showStockHistory() {
        User admin = admin();
        printer.printStockChanges(inventoryService.getHistory(admin, io.readLine("Product ID")));
    }

    private void listOrders() {
        User admin = admin();
        OrderStatus[] statuses = OrderStatus.values();
        for (int i = 0; i < statuses.length; i++) {
            io.println((i + 1) + ". " + statuses[i].getLabel());
        }
        Integer choice = io.readOptional("Status number to filter by", Integer::parseInt);
        OrderStatus status = null;
        if (choice != null) {
            if (choice < 1 || choice > statuses.length) {
                throw new ValidationException("The selected status is invalid.");
            }
            status = statuses[choice - 1];
        }
        printer.printOrders(orderService.getAllOrders(admin, status));
    }

    private void showOrderDetails() {
        User admin = admin();
        printer.printOrderDetails(orderService.getOrderForAdmin(admin, io.readLine("Order ID")));
    }

    /** With {@code current == null} every value is required; otherwise Enter keeps the current value. */
    private ProductRequest readProductRequest(Product current) {
        String name = askText("Product name", current == null ? null : current.getName());
        String description = askText("Description", current == null ? null : current.getDescription());
        BigDecimal price = askDecimal("Price", current == null ? null : current.getPrice().amount());
        int stock = askInt("Stock", current == null ? null : current.getStockQuantity());
        String categoryId = askText("Category ID", current == null ? null : current.getCategory().getId());
        String brand = askText("Brand", current == null ? null : current.getBrand());
        String images = askText("Image URLs (comma-separated)", current == null ? "" : String.join(",", current.getImageUrls()));
        return new ProductRequest(name, description, price, stock, categoryId, brand, splitImages(images));
    }

    private String askText(String label, String current) {
        if (current == null) {
            return io.readLine(label);
        }
        String input = io.readLine(label + " [" + current + "] (Enter = keep current)");
        return input.isEmpty() ? current : input;
    }

    private BigDecimal askDecimal(String label, BigDecimal current) {
        if (current == null) {
            return io.readDecimal(label);
        }
        BigDecimal input = io.readOptional(label + " [" + current.toPlainString() + "]", BigDecimal::new);
        return input == null ? current : input;
    }

    private int askInt(String label, Integer current) {
        if (current == null) {
            return io.readInt(label);
        }
        Integer input = io.readOptional(label + " [" + current + "]", Integer::parseInt);
        return input == null ? current : input;
    }

    private static List<String> splitImages(String raw) {
        return Arrays.stream(raw.split(",")).map(String::trim).filter(url -> !url.isEmpty()).toList();
    }
}
