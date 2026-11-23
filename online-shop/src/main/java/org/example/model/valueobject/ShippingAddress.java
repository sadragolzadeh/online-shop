package org.example.model.valueobject;

import org.example.model.exception.ValidationException;
import org.example.model.validation.Validations;

/** Immutable delivery details; also used as the address snapshot stored on an order. */
public record ShippingAddress(String recipientName, PhoneNumber phone, String country,
                              String city, String street, PostalCode postalCode) {

    public ShippingAddress {
        recipientName = Validations.requireNotBlank(recipientName, "Recipient name");
        country = Validations.requireNotBlank(country, "Country");
        city = Validations.requireNotBlank(city, "City");
        street = Validations.requireNotBlank(street, "Address");
        if (phone == null) {
            throw new ValidationException("Phone number must not be empty.");
        }
        if (postalCode == null) {
            throw new ValidationException("Postal code must not be empty.");
        }
    }

    public String asSingleLine() {
        return country + ", " + city + ", " + street + " (postal code: " + postalCode + ")";
    }
}
