package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.service.BlogPostService;
import com.example.artemenko_psychologist.service.ConsultationRequestService;
import com.example.artemenko_psychologist.service.NewsletterService;
import com.example.artemenko_psychologist.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
@RequiredArgsConstructor
@RequestMapping("/admin")
@Controller
public class Dashboard {

    private final ServiceService serviceService;
    private final BlogPostService blogPostService;
    private final ConsultationRequestService consultationRequestService;
    private final NewsletterService newsletterService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        // Добавление статистики
        model.addAttribute("activeServicesCount", serviceService.countActiveServices());
        model.addAttribute("publishedPostsCount", blogPostService.countPublishedPosts());
        model.addAttribute("newConsultationsCount", consultationRequestService.countNewRequests());
        model.addAttribute("subscribersCount", newsletterService.getSubscriberCount());

        return "admin/dashboard";
    }
}
