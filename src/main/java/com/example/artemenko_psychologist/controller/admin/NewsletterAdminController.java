package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.dto.blog.BlogPostListDto;
import com.example.artemenko_psychologist.service.BlogPostService;
import com.example.artemenko_psychologist.service.NewsletterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/newsletter")
@RequiredArgsConstructor
public class NewsletterAdminController {
    private final NewsletterService newsletterService;
    private final BlogPostService blogPostService;

    // Страница управления рассылкой
    @GetMapping
    public String newsletterPage(Model model) {
        // Получаем количество подписчиков
        long subscriberCount = newsletterService.getSubscriberCount();

        // Получаем последние блог-посты для выбора
        List<BlogPostListDto> latestPosts = blogPostService.getLatestBlogPosts(10);

        model.addAttribute("subscriberCount", subscriberCount);
        model.addAttribute("latestPosts", latestPosts);

        return "admin/newsletter/index";
    }

    // Отправка рассылки для конкретного поста
    @PostMapping("/send/{blogPostId}")
    public String sendNewsletter(
            @PathVariable Long blogPostId,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Получаем информацию о посте для темы письма
            BlogPostListDto post = blogPostService.getBlogPostById(blogPostId);

            // Отправляем рассылку
            newsletterService.sendNewsletterToAllSubscribers(
                    "Новая статья: " + post.getTitle(),
                    blogPostId
            );

            // Добавляем сообщение об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    "Розсилка успішно надіслана для статті: " + post.getTitle());
        } catch (Exception e) {
            // Обработка ошибок
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Помилка при надсиланні розсилки: " + e.getMessage());
        }

        return "redirect:/admin/newsletter";
    }

    // Создание ручной рассылки
    @GetMapping("/manual")
    public String manualNewsletterForm(Model model) {
        long subscriberCount = newsletterService.getSubscriberCount();
        model.addAttribute("subscriberCount", subscriberCount);
        return "admin/newsletter/manual";
    }

    // Отправка ручной рассылки
    @PostMapping("/manual-send")
    public String sendManualNewsletter(
            String subject,
            String content,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // TODO: Реализовать логику ручной рассылки
             newsletterService.sendManualNewsletter(subject, content);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Ручна розсилка успішно надіслана");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Помилка при надсиланні ручної розсилки: " + e.getMessage());
        }

        return "redirect:/admin/newsletter/manual";
    }
}