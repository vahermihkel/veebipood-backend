package ee.mihkel.veebipoodbackend.exception;

import ee.mihkel.veebipoodbackend.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.hibernate.PropertyValueException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.stream.Collectors;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        log.error("e: ", ex);
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request body", request);
    }

    @ExceptionHandler(PropertyValueException.class)
    public ResponseEntity<ErrorResponse> handlePropertyValue(PropertyValueException ex, HttpServletRequest request) {
        log.error("e: ", ex);
        return build(HttpStatus.BAD_REQUEST, "Missing required field: " + simplify(ex.getPropertyName()), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        log.error("e: ", ex);
        return build(HttpStatus.BAD_REQUEST, "Invalid or conflicting data: " + ex.getMostSpecificCause().getMessage(), request);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFound(HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password", request);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(ResponseStatusException ex, HttpServletRequest request) {
        log.error("e: ", ex);
        HttpStatusCode status = ex.getStatusCode();
        return build(HttpStatus.valueOf(status.value()), ex.getReason(), request);
    }

    @ExceptionHandler(RestClientResponseException.class)
    public ResponseEntity<ErrorResponse> handleRestClientResponse(RestClientResponseException ex, HttpServletRequest request) {
        log.error("Upstream API call failed on {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.valueOf(ex.getStatusCode().value()), "Upstream API error: " + ex.getStatusText(), request);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleRestClient(RestClientException ex, HttpServletRequest request) {
        log.error("Upstream API call failed on {}", request.getRequestURI(), ex);
        return build(HttpStatus.BAD_GATEWAY, "Upstream API is unreachable", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception on {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error occurred", request);
    }

    private String simplify(String propertyName) {
        if (propertyName == null) {
            return "unknown";
        }
        int idx = propertyName.lastIndexOf('.');
        return idx >= 0 ? propertyName.substring(idx + 1) : propertyName;
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
