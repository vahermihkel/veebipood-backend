package ee.mihkel.veebipoodbackend.service;

import ee.mihkel.veebipoodbackend.dto.CreateOrderRequest;
import ee.mihkel.veebipoodbackend.dto.OrderItemRequest;
import ee.mihkel.veebipoodbackend.entity.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private OrderService orderService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void throwsExeptionWhenNoItemsInOrder() {
        List<OrderItemRequest> orderItems = new ArrayList<>();
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(orderItems, "", "", "");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> orderService.createOrder(createOrderRequest, ""));
        assertEquals("400 BAD_REQUEST \"Order must contain at least one item\"", exception.getMessage());
    }

    @Test
    void createOrder() {
        OrderItemRequest orderItem = new OrderItemRequest(3L, 1);
        List<OrderItemRequest> orderItems = new ArrayList<>(Collections.singletonList(orderItem));
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(orderItems, "", "", "");
        Order order = orderService.createOrder(createOrderRequest, "a");
        assertEquals(210.00, order.getTotalPrice());
    }
}