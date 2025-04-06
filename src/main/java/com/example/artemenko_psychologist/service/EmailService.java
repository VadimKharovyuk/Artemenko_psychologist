package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.service.impl.EmailSender;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService implements EmailSender {
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;
    private final EmailQueueService emailQueueService;

    @Value("${spring.mail.username}")
    private String fromEmail;


    @Override
    public void sendHtmlMessage(String to, String subject, String template, Map<String, Object> context) {
        try {
            // Прямая отправка без очереди
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context thymeleafContext = new Context();
            context.forEach(thymeleafContext::setVariable);
            String htmlBody = templateEngine.process("email/manual-newsletter-template", thymeleafContext);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            // Добавляем задержку перед отправкой
            Thread.sleep(5000);

            mailSender.send(message);
        } catch (MessagingException | InterruptedException e) {
            log.error("Ошибка при отправке email: ", e);
        }
    }

    @Override
    public void sendBulkHtmlMessage(List<String> emails, String subject, String template, Map<String, Object> context) {
        for (String email : emails) {
            Map<String, Object> personalizedContext = new HashMap<>(context);
            personalizedContext.put("recipientEmail", email);

            // Добавляем в очередь вместо прямого вызова
            emailQueueService.queueEmail(() ->
                    sendHtmlMessage(email, subject, template, personalizedContext)
            );
        }
    }
}