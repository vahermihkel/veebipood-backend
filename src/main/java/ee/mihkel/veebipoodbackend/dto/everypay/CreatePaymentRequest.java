package ee.mihkel.veebipoodbackend.dto.everypay;

import jakarta.validation.constraints.NotNull;

public record CreatePaymentRequest(
        @NotNull(message = "order id is required") Long orderId
) {
}