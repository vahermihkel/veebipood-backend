package ee.mihkel.veebipoodbackend.service;

import com.resend.Resend;
import com.resend.services.emails.model.CreateEmailOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class EmailService {

    private final Resend resend;
    private final String from;
    private final String to;
    private final String subject;
    private final String html;

    public EmailService(
            @Value("${resend.api.key}") String apiKey,
            @Value("${resend.email.from}") String from,
            @Value("${resend.email.to}") String to,
            @Value("${resend.email.subject}") String subject,
            @Value("${resend.email.html}") String html) {
        this.resend = new Resend(apiKey);
        this.from = from;
        this.to = to;
        this.subject = subject;
        this.html = html;
    }

    public void sendEmail() {
        CreateEmailOptions createEmailOptions = CreateEmailOptions.builder()
                .from(from)
                .addTo(to)
                .subject(subject)
                .html(html)
                .build();

        try {
            System.out.println(createEmailOptions);
            resend.emails().send(createEmailOptions);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Failed to send email", e);
        }
    }
}
