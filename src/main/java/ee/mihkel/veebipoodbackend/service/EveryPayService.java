package ee.mihkel.veebipoodbackend.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import ee.mihkel.veebipoodbackend.dto.everypay.PaymentResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class EveryPayService {

    private final RestClient restClient;
    private final String apiUsername;
    private final String accountName;
    private final String customerUrl;

    public EveryPayService(
            @Value("${everypay.api.base-url}") String baseUrl,
            @Value("${everypay.api.username}") String apiUsername,
            @Value("${everypay.api.password}") String apiPassword,
            @Value("${everypay.api.account-name}") String accountName,
            @Value("${everypay.customer-url}") String customerUrl) {
        this.apiUsername = apiUsername;
        this.accountName = accountName;
        this.customerUrl = customerUrl;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders(headers -> headers.setBasicAuth(apiUsername, apiPassword))
                .build();
    }

    public PaymentResponse initiateOneOffPayment(String orderReference, BigDecimal amount) {
        OneOffPaymentRequest body = new OneOffPaymentRequest(
                accountName,
                UUID.randomUUID().toString(),
                Instant.now().truncatedTo(ChronoUnit.SECONDS).toString(),
                amount,
                orderReference,
                customerUrl,
                apiUsername
        );

        OneOffPaymentResponse response = restClient.post()
                .uri("/v4/payments/oneoff")
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(OneOffPaymentResponse.class);

        if (response == null || response.paymentLink() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "EveryPay did not return a payment link");
        }

        return new PaymentResponse(response.paymentReference(), response.paymentLink(), response.paymentState(), response.currency());
    }

    private record OneOffPaymentRequest(
            @JsonProperty("account_name") String accountName,
            String nonce,
            String timestamp,
            BigDecimal amount,
            @JsonProperty("order_reference") String orderReference,
            @JsonProperty("customer_url") String customerUrl,
            @JsonProperty("api_username") String apiUsername
    ) {
    }

    private record OneOffPaymentResponse(
            @JsonProperty("payment_reference") String paymentReference,
            @JsonProperty("payment_link") String paymentLink,
            @JsonProperty("payment_state") String paymentState,
            String currency
    ) {
    }
}