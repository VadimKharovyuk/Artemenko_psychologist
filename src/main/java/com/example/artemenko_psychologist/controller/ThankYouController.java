package com.example.artemenko_psychologist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/thank-you")
public class ThankYouController {

    @GetMapping("/subscription")
    public String showThankYouPage(Model model, RedirectAttributes redirectAttributes) {
        // Проверяем наличие флеш-атрибута с сообщением об успешной подписке
        if (model.containsAttribute("subscriptionSuccess")) {
            return "client/thank-you";
        }
        // Если прямой переход на страницу без флеш-атрибута - редирект на главную
        return "redirect:/";
    }
}