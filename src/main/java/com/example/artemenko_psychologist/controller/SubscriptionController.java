package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.dto.subscription.SubscriptionDto;
import com.example.artemenko_psychologist.service.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/subscribe")
@RequiredArgsConstructor
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

//    @PostMapping
//    public String subscribe(
//            @Valid SubscriptionDto subscriptionDto,
//            BindingResult bindingResult,
//            RedirectAttributes redirectAttributes
//    ) {
//        // Проверка валидации
//        if (bindingResult.hasErrors()) {
//            // Сохраняем невалидный DTO для повторного отображения формы
//            redirectAttributes.addFlashAttribute("subscriptionDto", subscriptionDto);
//            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.subscriptionDto", bindingResult);
//            redirectAttributes.addFlashAttribute("errorMessage", "Невірний формат електронної пошти");
//            return "redirect:/";
//        }
//
//        try {
//            // Попытка подписки
//            boolean subscribed = subscriptionService.subscribe(subscriptionDto);
//
//            // Установка сообщения
//            if (subscribed) {
//                redirectAttributes.addFlashAttribute("successMessage", "Ви успішно підписалися на розсилку!");
//            } else {
//                redirectAttributes.addFlashAttribute("warningMessage", "Ця електронна пошта вже підписана.");
//            }
//        } catch (Exception e) {
//            redirectAttributes.addFlashAttribute("errorMessage", "Виникла помилка при підписці. Спробуйте пізніше.");
//        }
//
//        return "redirect:/";
//    }


        @PostMapping
        public String subscribe(
                @Valid SubscriptionDto subscriptionDto,
                BindingResult bindingResult,
                RedirectAttributes redirectAttributes
        ) {
            // Проверка валидации
            if (bindingResult.hasErrors()) {
                // Сохраняем невалидный DTO для повторного отображения формы
                redirectAttributes.addFlashAttribute("subscriptionDto", subscriptionDto);
                redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.subscriptionDto", bindingResult);
                redirectAttributes.addFlashAttribute("errorMessage", "Невірний формат електронної пошти");
                return "redirect:/";
            }

            try {
                // Попытка подписки
                boolean subscribed = subscriptionService.subscribe(subscriptionDto);

                // Установка сообщения
                if (subscribed) {
                    // Добавляем флеш-атрибут для страницы благодарности
                    redirectAttributes.addFlashAttribute("subscriptionSuccess", true);
                    redirectAttributes.addFlashAttribute("subscribedEmail", subscriptionDto.getEmail());
                    return "redirect:/thank-you/subscription";
                } else {
                    redirectAttributes.addFlashAttribute("warningMessage", "Ця електронна пошта вже підписана.");
                    return "redirect:/";
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("errorMessage", "Виникла помилка при підписці. Спробуйте пізніше.");
                return "redirect:/";
            }
        }
    }
