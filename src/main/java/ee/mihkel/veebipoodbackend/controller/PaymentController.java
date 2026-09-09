package ee.mihkel.veebipoodbackend.controller;

import ee.mihkel.veebipoodbackend.dto.everypay.CreatePaymentRequest;
import ee.mihkel.veebipoodbackend.dto.everypay.PaymentResponse;
import ee.mihkel.veebipoodbackend.entity.Order;
import ee.mihkel.veebipoodbackend.repository.OrderRepository;
import ee.mihkel.veebipoodbackend.service.EveryPayService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final EveryPayService everyPayService;
    private final OrderRepository orderRepository;

    public PaymentController(EveryPayService everyPayService, OrderRepository orderRepository) {
        this.everyPayService = everyPayService;
        this.orderRepository = orderRepository;
    }

    @PostMapping(value = "/oneoff", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PaymentResponse> initiateOneOffPayment(@Valid @RequestBody CreatePaymentRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found: " + request.orderId()));

        PaymentResponse response = everyPayService.initiateOneOffPayment(
                order.getId().toString(), order.getTotalPrice());
        return ResponseEntity.ok(response);
    }
}