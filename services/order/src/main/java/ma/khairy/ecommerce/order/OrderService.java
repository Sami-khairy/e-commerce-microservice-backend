package ma.khairy.ecommerce.order;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ma.khairy.ecommerce.customer.CustomerClient;
import ma.khairy.ecommerce.exception.BusinessException;
import ma.khairy.ecommerce.kafka.OrderConfirmation;
import ma.khairy.ecommerce.kafka.OrderProducer;
import ma.khairy.ecommerce.orderline.OrderLineRequest;
import ma.khairy.ecommerce.orderline.OrderLineService;
import ma.khairy.ecommerce.product.ProductClient;
import ma.khairy.ecommerce.product.PurchaseRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository repository;
    private final OrderMapper mapper;
    private final CustomerClient customerClient;
    private final PaymentClient paymentClient;
    private final ProductClient productClient;
    private final OrderLineService orderLineService;
    private final OrderProducer orderProducer;

    @Transactional
    public Integer createOrder(OrderRequest request) {
        //using OpenFeign to communicate with the customer service (find customer by ID)
        var customer = this.customerClient.findCustomerById(request.customerId())
                .orElseThrow(() -> new BusinessException("Cannot create order:: No customer exists with the provided ID"));

        //using RestTemplate to communicate with the product service (purchase products)
        var purchasedProducts = productClient.purchaseProducts(request.products());

        //saving the order in the database
        var order = this.repository.save(mapper.toOrder(request));

        for (PurchaseRequest purchaseRequest : request.products()) {
            orderLineService.saveOrderLine(
                    new OrderLineRequest(
                            null,
                            order.getId(),
                            purchaseRequest.productId(),
                            purchaseRequest.quantity()
                    )
            );
        }

        //start payment processing
        var paymentRequest = new PaymentRequest(
                request.amount(),
                request.paymentMethod(),
                order.getId(),
                order.getReference(),
                customer
        );
        paymentClient.requestOrderPayment(paymentRequest);

        //send order confirmation to the Kafka topic (notification service)
        orderProducer.sendOrderConfirmation(
                new OrderConfirmation(
                        request.reference(),
                        request.amount(),
                        request.paymentMethod(),
                        customer,
                        purchasedProducts
                )
        );

        return order.getId();
    }

    public List<OrderResponse> findAllOrders() {
        return this.repository.findAll()
                .stream()
                .map(this.mapper::fromOrder)
                .collect(Collectors.toList());
    }

    public OrderResponse findById(Integer id) {
        return this.repository.findById(id)
                .map(this.mapper::fromOrder)
                .orElseThrow(() -> new EntityNotFoundException(String.format("No order found with the provided ID: %d", id)));
    }
}
