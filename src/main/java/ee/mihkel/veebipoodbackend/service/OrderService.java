package ee.mihkel.veebipoodbackend.service;

import ee.mihkel.veebipoodbackend.dto.CreateOrderRequest;
import ee.mihkel.veebipoodbackend.dto.OrderItemRequest;
import ee.mihkel.veebipoodbackend.entity.Order;
import ee.mihkel.veebipoodbackend.entity.OrderItem;
import ee.mihkel.veebipoodbackend.entity.OrderStatus;
import ee.mihkel.veebipoodbackend.entity.Product;
import ee.mihkel.veebipoodbackend.entity.User;
import ee.mihkel.veebipoodbackend.repository.OrderRepository;
import ee.mihkel.veebipoodbackend.repository.ProductRepository;
import ee.mihkel.veebipoodbackend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Order createOrder(CreateOrderRequest request, String username) {
        if (request.items() == null || request.items().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Order must contain at least one item");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderDate(LocalDateTime.now());
        order.setRecipientName(request.recipientName());
        order.setRecipientPhone(request.recipientPhone());
        order.setPlaceId(request.placeId());

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : request.items()) {
            if (itemRequest.quantity() == null || itemRequest.quantity() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Quantity must be positive");
            }

            Product product = productRepository.findById(itemRequest.productId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "Product not found: " + itemRequest.productId()));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setPrice(product.getPrice());
            order.getItems().add(orderItem);

            total = total.add(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setTotalPrice(total);

        return orderRepository.save(order);
    }
}
