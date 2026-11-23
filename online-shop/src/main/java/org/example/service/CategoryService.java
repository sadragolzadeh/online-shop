package org.example.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.example.model.entity.Category;
import org.example.model.entity.User;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;

public class CategoryService {

    private final Map<String, Category> categories = new LinkedHashMap<>();
    private final IdGenerator idGenerator = new IdGenerator("C");

    public Category createCategory(User admin, String name) {
        admin.ensureAdmin();
        Category category = new Category(idGenerator.next(), name);
        categories.put(category.getId(), category);
        return category;
    }

    public List<Category> getActiveCategories() {
        return categories.values().stream().filter(Category::isActive).toList();
    }

    /** Returns an existing, active category or fails with a descriptive error. */
    public Category getActiveCategory(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            throw new ValidationException("A category must be specified.");
        }
        Category category = categories.get(categoryId.trim());
        if (category == null || !category.isActive()) {
            throw new NotFoundException("The requested category does not exist.");
        }
        return category;
    }
}
