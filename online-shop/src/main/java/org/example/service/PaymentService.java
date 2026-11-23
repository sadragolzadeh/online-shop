package org.example.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.example.model.entity.Order;
import org.example.model.entity.Payment;
import org.example.model.exception.BusinessRuleException;
import org.example.model.exception.ValidationException;
import org.example.service.payment.GatewayResult;
import org.example.service.payment.PaymentGateway;

/** Pays orders through the gateway and verifies the gateway's answer before accepting it. */
public class PaymentService {

    private static final String CARD_NUMBER_FORMAT = "\\d{16}";

    private final OrderService orderService;
    private final PaymentGateway paymentGateway;
    private final List<Payment> payments = new ArrayList<>();
    private final Set<String> acceptedTransactionRefs = new HashSet<>();
    private final IdGenerator idGenerator = new IdGenerator("PAY");

    public PaymentService(OrderService orderService, PaymentGateway paymentGateway) {
        this.orderService = orderService;
        this.paymentGateway = paymentGateway;
    }

    /** The amount is always the order total; the customer cannot influence it. */
    public Payment payOrder(String userId, String orderId, String cardNumber) {
        validateCardNumber(cardNumber);
        Order order = orderService.getOrderOfUser(userId, orderId);
        order.ensurePayable();
        orderService.ensureOrderItemsPurchasable(order);

        Payment payment = new Payment(idGenerator.next(), order.getId(), order.getTotalAmount());
        payments.add(payment);
        GatewayResult result = paymentGateway.charge(order.getId(), payment.getAmount(), cardNumber);
        verifyAndApply(order, payment, result);
        return payment;
    }

    public List<Payment> getPaymentsOfOrder(String userId, String orderId) {
        Order order = orderService.getOrderOfUser(userId, orderId);
        return payments.stream().filter(payment -> payment.getOrderId().equals(order.getId())).toList();
    }

    private void verifyAndApply(Order order, Payment payment, GatewayResult result) {
        if (!result.approved()) {
            payment.markFailed(result.message());
            return;
        }
        if (result.paidAmount().compareTo(order.getTotalAmount()) != 0) {
            payment.markFailed("The paid amount does not match the order amount.");
            return;
        }
        if (!acceptedTransactionRefs.add(result.transactionRef())) {
            throw new BusinessRuleException("This payment has already been recorded.");
        }
        payment.markSuccessful(result.transactionRef());
        orderService.completePayment(order);
    }

    private static void validateCardNumber(String cardNumber) {
        if (cardNumber == null || !cardNumber.matches(CARD_NUMBER_FORMAT)) {
            throw new ValidationException("Card number must be 16 digits.");
        }
    }
}
