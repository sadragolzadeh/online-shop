package org.example.model.entity;

import org.example.model.validation.Validations;
import org.example.model.valueobject.ShippingAddress;

/** An address saved in a user's address book. */
public class Address {

    private final String id;
    private String title;
    private ShippingAddress details;
    private boolean defaultAddress;

    public Address(String id, String title, ShippingAddress details, boolean defaultAddress) {
        this.id = Validations.requireNotBlank(id, "Address ID");
        this.title = Validations.requireNotBlank(title, "Address title");
        this.details = details;
        this.defaultAddress = defaultAddress;
    }

    public void update(String newTitle, ShippingAddress newDetails) {
        this.title = Validations.requireNotBlank(newTitle, "Address title");
        this.details = newDetails;
    }

    public void markAsDefault() {
        defaultAddress = true;
    }

    public void unmarkAsDefault() {
        defaultAddress = false;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public ShippingAddress getDetails() {
        return details;
    }

    public boolean isDefaultAddress() {
        return defaultAddress;
    }
}
