package ee.mihkel.veebipoodbackend.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import ee.mihkel.veebipoodbackend.dto.smartpost.BarcodeResponse;
import ee.mihkel.veebipoodbackend.dto.smartpost.LabelResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class SmartPostService {

    private final RestClient restClient;
    private final String apiKey;
    private final String gatewaySecret;

    public SmartPostService(
            @Value("${smartpost.api.base-url}") String baseUrl,
            @Value("${smartpost.api.key}") String apiKey,
            @Value("${smartpost.api.gateway-secret}") String gatewaySecret) {
        this.apiKey = apiKey;
        this.gatewaySecret = gatewaySecret;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    public String getPlaces(String country) {
        return restClient.get()
                .uri("/places?country={country}", country)
                .header("Authorization", apiKey)
                .header("X-GATEWAY-SECRET", gatewaySecret)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(String.class);
    }

    public BarcodeResponse createOrder(String recipientName, String recipientPhone, String placeId) {
        OrderRequest body = new OrderRequest(new OrdersWrapper(new OrderItem(
                new Recipient(recipientName, recipientPhone),
                new Destination(placeId)
        )));

        OrderResponse response = restClient.post()
                .uri("/orders")
                .header("Authorization", apiKey)
                .header("X-GATEWAY-SECRET", gatewaySecret)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(OrderResponse.class);

        if (response == null || response.orders() == null
                || response.orders().item() == null || response.orders().item().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "SmartPost did not return a barcode");
        }

        OrderResponseItem item = response.orders().item().getFirst();
        return new BarcodeResponse(item.barcode(), item.reference());
    }

    public LabelResponse getLabel(String barcode, String format) {
        ResponseEntity<byte[]> response = restClient.get()
                .uri("/labels?format={format}&barcode={barcode}", format, barcode)
                .header("Authorization", apiKey)
                .header("X-GATEWAY-SECRET", gatewaySecret)
                .retrieve()
                .toEntity(byte[].class);

        if (response.getBody() == null || response.getBody().length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "SmartPost did not return a label");
        }

        MediaType contentType = response.getHeaders().getContentType();
        return new LabelResponse(response.getBody(), contentType != null ? contentType : MediaType.APPLICATION_PDF);
    }

    private record OrderRequest(OrdersWrapper orders) {
    }

    private record OrdersWrapper(OrderItem item) {
    }

    private record OrderItem(Recipient recipient, Destination destination) {
    }

    private record Recipient(String name, String phone) {
    }

    private record Destination(@JsonProperty("place_id") String placeId) {
    }

    private record OrderResponse(OrdersResponseWrapper orders) {
    }

    private record OrdersResponseWrapper(List<OrderResponseItem> item) {
    }

    private record OrderResponseItem(String barcode, String reference) {
    }
}