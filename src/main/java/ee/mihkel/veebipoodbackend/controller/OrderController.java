package ee.mihkel.veebipoodbackend.controller;

import ee.mihkel.veebipoodbackend.dto.CreateOrderRequest;
import ee.mihkel.veebipoodbackend.entity.Order;
import ee.mihkel.veebipoodbackend.entity.OrderItem;
import ee.mihkel.veebipoodbackend.entity.Product;
import ee.mihkel.veebipoodbackend.entity.User;
import ee.mihkel.veebipoodbackend.repository.OrderRepository;
import ee.mihkel.veebipoodbackend.repository.ProductRepository;
import ee.mihkel.veebipoodbackend.repository.UserRepository;
import ee.mihkel.veebipoodbackend.service.EmailService;
import ee.mihkel.veebipoodbackend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderService orderService;
    private final EmailService emailService;

//    public OrderController(OrderRepository orderRepository, UserRepository userRepository,
//                            ProductRepository productRepository, OrderService orderService) {
//        this.orderRepository = orderRepository;
//        this.userRepository = userRepository;
//        this.productRepository = productRepository;
//        this.orderService = orderService;
//    }

    @PostMapping
    public ResponseEntity<Order> create(@Valid @RequestBody CreateOrderRequest request, Authentication authentication) {
        Order order = orderService.createOrder(request, authentication.getName());
        emailService.sendEmail();
        return ResponseEntity.ok(order);
    }

    @GetMapping
    public List<Order> getAll() {
        return orderRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return orderRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Order> update(@PathVariable Long id, @RequestBody Order updated) {
        if (!resolveReferences(updated)) {
            return ResponseEntity.badRequest().build();
        }
        return orderRepository.findById(id)
                .map(existing -> {
                    existing.setUser(updated.getUser());
                    existing.setStatus(updated.getStatus());
                    existing.setTotalPrice(updated.getTotalPrice());
                    existing.setOrderDate(updated.getOrderDate());

                    existing.getItems().clear();
                    for (OrderItem item : updated.getItems()) {
                        item.setOrder(existing);
                        existing.getItems().add(item);
                    }

                    return ResponseEntity.ok(orderRepository.save(existing));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!orderRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        orderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean resolveReferences(Order order) {
        if (order.getUser() == null || order.getUser().getId() == null) {
            return false;
        }
        User user = userRepository.findById(order.getUser().getId()).orElse(null);
        if (user == null) {
            return false;
        }
        order.setUser(user);

        for (OrderItem item : order.getItems()) {
            if (item.getProduct() == null || item.getProduct().getId() == null) {
                return false;
            }
            Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
            if (product == null) {
                return false;
            }
            item.setProduct(product);
            item.setOrder(order);
        }
        return true;
    }
}