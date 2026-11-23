package org.example.service.payment;

import java.util.UUID;
import org.example.model.valueobject.Money;

/** Test double: every card is approved except cards whose last digit is 0. */
public class SimulatedPaymentGateway implements PaymentGateway {

    @Override
    public GatewayResult charge(String orderId, Money amount, String cardNumber) {
        boolean declined = cardNumber.endsWith("0");
        if (declined) {
            return new GatewayResult(false, null, Money.ZERO, "The card was declined by the bank.");
        }
        return new GatewayResult(true, "TX-" + UUID.randomUUID(), amount, "Payment completed successfully.");
    }
}
