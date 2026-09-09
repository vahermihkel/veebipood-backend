package ee.mihkel.veebipoodbackend.dto.everypay;

public record PaymentResponse(
        String paymentReference,
        String paymentLink,
        String paymentState,
        String currency
) {
}