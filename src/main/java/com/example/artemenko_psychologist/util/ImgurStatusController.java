package com.example.artemenko_psychologist.util;

import com.example.artemenko_psychologist.util.ImgurService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/admin/imgur-status")
@RequiredArgsConstructor
public class ImgurStatusController {

    private final ImgurService imgurService;

    /**
     * Отображение страницы со статусом лимитов Imgur API
     */
    @GetMapping
    public String showImgurStatus(Model model) {
        Map<String, Object> rateLimits = imgurService.getRateLimitInfo();
        model.addAttribute("rateLimits", rateLimits);
        model.addAttribute("pageTitle", "Статус API Imgur");

        return "admin/imgur-status";
    }
}