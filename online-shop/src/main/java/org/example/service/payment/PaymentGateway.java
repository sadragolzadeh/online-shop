package org.example.service.payment;

import org.example.model.valueobject.Money;

/** Port to an external payment provider. */
public interface PaymentGateway {

    GatewayResult charge(String orderId, Money amount, String cardNumber);
}
