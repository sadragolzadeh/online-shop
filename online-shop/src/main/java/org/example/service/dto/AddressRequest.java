package org.example.service.dto;

import org.example.model.valueobject.PhoneNumber;
import org.example.model.valueobject.PostalCode;
import org.example.model.valueobject.ShippingAddress;

/** Raw (unvalidated) address data coming from the presentation layer. */
public record AddressRequest(String title, String recipientName, String phone, String country,
                             String city, String street, String postalCode, boolean makeDefault) {

    public ShippingAddress toShippingAddress() {
        return new ShippingAddress(recipientName, new PhoneNumber(phone), country, city, street,
                new PostalCode(postalCode));
    }
}
