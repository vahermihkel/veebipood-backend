package ee.mihkel.veebipoodbackend.dto.smartpost;

import org.springframework.http.MediaType;

public record LabelResponse(byte[] content, MediaType contentType) {
}