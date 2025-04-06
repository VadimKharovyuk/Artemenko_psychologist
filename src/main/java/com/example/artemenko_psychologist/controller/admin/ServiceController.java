package com.example.artemenko_psychologist.controller.admin;

import com.example.artemenko_psychologist.dto.ServiceCreateFormDTO;
import com.example.artemenko_psychologist.dto.ServiceDTO;
import com.example.artemenko_psychologist.dto.ServiceUpdateFormDTO;
import com.example.artemenko_psychologist.exception.ResourceNotFoundException;
import com.example.artemenko_psychologist.service.ServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.io.IOException;

@Controller
@RequestMapping("/admin/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceController {

    private final ServiceService serviceService;

    /**
     * Отображает список всех услуг
     */
    @GetMapping
    public String listServices(Model model) {
        model.addAttribute("services", serviceService.getAllServices());
        return "admin/services/list";
    }

    /**
     * Отображает форму создания новой услуги
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("serviceForm", new ServiceCreateFormDTO());
        model.addAttribute("isNew", true);
        return "admin/services/form";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            // Получаем данные услуги для формы редактирования
            ServiceUpdateFormDTO serviceForm = serviceService.getServiceUpdateFormById(id);

            // Добавляем все необходимые атрибуты в модель
            model.addAttribute("serviceForm", serviceForm);
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Редактирование услуги");

            return "admin/services/form";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/services";
        }
    }

    // Метод для обработки обновления услуги
    @PostMapping("/{id}/update")
    public String updateService(
            @PathVariable Long id,
            @Valid @ModelAttribute("serviceForm") ServiceUpdateFormDTO serviceForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("isEdit", true);
            model.addAttribute("pageTitle", "Редактирование услуги");
            return "admin/services/form";
        }

        try {
            serviceService.updateService(serviceForm);
            redirectAttributes.addFlashAttribute("successMessage", "Услуга успешно обновлена");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении услуги: " + e.getMessage());
        }

        return "redirect:/admin/services";
    }

    /**
     * Обрабатывает создание новой или обновление существующей услуги
     */
    @PostMapping
    public String createOrUpdateService(
            @Valid @ModelAttribute("serviceForm") ServiceCreateFormDTO serviceForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Проверяем наличие ошибок валидации
        if (bindingResult.hasErrors()) {
            model.addAttribute("isNew", serviceForm.getId() == null);
            return "admin/services/form";
        }

        try {
            ServiceDTO savedService = serviceService.createOrUpdateService(serviceForm);
            redirectAttributes.addFlashAttribute("successMessage",
                    serviceForm.getId() == null ?
                            "Услуга успешно создана" :
                            "Услуга \"" + savedService.getTitle() + "\" успешно обновлена");
            return "redirect:/admin/services";
        } catch (IOException e) {
            log.error("Ошибка при загрузке изображения", e);
            model.addAttribute("errorMessage", "Ошибка при загрузке изображения: " + e.getMessage());
            model.addAttribute("isNew", serviceForm.getId() == null);
            return "admin/services/form";
        } catch (Exception e) {
            log.error("Ошибка при сохранении услуги", e);
            model.addAttribute("errorMessage", "Ошибка при сохранении услуги: " + e.getMessage());
            model.addAttribute("isNew", serviceForm.getId() == null);
            return "admin/services/form";
        }
    }

    /**
     * Удаляет услугу
     */
    @PostMapping("/{id}/delete")
    public String deleteService(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            serviceService.deleteService(id);
            redirectAttributes.addFlashAttribute("successMessage", "Услуга успешно удалена");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/services";
    }

    /**
     * Изменяет статус активности услуги
     */
    @PostMapping("/{id}/toggle-active")
    public String toggleServiceActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ServiceDTO updatedService = serviceService.toggleServiceActive(id);
            String status = updatedService.isActive() ? "активирована" : "деактивирована";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Услуга \"" + updatedService.getTitle() + "\" успешно " + status);
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/services";
    }

    /**
     * Обработка ошибок валидации
     */
    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, RedirectAttributes redirectAttributes) {
        log.error("Ошибка в контроллере услуг", e);
        redirectAttributes.addFlashAttribute("errorMessage", "Произошла ошибка: " + e.getMessage());
        return "redirect:/admin/services";
    }
}