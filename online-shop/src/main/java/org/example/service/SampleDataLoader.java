package org.example.service;

import java.math.BigDecimal;
import java.util.List;
import org.example.model.entity.Category;
import org.example.model.entity.User;
import org.example.model.enums.UserRole;
import org.example.service.dto.ProductRequest;

/** Seeds the in-memory store with an admin, a demo customer, categories and products. */
public class SampleDataLoader {

    public static final String ADMIN_EMAIL = "admin@shop.com";
    public static final String ADMIN_PASSWORD = "Admin@1234";
    public static final String CUSTOMER_EMAIL = "ali@shop.com";
    public static final String CUSTOMER_PASSWORD = "Customer@1234";

    private final UserService userService;
    private final PasswordHasher passwordHasher;
    private final CategoryService categoryService;
    private final ProductService productService;

    public SampleDataLoader(UserService userService, PasswordHasher passwordHasher,
                            CategoryService categoryService, ProductService productService) {
        this.userService = userService;
        this.passwordHasher = passwordHasher;
        this.categoryService = categoryService;
        this.productService = productService;
    }

    public void load() {
        User admin = userService.createUser("Shop Admin", ADMIN_EMAIL, null,
                passwordHasher.hash(ADMIN_PASSWORD), UserRole.ADMIN);
        userService.createUser("Ali Rezaei", CUSTOMER_EMAIL, "09121234567",
                passwordHasher.hash(CUSTOMER_PASSWORD), UserRole.CUSTOMER);

        Category laptops = categoryService.createCategory(admin, "Laptops");
        Category phones = categoryService.createCategory(admin, "Mobile Phones");
        Category appliances = categoryService.createCategory(admin, "Home Appliances");

        addProduct(admin, "Asus ZenBook Laptop", "Lightweight 14-inch laptop", 45_000_000, 5, laptops, "Asus");
        addProduct(admin, "Lenovo ThinkPad Laptop", "Laptop for work and programming", 52_000_000, 3, laptops, "Lenovo");
        addProduct(admin, "Samsung Galaxy A55 Phone", "Mid-range smartphone", 18_000_000, 10, phones, "Samsung");
        addProduct(admin, "Xiaomi Redmi Note 13 Phone", "Budget phone with a strong battery", 11_000_000, 0, phones, "Xiaomi");
        addProduct(admin, "Philips Vacuum Cleaner", "2000 W vacuum cleaner", 9_500_000, 7, appliances, "Philips");
    }

    private void addProduct(User admin, String name, String description, long price, int stock,
                            Category category, String brand) {
        productService.createProduct(admin, new ProductRequest(name, description,
                BigDecimal.valueOf(price), stock, category.getId(), brand, List.of()));
    }
}
