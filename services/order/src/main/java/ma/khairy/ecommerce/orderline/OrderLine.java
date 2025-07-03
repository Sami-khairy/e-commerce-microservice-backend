package ma.khairy.ecommerce.orderline;

import jakarta.persistence.*;
import lombok.*;
import ma.khairy.ecommerce.order.Order;

@AllArgsConstructor
@Builder
@Getter
@Setter
@Entity
@NoArgsConstructor
public class OrderLine {

    @Id
    @GeneratedValue
    private Integer id;
    @ManyToOne
    @JoinColumn(name = "order_id")
    private Order order;
    private Integer productId;
    private double quantity;
}