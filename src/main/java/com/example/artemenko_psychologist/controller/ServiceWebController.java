package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.dto.ServiceDTO;
import com.example.artemenko_psychologist.exception.ResourceNotFoundException;
import com.example.artemenko_psychologist.service.ServiceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/services")
@RequiredArgsConstructor
@Slf4j
public class ServiceWebController {

    private final ServiceService serviceService;

    /**
     * Главная страница услуг
     */
    @GetMapping
    public String listServices(Model model) {
        try {
            // Получаем список всех услуг
            List<ServiceDTO> services = serviceService.getAllServices();
            log.info("Loaded {} services", services.size());

            model.addAttribute("services", services);
            return "services/list";
        } catch (Exception e) {
            log.error("Error loading services", e);
            model.addAttribute("errorMessage", "Не удалось загрузить список услуг: " + e.getMessage());
            return "error";
        }
    }

    /**
     * Страница создания новой услуги
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("service", new ServiceDTO());
        return "services/form";
    }

    /**
     * Страница редактирования услуги
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            ServiceDTO service = serviceService.getServiceById(id);
            model.addAttribute("service", service);
            return "services/form";
        } catch (ResourceNotFoundException e) {
            log.error("Service not found with id {}", id, e);
            return "redirect:/services?error=notfound";
        }
    }

    /**
     * Сохранение услуги (создание или обновление)
     */
    @PostMapping("/save")
    public String saveService(
            @ModelAttribute("service") ServiceDTO serviceDTO,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Установка файла изображения, если предоставлен
            if (imageFile != null && !imageFile.isEmpty()) {
                serviceDTO.setImageFile(imageFile);
            }

            // Сохранение услуги
            ServiceDTO savedService = serviceService.saveService(serviceDTO);

            log.info("Service saved successfully: {}", savedService.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    savedService.getId() == null
                            ? "Услуга успешно создана"
                            : "Услуга успешно обновлена");

            return "redirect:/services";
        } catch (IOException e) {
            log.error("Error saving service", e);
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Ошибка при сохранении услуги: " + e.getMessage());
            return "redirect:/services/create";
        }
    }

    /**
     * Удаление услуги
     */
    @GetMapping("/delete/{id}")
    public String deleteService(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            serviceService.deleteService(id);
            redirectAttributes.addFlashAttribute("successMessage", "Услуга успешно удалена");
        } catch (ResourceNotFoundException e) {
            log.error("Service not found for deletion", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Услуга не найдена");
        }
        return "redirect:/services";
    }

    /**
     * Обработчик ошибок
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        log.error("Unexpected error", e);
        model.addAttribute("errorMessage", "Произошла непредвиденная ошибка: " + e.getMessage());
        return "error";
    }
}