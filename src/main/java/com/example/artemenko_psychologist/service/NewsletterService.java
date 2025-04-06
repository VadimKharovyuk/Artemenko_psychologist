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
    private final EmailService emailService;
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

        emailService.sendBulkHtmlMessage(
                subscriberEmails,
                subject,
                "newsletter-template", // Убедитесь, что этот шаблон существует как "email/newsletter-template.html"
                context
        );
    }

    public long getSubscriberCount() {
      return   subscriptionRepository.count();
    }

    public void sendManualNewsletter(String subject, String content) {
        // Получаем список email-адресов подписчиков
        List<String> subscriberEmails = subscriptionRepository.findAllSubscriberEmails();

        if (subscriberEmails.isEmpty()) {
            log.warn("Попытка отправки ручной рассылки при отсутствии подписчиков");
            return;
        }

        log.info("Начало ручной рассылки. Тема: {}, Количество получателей: {}",
                subject, subscriberEmails.size());

        // Подготавливаем контекст для письма
        for (String email : subscriberEmails) {
            Map<String, Object> context = new HashMap<>();
            context.put("manualContent", content);
            context.put("recipientEmail", email);

            try {
                // Отправка письма с задержкой
                emailService.sendHtmlMessage(
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
}