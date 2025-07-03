package ma.khairy.ecommerce.payment;

import ma.khairy.ecommerce.customer.CustomerResponse;
import ma.khairy.ecommerce.order.PaymentMethod;

import java.math.BigDecimal;

public record PaymentRequest(
        BigDecimal amount,
        PaymentMethod paymentMethod,
        Integer orderId,
        String orderReference,
        CustomerResponse customer
) {
}
