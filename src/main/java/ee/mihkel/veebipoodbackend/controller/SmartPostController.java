package ee.mihkel.veebipoodbackend.controller;

import ee.mihkel.veebipoodbackend.dto.smartpost.BarcodeResponse;
import ee.mihkel.veebipoodbackend.dto.smartpost.CreateBarcodeRequest;
import ee.mihkel.veebipoodbackend.dto.smartpost.LabelResponse;
import ee.mihkel.veebipoodbackend.entity.Order;
import ee.mihkel.veebipoodbackend.repository.OrderRepository;
import ee.mihkel.veebipoodbackend.service.SmartPostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/smartpost")
public class SmartPostController {

    private final SmartPostService smartPostService;
    private final OrderRepository orderRepository;

    public SmartPostController(SmartPostService smartPostService, OrderRepository orderRepository) {
        this.smartPostService = smartPostService;
        this.orderRepository = orderRepository;
    }

    @GetMapping(value = "/places", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> getPlaces(@RequestParam(defaultValue = "EE") String country) {
        return ResponseEntity.ok(smartPostService.getPlaces(country));
    }

    @PostMapping(value = "/barcode", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BarcodeResponse> generateBarcode(@Valid @RequestBody CreateBarcodeRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Order not found: " + request.orderId()));

        BarcodeResponse response = smartPostService.createOrder(
                order.getRecipientName(), order.getRecipientPhone(), order.getPlaceId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/labels")
    public ResponseEntity<byte[]> getLabel(
            @RequestParam String barcode,
            @RequestParam(defaultValue = "A5") String format) {
        LabelResponse label = smartPostService.getLabel(barcode, format);
        return ResponseEntity.ok()
                .contentType(label.contentType())
                .body(label.content());
    }
}