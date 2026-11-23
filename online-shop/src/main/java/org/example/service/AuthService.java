package org.example.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.example.model.entity.User;
import org.example.model.enums.UserRole;
import org.example.model.exception.AccessDeniedException;
import org.example.model.exception.NotFoundException;
import org.example.model.exception.ValidationException;
import org.example.model.valueobject.PasswordPolicy;
import org.example.model.valueobject.PasswordResetToken;

/** Registration, login/logout, password recovery and the current session. */
public class AuthService {

    private static final int RESET_CODE_VALIDITY_MINUTES = 15;
    private static final int RESET_CODE_BOUND = 1_000_000;

    private final UserService userService;
    private final PasswordHasher passwordHasher;
    private final Map<String, PasswordResetToken> resetTokens = new HashMap<>();
    private final SecureRandom random = new SecureRandom();
    private User currentUser;

    public AuthService(UserService userService, PasswordHasher passwordHasher) {
        this.userService = userService;
        this.passwordHasher = passwordHasher;
    }

    public User register(String fullName, String email, String phone, String password) {
        PasswordPolicy.validate(password);
        return userService.createUser(fullName, email, phone, passwordHasher.hash(password), UserRole.CUSTOMER);
    }

    public User login(String identifier, String password) {
        if (identifier == null || identifier.isBlank() || password == null || password.isBlank()) {
            throw new ValidationException("Email/mobile number and password must not be empty.");
        }
        currentUser = userService.findByIdentifier(identifier)
                .filter(User::isActive)
                .filter(user -> passwordHasher.matches(password, user.getPasswordHash()))
                .orElseThrow(() -> new AccessDeniedException("Incorrect email/mobile number or password."));
        return currentUser;
    }

    public void logout() {
        currentUser = null;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public User requireCurrentUser() {
        return getCurrentUser().orElseThrow(() -> new AccessDeniedException("You must log in first."));
    }

    /** Creates a recovery code for a registered identifier; a real system would e-mail/SMS it. */
    public String requestPasswordReset(String identifier) {
        User user = findRegisteredUser(identifier);
        String code = String.format("%06d", random.nextInt(RESET_CODE_BOUND));
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(RESET_CODE_VALIDITY_MINUTES);
        resetTokens.put(user.getId(), new PasswordResetToken(user.getId(), code, expiresAt));
        return code;
    }

    public void resetPassword(String identifier, String code, String newPassword) {
        User user = findRegisteredUser(identifier);
        PasswordResetToken token = resetTokens.get(user.getId());
        if (token == null || token.isExpired(LocalDateTime.now()) || !token.matches(code)) {
            throw new ValidationException("The recovery code is invalid or expired.");
        }
        PasswordPolicy.validate(newPassword);
        userService.changePasswordHash(user.getId(), passwordHasher.hash(newPassword));
        resetTokens.remove(user.getId());
    }

    private User findRegisteredUser(String identifier) {
        return userService.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundException("This email or mobile number is not registered."));
    }
}
