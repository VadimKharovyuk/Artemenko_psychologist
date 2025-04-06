package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.dto.ServiceDTO;
import com.example.artemenko_psychologist.exception.ResourceNotFoundException;
import com.example.artemenko_psychologist.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/services")
@RequiredArgsConstructor
public class ClientServicesController {

    private final ServiceService serviceService;

    /**
     * Отображает список всех услуг
     */
    @GetMapping
    public String getAllServices(Model model) {
        List<ServiceDTO> services = serviceService.getAllServices().stream()
                .filter(ServiceDTO::isActive)
                .toList();

        model.addAttribute("services", services);
        model.addAttribute("pageTitle", "Услуги психолога");

        return "client/services/list";
    }

    /**
     * Отображает детальную информацию об услуге
     */
    @GetMapping("/{id}")
    public String getServiceDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            ServiceDTO service = serviceService.getServiceById(id);

            // Проверяем, что услуга активна
            if (!service.isActive()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Данная услуга временно недоступна");
                return "redirect:/services";
            }

            model.addAttribute("service", service);
            model.addAttribute("pageTitle", service.getTitle());

            return "client/services/details";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Услуга не найдена");
            return "redirect:/services";
        }
    }
}