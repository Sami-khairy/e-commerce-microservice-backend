package ma.khairy.ecommerce.kafka;

import ma.khairy.ecommerce.customer.CustomerResponse;
import ma.khairy.ecommerce.order.PaymentMethod;
import ma.khairy.ecommerce.product.PurchaseResponse;

import java.math.BigDecimal;
import java.util.List;

public record OrderConfirmation (
        String orderReference,
        BigDecimal totalAmount,
        PaymentMethod paymentMethod,
        CustomerResponse customer,
        List<PurchaseResponse> products

) {
}
