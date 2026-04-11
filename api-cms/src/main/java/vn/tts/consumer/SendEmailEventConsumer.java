package vn.tts.consumer;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vn.tts.model.event.SendEmailEvent;
import vn.tts.model.payload.EmailPayload;
import vn.tts.service.EmailService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SendEmailEventConsumer {

    private final static String TOPIC_NOTIFY = "topic_publish_notify";

    private final EmailService emailService;

    @KafkaListener(topics = TOPIC_NOTIFY, groupId = TOPIC_NOTIFY)
    public void handleSendEmailEvent(SendEmailEvent event) throws MessagingException {

        String subject = String.format("[%s] %s event", event.getEntityType(), event.getAction());
        String body = String.format("Entity %s has been %s.\n\nMessage: %s",
                event.getEntityId(), event.getAction(), event.getMessage());

        List<String> uniqueEmails = event.getEmail().stream()
                .filter(email -> email != null && !email.isBlank())
                .map(String::trim)
                .distinct()
                .toList();
        System.out.println("Sending email to: " + uniqueEmails);
        emailService.sendEmail(
                new EmailPayload(uniqueEmails, subject, body)
        );
    }
}
