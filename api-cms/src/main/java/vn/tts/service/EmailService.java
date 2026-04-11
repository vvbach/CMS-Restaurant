package vn.tts.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import vn.tts.model.payload.EmailPayload;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    public void sendEmail(EmailPayload payload) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

        Context context = new Context();
        context.setVariable("message", payload.getMessage());
        String htmlContent = templateEngine.process("email-template", context);

        helper.setTo(payload.getTo().toArray(new String[0]));
        helper.setSubject(payload.getSubject());
        helper.setText(htmlContent, true);

        mailSender.send(mimeMessage);

        log.info("Email sent to: {}", String.join(", ", payload.getTo()));
    }
}
