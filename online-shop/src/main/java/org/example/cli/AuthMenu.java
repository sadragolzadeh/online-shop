package org.example.cli;

import java.util.List;
import org.example.service.AuthService;

/** Guest actions: registration, login and password recovery. */
public class AuthMenu extends Menu {

    private final AuthService authService;

    public AuthMenu(ConsoleIO io, AuthService authService) {
        super(io);
        this.authService = authService;
    }

    @Override
    public void open() {
        List<MenuItem> items = List.of(
                new MenuItem("Register", this::register),
                new MenuItem("Log in", this::login),
                new MenuItem("Forgot password", this::recoverPassword));
        boolean keepGoing = true;
        while (keepGoing && authService.getCurrentUser().isEmpty()) {
            keepGoing = showOnce("Log in / Register", items, "Back");
        }
    }

    private void register() {
        String fullName = io.readLine("Full name");
        String email = io.readLine("Email (optional)");
        String phone = io.readLine("Mobile number (optional)");
        String password = io.readLine("Password (at least 8 characters)");
        authService.register(fullName, email, phone, password);
        io.printSuccess("Account created. You can now log in.");
    }

    private void login() {
        String identifier = io.readLine("Email or mobile number");
        String password = io.readLine("Password");
        String name = authService.login(identifier, password).getFullName();
        io.printSuccess("Welcome, " + name + ".");
    }

    private void recoverPassword() {
        String identifier = io.readLine("Registered email or mobile number");
        String code = authService.requestPasswordReset(identifier);
        io.println("(Simulated message) Your recovery code: " + code);
        String enteredCode = io.readLine("Recovery code");
        String newPassword = io.readLine("New password");
        authService.resetPassword(identifier, enteredCode, newPassword);
        io.printSuccess("Password changed. You can now log in.");
    }
}
