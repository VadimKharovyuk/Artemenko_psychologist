package com.example.artemenko_psychologist.controller.admin;

import com.example.artemenko_psychologist.dto.faq.FaqCreateDto;
import com.example.artemenko_psychologist.dto.faq.FaqDto;
import com.example.artemenko_psychologist.dto.faq.FaqUpdateDto;

import com.example.artemenko_psychologist.service.impl.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/faqs")
@RequiredArgsConstructor
public class FaqAdminController {

    private final FaqService faqService;

    @GetMapping
    public String listFaqs(Model model) {
        List<FaqDto> faqs = faqService.getAllFaqs();
        model.addAttribute("faqs", faqs);
        return "admin/faqs/list";
    }

    @PostMapping
    public String createFaq(@RequestParam("question") String question,
                            @RequestParam("answer") String answer,
                            @RequestParam(value = "active", required = false, defaultValue = "false") boolean active,
                            RedirectAttributes redirectAttributes) {
        try {
            FaqCreateDto createDto = new FaqCreateDto(question, answer, active);
            FaqDto created = faqService.createFaq(createDto);
            redirectAttributes.addFlashAttribute("success", "Вопрос успешно создан");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при создании вопроса: " + e.getMessage());
        }
        return "redirect:/admin/faqs";
    }

    @GetMapping("/{id}/edit")
    public String editFaqForm(@PathVariable Long id, Model model) {
        FaqDto faq = faqService.getFaqById(id);
        model.addAttribute("faq", faq);
        return "admin/faqs/edit";
    }

    @PostMapping("/{id}")
    public String updateFaq(@PathVariable Long id,
                            @RequestParam("question") String question,
                            @RequestParam("answer") String answer,
                            @RequestParam(value = "active", required = false, defaultValue = "false") boolean active,
                            RedirectAttributes redirectAttributes) {
        try {
            FaqUpdateDto updateDto = new FaqUpdateDto(question, answer, active);
            FaqDto updated = faqService.updateFaq(id, updateDto);
            redirectAttributes.addFlashAttribute("success", "Вопрос успешно обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении вопроса: " + e.getMessage());
        }
        return "redirect:/admin/faqs";
    }

    @PostMapping("/{id}/toggle")
    public String toggleFaqActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            FaqDto faq = faqService.toggleActive(id);
            String status = faq.isActive() ? "активирован" : "деактивирован";
            redirectAttributes.addFlashAttribute("success", "Вопрос успешно " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при изменении статуса вопроса: " + e.getMessage());
        }
        return "redirect:/admin/faqs";
    }

    @PostMapping("/{id}/delete")
    public String deleteFaq(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            faqService.deleteFaq(id);
            redirectAttributes.addFlashAttribute("success", "Вопрос успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении вопроса: " + e.getMessage());
        }
        return "redirect:/admin/faqs";
    }
}