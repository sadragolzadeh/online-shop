package org.example.cli;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import org.example.model.entity.Product;
import org.example.model.enums.ProductSortOption;
import org.example.model.exception.ValidationException;
import org.example.model.valueobject.ProductFilter;
import org.example.service.AuthService;
import org.example.service.CartService;
import org.example.service.CategoryService;
import org.example.service.ProductService;
import org.example.service.ReviewService;

/** Browsing the catalog (open to guests) plus review/rating/add-to-cart for logged-in customers. */
public class CatalogMenu extends Menu {

    private static final String NO_PRODUCTS = "No products found.";

    private final EntityPrinter printer;
    private final AuthService authService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final CartService cartService;

    public CatalogMenu(ConsoleIO io, EntityPrinter printer, AuthService authService,
                       ProductService productService, CategoryService categoryService,
                       ReviewService reviewService, CartService cartService) {
        super(io);
        this.printer = printer;
        this.authService = authService;
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.cartService = cartService;
    }

    @Override
    public void open() {
        loop("Shop", List.of(
                new MenuItem("List products", this::listProducts),
                new MenuItem("Categories", this::listCategories),
                new MenuItem("Products of a category", this::listProductsOfCategory),
                new MenuItem("Product details", this::showProductDetails),
                new MenuItem("Search products", this::search),
                new MenuItem("Filter products", this::filter),
                new MenuItem("Sort products", this::sort),
                new MenuItem("Add product to cart", this::addToCart),
                new MenuItem("Write a product review", this::addReview),
                new MenuItem("Rate a product", this::rateProduct)));
    }

    private void listProducts() {
        printer.printProducts(productService.getActiveProducts(), "There are no active products at the moment.");
    }

    private void listCategories() {
        printer.printCategories(categoryService.getActiveCategories());
    }

    private void listProductsOfCategory() {
        listCategories();
        String categoryId = io.readLine("Category ID");
        printer.printProducts(productService.getProductsByCategory(categoryId), "There are no products in this category.");
    }

    private void showProductDetails() {
        Product product = productService.getProduct(io.readLine("Product ID"));
        printer.printProductDetails(product, reviewService.getReviews(product.getId()));
    }

    private void search() {
        String query = io.readLine("Search term (max 100 characters)");
        printer.printProducts(productService.searchProducts(query), NO_PRODUCTS + " Try a different search term.");
    }

    private void filter() {
        ProductFilter filter = ProductFilter.builder()
                .categoryId(io.readOptional("Category ID", Function.identity()))
                .minPrice(io.readOptional("Minimum price", BigDecimal::new))
                .maxPrice(io.readOptional("Maximum price", BigDecimal::new))
                .brand(io.readOptional("Brand", Function.identity()))
                .minRating(io.readOptional("Minimum rating (1 to 5)", Integer::parseInt))
                .inStockOnly(io.confirm("In-stock products only?"))
                .build();
        printer.printProducts(productService.filterProducts(filter), NO_PRODUCTS);
    }

    private void sort() {
        ProductSortOption[] options = ProductSortOption.values();
        for (int i = 0; i < options.length; i++) {
            io.println((i + 1) + ". " + options[i].getLabel());
        }
        int choice = io.readInt("Sort option number");
        if (choice < 1 || choice > options.length) {
            throw new ValidationException("Only the displayed options can be chosen.");
        }
        List<Product> sorted = productService.sortProducts(productService.getActiveProducts(), options[choice - 1]);
        printer.printProducts(sorted, NO_PRODUCTS);
    }

    private void addToCart() {
        String userId = authService.requireCurrentUser().getId();
        String productId = io.readLine("Product ID");
        int quantity = io.readInt("Quantity");
        cartService.addProduct(userId, productId, quantity);
        io.printSuccess("Product added to cart.");
    }

    private void addReview() {
        String userId = authService.requireCurrentUser().getId();
        String productId = io.readLine("Product ID");
        String text = io.readLine("Review text (max 500 characters)");
        reviewService.addReview(userId, productId, text);
        io.printSuccess("Your review was saved.");
    }

    private void rateProduct() {
        String userId = authService.requireCurrentUser().getId();
        String productId = io.readLine("Product ID");
        int score = io.readInt("Rating (1 to 5)");
        reviewService.rateProduct(userId, productId, score);
        io.printSuccess("Your rating was saved.");
    }
}
