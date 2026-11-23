package org.example.cli;

import java.util.List;
import java.util.Optional;
import org.example.model.entity.User;
import org.example.service.AuthService;

/** Top-level screen: shows a different main menu for guests, customers and admins. */
public class CliApplication extends Menu {

    private final AuthService authService;
    private final AuthMenu authMenu;
    private final CatalogMenu catalogMenu;
    private final ProfileMenu profileMenu;
    private final CartMenu cartMenu;
    private final OrderMenu orderMenu;
    private final AdminMenu adminMenu;

    public CliApplication(ConsoleIO io, AuthService authService, AuthMenu authMenu, CatalogMenu catalogMenu,
                          ProfileMenu profileMenu, CartMenu cartMenu, OrderMenu orderMenu, AdminMenu adminMenu) {
        super(io);
        this.authService = authService;
        this.authMenu = authMenu;
        this.catalogMenu = catalogMenu;
        this.profileMenu = profileMenu;
        this.cartMenu = cartMenu;
        this.orderMenu = orderMenu;
        this.adminMenu = adminMenu;
    }

    @Override
    public void open() {
        io.println("Welcome to the online shop.");
        boolean keepRunning = true;
        while (keepRunning) {
            keepRunning = showOnce("Main Menu", currentItems(), "Exit");
        }
        io.println("Goodbye!");
    }

    private List<MenuItem> currentItems() {
        Optional<User> user = authService.getCurrentUser();
        if (user.isEmpty()) {
            return guestItems();
        }
        return user.get().isAdmin() ? adminItems() : customerItems();
    }

    private List<MenuItem> guestItems() {
        return List.of(
                new MenuItem("Log in / Register", authMenu::open),
                new MenuItem("Browse the shop", catalogMenu::open));
    }

    private List<MenuItem> customerItems() {
        return List.of(
                new MenuItem("Shop", catalogMenu::open),
                new MenuItem("Shopping cart", cartMenu::open),
                new MenuItem("Orders and payment", orderMenu::open),
                new MenuItem("Profile and addresses", profileMenu::open),
                new MenuItem("Log out", this::logout));
    }

    private List<MenuItem> adminItems() {
        return List.of(
                new MenuItem("Admin Panel", adminMenu::open),
                new MenuItem("Browse the shop", catalogMenu::open),
                new MenuItem("Log out", this::logout));
    }

    private void logout() {
        authService.logout();
        io.printSuccess("You have been logged out.");
    }
}
