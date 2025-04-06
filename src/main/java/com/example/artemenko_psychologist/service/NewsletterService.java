package com.example.artemenko_psychologist.service;
import com.example.artemenko_psychologist.dto.blog.BlogPostListDto;
import com.example.artemenko_psychologist.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsletterService {
    private final SubscriptionRepository subscriptionRepository;
    private final EmailQueueService emailQueueService;
    private final BlogPostService blogPostService;

    public void sendNewsletterToAllSubscribers(String subject, Long blogPostId) {
        List<String> subscriberEmails = subscriptionRepository.findAllSubscriberEmails();

        if (subscriberEmails.isEmpty()) {
            log.warn("Попытка отправки рассылки при отсутствии подписчиков");
            return;
        }

        // Получение данных о статье
        BlogPostListDto post = blogPostService.getBlogPostById(blogPostId);

        Map<String, Object> context = new HashMap<>();
        context.put("blogPostId", blogPostId);
        context.put("subscriberCount", subscriberEmails.size());
        context.put("postTitle", post.getTitle());
        context.put("postContent", post.getShortDescription()); // Добавляем содержание поста
        context.put("postImageUrl", post.getPreviewImageUrl()); // Если есть изображение

        log.info("Начало рассылки новостного письма. Тема: {}, Количество получателей: {}",
                subject, subscriberEmails.size());

        // Изменяем метод на queueBulkEmail
        emailQueueService.queueBulkEmail(
                subscriberEmails,
                subject,
                "newsletter-template", // Убедитесь, что этот шаблон существует как "email/newsletter-template.html"
                context
        );
    }

    public long getSubscriberCount() {
        return subscriptionRepository.count();
    }

    public void sendManualNewsletter(String subject, String content) {
        // Получаем список email-адресов подписчиков
        List<String> subscriberEmails = subscriptionRepository.findAllSubscriberEmails();

        if (subscriberEmails.isEmpty()) {
            log.warn("Попытка отправки ручной рассылки при отсутствии подписчиков");
            return;
        }

        // Подготавливаем контекст для письма
        for (String email : subscriberEmails) {
            Map<String, Object> context = new HashMap<>();
            context.put("manualContent", content);
            context.put("recipientEmail", email);

            try {
                // Изменяем метод на queueEmail
                emailQueueService.queueEmail(
                        email,
                        subject,
                        "manual-newsletter-template",
                        context
                );
            } catch (Exception e) {
                log.error("Ошибка при отправке ручной рассылки для {}: {}", email, e.getMessage());
            }
        }
    }

    /**
     * Отправляет приветственное письмо новому подписчику
     * @param email Email подписчика
     */
    public void sendWelcomeEmail(String email) {
        if (email == null || email.isEmpty()) {
            log.warn("Попытка отправки приветственного письма на пустой email");
            return;
        }

//        log.info("Отправка приветственного письма новому подписчику: {}", email);

        Map<String, Object> context = new HashMap<>();
        context.put("recipientEmail", email);

        try {
            emailQueueService.queueEmail(
                    email,
                    "Дякуємо за підписку на розсилку психолога Артеменко",
                    "welcome-template",
                    context
            );
        } catch (Exception e) {
            log.error("Ошибка при отправке приветственного письма для {}: {}", email, e.getMessage());
        }
    }
}