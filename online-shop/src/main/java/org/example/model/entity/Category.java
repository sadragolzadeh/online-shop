package org.example.model.entity;

import org.example.model.validation.Validations;

public class Category {

    private final String id;
    private String name;
    private boolean active;

    public Category(String id, String name) {
        this.id = Validations.requireNotBlank(id, "Category ID");
        this.name = Validations.requireNotBlank(name, "Category name");
        this.active = true;
    }

    public void rename(String newName) {
        this.name = Validations.requireNotBlank(newName, "Category name");
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public boolean isActive() {
        return active;
    }
}
