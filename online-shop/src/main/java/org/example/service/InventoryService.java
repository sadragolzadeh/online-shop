package org.example.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.example.model.entity.Product;
import org.example.model.entity.StockChange;
import org.example.model.entity.User;
import org.example.model.enums.StockChangeReason;
import org.example.model.exception.ValidationException;

/** Stock queries and changes; every change is recorded in an audit history. */
public class InventoryService {

    private static final String SYSTEM_ACTOR = "SYSTEM";

    private final ProductService productService;
    private final List<StockChange> history = new ArrayList<>();
    private final IdGenerator idGenerator = new IdGenerator("S");

    public InventoryService(ProductService productService) {
        this.productService = productService;
    }

    public List<Product> getInventory(User admin) {
        return productService.getAllProducts(admin).stream()
                .sorted(Comparator.comparing(Product::getName))
                .toList();
    }

    /** Increases (positive delta) or decreases (negative delta) the stock of a product. */
    public StockChange adjustStock(User admin, String productId, int delta) {
        admin.ensureAdmin();
        if (delta == 0) {
            throw new ValidationException("The stock change amount cannot be zero.");
        }
        return applyChange(productId, delta, StockChangeReason.MANUAL_ADJUSTMENT, admin.getId());
    }

    /** Called by the order flow once an order has been paid. */
    public StockChange recordSale(String productId, int quantity) {
        Product product = productService.getProduct(productId);
        StockChange change = applyChange(productId, -quantity, StockChangeReason.ORDER_PAID, SYSTEM_ACTOR);
        product.increaseSalesCount(quantity);
        return change;
    }

    public List<StockChange> getHistory(User admin, String productId) {
        admin.ensureAdmin();
        productService.getProduct(productId);
        return history.stream().filter(change -> change.getProductId().equals(productId)).toList();
    }

    private StockChange applyChange(String productId, int delta, StockChangeReason reason, String actor) {
        Product product = productService.getProduct(productId);
        int previousQuantity = product.getStockQuantity();
        product.changeStock(delta);
        StockChange change = new StockChange(idGenerator.next(), productId, previousQuantity,
                product.getStockQuantity(), reason, actor);
        history.add(change);
        return change;
    }
}
