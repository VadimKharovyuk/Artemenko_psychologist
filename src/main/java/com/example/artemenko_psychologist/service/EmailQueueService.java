package com.example.artemenko_psychologist.service;//package com.example.artemenko_psychologist.service;
import org.thymeleaf.context.Context;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailQueueService {
    private final ExecutorService executorService = Executors.newFixedThreadPool(1);
    private final BlockingQueue<EmailTask> emailQueue = new LinkedBlockingQueue<>();
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    // Создаем класс для задачи отправки email
    @Data
    @Builder
    private static class EmailTask {
        private String to;
        private String subject;
        private String template;
        private Map<String, Object> context;
    }

    @PostConstruct
    public void init() {
        executorService.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    EmailTask task = emailQueue.take();
                    try {
                        sendEmail(task);
                    } catch (Exception e) {
                        log.error("Ошибка при выполнении задачи отправки email", e);
                    }
                    TimeUnit.SECONDS.sleep(5); // Задержка между отправками
                } catch (InterruptedException e) {
                    log.warn("Прерывание потока обработки email-очереди");
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
    }

    private void sendEmail(EmailTask task) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            Context thymeleafContext = new Context();
            task.getContext().forEach(thymeleafContext::setVariable);
            String htmlBody = templateEngine.process("email/" + task.getTemplate(), thymeleafContext);

            helper.setFrom(fromEmail);
            helper.setTo(task.getTo());
            helper.setSubject(task.getSubject());
            helper.setText(htmlBody, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке email: ", e);
        }
    }

    public void queueEmail(String to, String subject, String template, Map<String, Object> context) {
        EmailTask task = EmailTask.builder()
                .to(to)
                .subject(subject)
                .template(template)
                .context(context)
                .build();

        if (!emailQueue.offer(task)) {
            log.warn("Не удалось добавить задачу в очередь email-рассылки");
        }
    }

    public void queueBulkEmail(List<String> emails, String subject, String template, Map<String, Object> context) {
        for (String email : emails) {
            Map<String, Object> personalizedContext = new HashMap<>(context);
            personalizedContext.put("recipientEmail", email);
            queueEmail(email, subject, template, personalizedContext);
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Завершение службы email-очереди");
        executorService.shutdownNow();
    }
}