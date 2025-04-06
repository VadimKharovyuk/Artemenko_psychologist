package com.example.artemenko_psychologist.controller.admin;

import com.example.artemenko_psychologist.dto.consultation.ConsultationRequestDetailsDto;
import com.example.artemenko_psychologist.dto.consultation.ConsultationRequestListDto;
import com.example.artemenko_psychologist.enums.ConsultationStatus;
import com.example.artemenko_psychologist.service.ConsultationRequestService;
import com.example.artemenko_psychologist.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/consultations")
@RequiredArgsConstructor
public class ConsultationRequestAdminController {

    private final ConsultationRequestService consultationRequestService;
    private final ServiceService serviceEntityService;

    /**
     * Отображает список всех запросов на консультацию
     */
    @GetMapping
    public String listConsultationRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ConsultationStatus status,
            Model model) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ConsultationRequestListDto> requests;

        if (status != null) {
            requests = consultationRequestService.getRequestsByStatus(status, pageRequest);
            model.addAttribute("currentStatus", status);
        } else {
            requests = consultationRequestService.getRequestsPage(pageRequest);
        }

        model.addAttribute("requests", requests);
        model.addAttribute("statuses", ConsultationStatus.values());
        model.addAttribute("newRequestsCount", consultationRequestService.countNewRequests());

        return "admin/consultations/list";
    }

    /**
     * Отображает детали запроса на консультацию
     */
    @GetMapping("/{id}")
    public String viewConsultationRequest(@PathVariable Long id, Model model) {
        ConsultationRequestDetailsDto request = consultationRequestService.getRequestById(id);

        model.addAttribute("request", request);
        model.addAttribute("statuses", ConsultationStatus.values());
        model.addAttribute("services", serviceEntityService.getAllServices());

        return "admin/consultations/details";
    }

    /**
     * Обновляет запрос на консультацию
     */
    @PostMapping("/{id}")
    public String updateConsultationRequest(
            @PathVariable Long id,
            @ModelAttribute ConsultationRequestDetailsDto requestDto,
            RedirectAttributes redirectAttributes) {

        try {
            consultationRequestService.updateRequest(id, requestDto);
            redirectAttributes.addFlashAttribute("successMessage", "Запрос успешно обновлен");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении запроса: " + e.getMessage());
        }

        return "redirect:/admin/consultations/" + id;
    }

    /**
     * Обрабатывает быстрое изменение статуса запроса
     */
    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam ConsultationStatus status,
            @RequestParam(required = false) String returnUrl,
            RedirectAttributes redirectAttributes) {

        try {
            consultationRequestService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Статус запроса изменен на: " + status.getDisplayName());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при изменении статуса: " + e.getMessage());
        }

        // Если указан URL возврата, переходим туда, иначе на страницу запроса
        return returnUrl != null && !returnUrl.isEmpty()
                ? "redirect:" + returnUrl
                : "redirect:/admin/consultations/" + id;
    }

    /**
     * Удаляет запрос на консультацию
     */
    @PostMapping("/{id}/delete")
    public String deleteConsultationRequest(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            consultationRequestService.deleteRequest(id);
            redirectAttributes.addFlashAttribute("successMessage", "Запрос успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении запроса: " + e.getMessage());
        }

        return "redirect:/admin/consultations";
    }

    /**
     * Обработчик для добавления модели к каждому запросу
     */
    @ModelAttribute
    public void addCommonAttributes(Model model) {
        // Добавляем общие атрибуты, которые нужны на всех страницах консультаций
        model.addAttribute("activeMenu", "consultations");
    }
}