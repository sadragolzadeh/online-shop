package org.example.cli;

import java.util.List;
import java.util.Optional;
import org.example.model.entity.Address;
import org.example.model.entity.Cart;
import org.example.model.entity.Order;
import org.example.model.entity.Payment;
import org.example.model.enums.PaymentStatus;
import org.example.model.exception.BusinessRuleException;
import org.example.service.AddressService;
import org.example.service.AuthService;
import org.example.service.OrderService;
import org.example.service.PaymentService;

/** Checkout, payment and order history of the logged-in customer. */
public class OrderMenu extends Menu {

    private final EntityPrinter printer;
    private final AuthService authService;
    private final AddressService addressService;
    private final OrderService orderService;
    private final PaymentService paymentService;

    public OrderMenu(ConsoleIO io, EntityPrinter printer, AuthService authService, AddressService addressService,
                     OrderService orderService, PaymentService paymentService) {
        super(io);
        this.printer = printer;
        this.authService = authService;
        this.addressService = addressService;
        this.orderService = orderService;
        this.paymentService = paymentService;
    }

    @Override
    public void open() {
        loop("Orders", List.of(
                new MenuItem("Start checkout (place an order)", this::checkout),
                new MenuItem("Pay an order", this::payExistingOrder),
                new MenuItem("My orders", this::listOrders),
                new MenuItem("Order details", this::showOrderDetails)));
    }

    private String userId() {
        return authService.requireCurrentUser().getId();
    }

    private void checkout() {
        String userId = userId();
        Cart cart = orderService.startCheckout(userId);
        printer.printCart(cart);

        List<Address> addresses = addressService.getAddresses(userId);
        if (addresses.isEmpty()) {
            throw new BusinessRuleException("Please add an address in the 'Profile' section before placing an order.");
        }
        printer.printAddresses(addresses);
        String addressId = chooseAddressId(addresses);

        io.println("Final order amount: " + cart.getTotalAmount());
        if (!io.confirm("Place the order?")) {
            return;
        }
        Order order = orderService.placeOrder(userId, addressId);
        io.printSuccess("Order " + order.getId() + " was placed and is awaiting payment.");
        if (io.confirm("Pay now?")) {
            pay(order.getId());
        }
    }

    private String chooseAddressId(List<Address> addresses) {
        Optional<Address> defaultAddress = addresses.stream().filter(Address::isDefaultAddress).findFirst();
        String input = io.readLine("Shipping address ID" + defaultAddress.map(a -> " (Enter = " + a.getId() + ")").orElse(""));
        if (input.isEmpty() && defaultAddress.isPresent()) {
            return defaultAddress.get().getId();
        }
        return input;
    }

    private void payExistingOrder() {
        pay(io.readLine("Order ID"));
    }

    private void pay(String orderId) {
        Order order = orderService.getOrderOfUser(userId(), orderId);
        io.println("Amount to pay: " + order.getTotalAmount());
        io.println("(Gateway simulator: cards ending in 0 are declined)");
        String cardNumber = io.readLine("16-digit card number");

        Payment payment = paymentService.payOrder(userId(), orderId, cardNumber);
        if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
            io.printSuccess("Payment successful. Order " + orderId + " is now in 'Paid' status.");
            return;
        }
        io.printError("Payment failed: " + payment.getFailureReason().orElse("Unknown"));
        io.println("The order has not been paid yet; you can try again with the 'Pay an order' option.");
    }

    private void listOrders() {
        printer.printOrders(orderService.getOrdersOfUser(userId()));
    }

    private void showOrderDetails() {
        String userId = userId();
        Order order = orderService.getOrderOfUser(userId, io.readLine("Order ID"));
        printer.printOrderDetails(order);
        printer.printPayments(paymentService.getPaymentsOfOrder(userId, order.getId()));
    }
}
