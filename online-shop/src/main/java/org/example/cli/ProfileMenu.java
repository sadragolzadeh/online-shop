package org.example.cli;

import java.util.List;
import org.example.model.entity.User;
import org.example.service.AddressService;
import org.example.service.AuthService;
import org.example.service.UserService;
import org.example.service.dto.AddressRequest;

/** Profile viewing/editing and address book management of the logged-in customer. */
public class ProfileMenu extends Menu {

    private static final String KEEP_HINT = " (Enter = keep, - = remove)";

    private final EntityPrinter printer;
    private final AuthService authService;
    private final UserService userService;
    private final AddressService addressService;

    public ProfileMenu(ConsoleIO io, EntityPrinter printer, AuthService authService,
                       UserService userService, AddressService addressService) {
        super(io);
        this.printer = printer;
        this.authService = authService;
        this.userService = userService;
        this.addressService = addressService;
    }

    @Override
    public void open() {
        loop("My Profile", List.of(
                new MenuItem("View profile", this::showProfile),
                new MenuItem("Edit profile", this::editProfile),
                new MenuItem("Manage addresses", this::openAddressMenu)));
    }

    private void showProfile() {
        printer.printProfile(authService.requireCurrentUser());
    }

    private void editProfile() {
        User user = authService.requireCurrentUser();
        String name = askKeep("Name", user.getFullName());
        String email = askKeep("Email", user.getEmail().map(Object::toString).orElse(""));
        String phone = askKeep("Mobile number", user.getPhone().map(Object::toString).orElse(""));
        userService.updateProfile(user.getId(), name, email, phone);
        io.printSuccess("Profile updated.");
    }

    private String askKeep(String label, String current) {
        String input = io.readLine(label + " [" + current + "]" + KEEP_HINT);
        if (input.isEmpty()) {
            return current;
        }
        return input.equals("-") ? "" : input;
    }

    private void openAddressMenu() {
        loop("My Addresses", List.of(
                new MenuItem("List addresses", this::listAddresses),
                new MenuItem("Add address", this::addAddress),
                new MenuItem("Edit address", this::editAddress),
                new MenuItem("Delete address", this::deleteAddress),
                new MenuItem("Set default address", this::setDefaultAddress)));
    }

    private void listAddresses() {
        printer.printAddresses(addressService.getAddresses(authService.requireCurrentUser().getId()));
    }

    private void addAddress() {
        addressService.addAddress(authService.requireCurrentUser().getId(), readAddressRequest());
        io.printSuccess("Address added.");
    }

    private void editAddress() {
        String userId = authService.requireCurrentUser().getId();
        listAddresses();
        String addressId = io.readLine("Address ID");
        addressService.updateAddress(userId, addressId, readAddressRequest());
        io.printSuccess("Address updated.");
    }

    private void deleteAddress() {
        String userId = authService.requireCurrentUser().getId();
        listAddresses();
        addressService.deleteAddress(userId, io.readLine("Address ID"));
        io.printSuccess("Address deleted.");
    }

    private void setDefaultAddress() {
        String userId = authService.requireCurrentUser().getId();
        listAddresses();
        addressService.setDefaultAddress(userId, io.readLine("Address ID"));
        io.printSuccess("Default address changed.");
    }

    private AddressRequest readAddressRequest() {
        String title = io.readLine("Address title (e.g. Home)");
        String recipient = io.readLine("Recipient name");
        String phone = io.readLine("Recipient phone number");
        String country = io.readLine("Country");
        String city = io.readLine("City");
        String street = io.readLine("Address");
        String postalCode = io.readLine("Postal code");
        boolean makeDefault = io.confirm("Make this the default address?");
        return new AddressRequest(title, recipient, phone, country, city, street, postalCode, makeDefault);
    }
}
