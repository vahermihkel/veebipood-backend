package ee.mihkel.veebipoodbackend.dto.smartpost;

import jakarta.validation.constraints.NotNull;

public record CreateBarcodeRequest(
        @NotNull(message = "order id is required") Long orderId
) {
}