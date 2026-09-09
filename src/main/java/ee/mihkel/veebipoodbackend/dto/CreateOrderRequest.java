package ee.mihkel.veebipoodbackend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "order must contain at least one item") @Valid List<OrderItemRequest> items,
        @NotBlank(message = "recipient name is required") String recipientName,
        @NotBlank(message = "recipient phone is required") String recipientPhone,
        @NotBlank(message = "place id is required") String placeId
) {
}