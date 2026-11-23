package org.example.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.example.model.entity.User;
import org.example.model.enums.UserRole;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.model.valueobject.Email;
import org.example.model.valueobject.PhoneNumber;

/** Owns the users and guarantees e-mail / phone uniqueness. */
public class UserService {

    private final Map<String, User> users = new LinkedHashMap<>();
    private final IdGenerator idGenerator = new IdGenerator("U");

    public User createUser(String fullName, String email, String phone, String passwordHash, UserRole role) {
        Email parsedEmail = parseEmail(email);
        PhoneNumber parsedPhone = parsePhone(phone);
        ensureContactIsUnique(parsedEmail, parsedPhone, null);
        User user = new User(idGenerator.next(), fullName, parsedEmail, parsedPhone, passwordHash, role);
        users.put(user.getId(), user);
        return user;
    }

    public User getUser(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new NotFoundException("The requested user was not found.");
        }
        return user;
    }

    /** Finds a user by e-mail or mobile number; malformed identifiers simply match nobody. */
    public Optional<User> findByIdentifier(String identifier) {
        if (identifier == null || identifier.isBlank()) {
            return Optional.empty();
        }
        try {
            if (identifier.contains("@")) {
                Email email = new Email(identifier);
                return users.values().stream().filter(user -> user.hasEmail(email)).findFirst();
            }
            PhoneNumber phone = new PhoneNumber(identifier);
            return users.values().stream().filter(user -> user.hasPhone(phone)).findFirst();
        } catch (ValidationException e) {
            return Optional.empty();
        }
    }

    public User updateProfile(String userId, String fullName, String email, String phone) {
        User user = getUser(userId);
        Email newEmail = parseEmail(email);
        PhoneNumber newPhone = parsePhone(phone);
        ensureContactIsUnique(newEmail, newPhone, userId);
        user.updateProfile(fullName, newEmail, newPhone);
        return user;
    }

    public void changePasswordHash(String userId, String newPasswordHash) {
        getUser(userId).changePasswordHash(newPasswordHash);
    }

    private void ensureContactIsUnique(Email email, PhoneNumber phone, String excludedUserId) {
        for (User other : users.values()) {
            if (other.getId().equals(excludedUserId)) {
                continue;
            }
            if (email != null && other.hasEmail(email)) {
                throw new BusinessRuleException("This email is already registered.");
            }
            if (phone != null && other.hasPhone(phone)) {
                throw new BusinessRuleException("This mobile number is already registered.");
            }
        }
    }

    private static Email parseEmail(String raw) {
        return raw == null || raw.isBlank() ? null : new Email(raw);
    }

    private static PhoneNumber parsePhone(String raw) {
        return raw == null || raw.isBlank() ? null : new PhoneNumber(raw);
    }
}
