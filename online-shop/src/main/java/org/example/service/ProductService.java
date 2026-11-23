package org.example.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.example.model.entity.Category;
import org.example.model.entity.Product;
import org.example.model.entity.User;
import org.example.model.enums.ProductSortOption;
import org.example.model.exception.NotFoundException;
import org.example.model.validation.Validations;
import org.example.model.valueobject.Money;
import org.example.model.valueobject.ProductFilter;
import org.example.service.dto.ProductRequest;

/** Product catalog: customer browsing (search/filter/sort) and admin management. */
public class ProductService {

    private static final int MAX_SEARCH_LENGTH = 100;

    private final Map<String, Product> products = new LinkedHashMap<>();
    private final IdGenerator idGenerator = new IdGenerator("P");
    private final CategoryService categoryService;

    public ProductService(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    // ----- admin use cases -----

    public Product createProduct(User admin, ProductRequest request) {
        admin.ensureAdmin();
        Category category = categoryService.getActiveCategory(request.categoryId());
        Product product = new Product(idGenerator.next(), request.name(), request.description(),
                new Money(request.price()), request.stockQuantity(), category, request.brand(),
                request.imageUrls());
        products.put(product.getId(), product);
        return product;
    }

    public Product updateProduct(User admin, String productId, ProductRequest request) {
        admin.ensureAdmin();
        Product product = getProduct(productId);
        Category category = categoryService.getActiveCategory(request.categoryId());
        product.update(request.name(), request.description(), new Money(request.price()),
                request.stockQuantity(), category, request.brand(), request.imageUrls());
        return product;
    }

    /** Deactivates (never deletes) a product so past orders stay intact. */
    public Product deactivateProduct(User admin, String productId) {
        admin.ensureAdmin();
        Product product = getProduct(productId);
        product.deactivate();
        return product;
    }

    public List<Product> getAllProducts(User admin) {
        admin.ensureAdmin();
        return List.copyOf(products.values());
    }

    // ----- customer use cases -----

    public Product getProduct(String productId) {
        Product product = productId == null ? null : products.get(productId.trim());
        if (product == null) {
            throw new NotFoundException("The requested product does not exist.");
        }
        return product;
    }

    public List<Product> getActiveProducts() {
        return activeProducts().toList();
    }

    public List<Product> getProductsByCategory(String categoryId) {
        Category category = categoryService.getActiveCategory(categoryId);
        return activeProducts().filter(product -> product.getCategory().equals(category)).toList();
    }

    public List<Product> searchProducts(String query) {
        String keyword = Validations.requireNotBlank(query, "Search term");
        Validations.requireMaxLength(keyword, MAX_SEARCH_LENGTH, "Search term");
        return activeProducts().filter(product -> product.matchesKeyword(keyword)).toList();
    }

    public List<Product> filterProducts(ProductFilter filter) {
        filter.getCategoryId().ifPresent(categoryService::getActiveCategory);
        return activeProducts().filter(filter::matches).toList();
    }

    public List<Product> sortProducts(List<Product> source, ProductSortOption option) {
        Comparator<Product> comparator = option.getComparator();
        return source.stream().sorted(comparator).toList();
    }

    private Stream<Product> activeProducts() {
        return products.values().stream().filter(Product::isActive);
    }
}
