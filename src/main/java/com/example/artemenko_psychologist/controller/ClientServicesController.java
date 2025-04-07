package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.dto.consultation.ConsultationRequestCreateDto;
import com.example.artemenko_psychologist.dto.service.ServiceDTO;
import com.example.artemenko_psychologist.exception.ResourceNotFoundException;
import com.example.artemenko_psychologist.service.ConsultationRequestService;
import com.example.artemenko_psychologist.service.ReviewService;
import com.example.artemenko_psychologist.service.ServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/services")
@RequiredArgsConstructor
public class ClientServicesController {

    private final ServiceService serviceService;
    private final ConsultationRequestService consultationRequestService;
    private final ReviewService reviewService;

    /**
     * Отображает список всех услуг
     */
    @GetMapping
    public String getAllServices(Model model) {
        List<ServiceDTO> services = serviceService.getAllServices().stream()
                .filter(ServiceDTO::isActive)
                .toList();

        // Добавляем количество отзывов для каждой услуги
        Map<Long, Long> reviewsCountMap = new HashMap<>();
        for (ServiceDTO service : services) {
            long count = reviewService.countReviewsByServiceId(service.getId());
            reviewsCountMap.put(service.getId(), count);
        }

        model.addAttribute("services", services);
        model.addAttribute("pageTitle", "Услуги психолога");
        model.addAttribute("reviewsCountMap", reviewsCountMap);
        return "client/services/list";
    }

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

            // Проверяем, был ли передан flash-атрибут с DTO при ошибке валидации
            if (!model.containsAttribute("consultationRequestDto")) {
                // Если нет, создаем новый и устанавливаем serviceId
                ConsultationRequestCreateDto dto = new ConsultationRequestCreateDto();
                dto.setServiceId(id);
                model.addAttribute("consultationRequestDto", dto);
            }

            return "client/services/details";
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Услуга не найдена");
            return "redirect:/services";
        }
    }
    @PostMapping("/consultation-request")
    public String createConsultationRequest(
            @Valid @ModelAttribute ConsultationRequestCreateDto consultationRequestDto,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Проверка наличия serviceId
        if (consultationRequestDto.getServiceId() == null) {
            bindingResult.rejectValue("serviceId", "error.serviceId", "Услуга не выбрана");
            redirectAttributes.addFlashAttribute("errorMessage", "Не указана услуга. Пожалуйста, попробуйте снова.");
            return "redirect:/services";
        }

        // Проверка валидации
        if (bindingResult.hasErrors()) {
            // Сохраняем DTO и ошибки во flash-атрибутах для отображения после редиректа
            redirectAttributes.addFlashAttribute("consultationRequestDto", consultationRequestDto);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.consultationRequestDto", bindingResult);
            return "redirect:/services/" + consultationRequestDto.getServiceId();
        }

        try {
            // Создание запроса на консультацию
            consultationRequestService.createRequest(consultationRequestDto);

            // Добавление сообщения об успехе
            redirectAttributes.addFlashAttribute("successMessage",
                    "Ваша заявка успешно отправлена! Мы свяжемся с вами в ближайшее время.");

            return "redirect:/services/" + consultationRequestDto.getServiceId() ;
        } catch (Exception e) {
            // Обработка ошибок при создании запроса
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Произошла ошибка при отправке заявки. Пожалуйста, попробуйте позже.");
            return "redirect:/services/" + consultationRequestDto.getServiceId();
        }
    }
}