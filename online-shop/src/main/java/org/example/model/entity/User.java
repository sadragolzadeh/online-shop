package org.example.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.example.model.enums.UserRole;
import org.example.model.exception.AccessDeniedException;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.model.validation.Validations;
import org.example.model.valueobject.Email;
import org.example.model.valueobject.PhoneNumber;

public class User {

    private final String id;
    private String fullName;
    private Email email;
    private PhoneNumber phone;
    private String passwordHash;
    private final UserRole role;
    private boolean active;
    private final LocalDateTime createdAt;
    private final List<Address> addresses = new ArrayList<>();

    public User(String id, String fullName, Email email, PhoneNumber phone, String passwordHash, UserRole role) {
        requireAtLeastOneContact(email, phone);
        this.id = Validations.requireNotBlank(id, "User ID");
        this.fullName = Validations.requireNotBlank(fullName, "Name");
        this.email = email;
        this.phone = phone;
        this.passwordHash = Validations.requireNotBlank(passwordHash, "Password");
        this.role = role;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }

    public void updateProfile(String newFullName, Email newEmail, PhoneNumber newPhone) {
        requireAtLeastOneContact(newEmail, newPhone);
        this.fullName = Validations.requireNotBlank(newFullName, "Name");
        this.email = newEmail;
        this.phone = newPhone;
    }

    public void changePasswordHash(String newPasswordHash) {
        this.passwordHash = Validations.requireNotBlank(newPasswordHash, "Password");
    }

    public void activate() {
        active = true;
    }

    public void deactivate() {
        active = false;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public void ensureAdmin() {
        if (!isAdmin()) {
            throw new AccessDeniedException("This operation is only allowed for admins.");
        }
    }

    public boolean hasEmail(Email candidate) {
        return email != null && email.equals(candidate);
    }

    public boolean hasPhone(PhoneNumber candidate) {
        return phone != null && phone.equals(candidate);
    }

    public void addAddress(Address address) {
        if (addresses.isEmpty()) {
            address.markAsDefault();
        } else if (address.isDefaultAddress()) {
            clearDefaultAddress();
        }
        addresses.add(address);
    }

    public Address getAddress(String addressId) {
        return addresses.stream()
                .filter(address -> address.getId().equals(addressId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("The requested address does not exist in your account."));
    }

    public void removeAddress(String addressId) {
        Address address = getAddress(addressId);
        addresses.remove(address);
        if (address.isDefaultAddress() && !addresses.isEmpty()) {
            addresses.get(0).markAsDefault();
        }
    }

    public void setDefaultAddress(String addressId) {
        Address newDefault = getAddress(addressId);
        clearDefaultAddress();
        newDefault.markAsDefault();
    }

    private void clearDefaultAddress() {
        addresses.forEach(Address::unmarkAsDefault);
    }

    private static void requireAtLeastOneContact(Email email, PhoneNumber phone) {
        if (email == null && phone == null) {
            throw new ValidationException("An email or a mobile number is required.");
        }
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public Optional<Email> getEmail() {
        return Optional.ofNullable(email);
    }

    public Optional<PhoneNumber> getPhone() {
        return Optional.ofNullable(phone);
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }
}
