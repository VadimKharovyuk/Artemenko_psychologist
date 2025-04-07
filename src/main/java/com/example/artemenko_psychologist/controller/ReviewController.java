package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.dto.reviews.ReviewCreateDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewDetailsDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewListDTO;

import com.example.artemenko_psychologist.model.Service;
import com.example.artemenko_psychologist.repository.ServiceRepository;
import com.example.artemenko_psychologist.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final ServiceRepository serviceRepository;

    /**
     * Показать все отзывы
     */
    @GetMapping
    public String getAllReviews(Model model) {
        List<ReviewListDTO> reviews = reviewService.getAllReviews();
        model.addAttribute("reviews", reviews);
        return "client/reviews/list";
    }

    /**
     * Показать последние отзывы на главной странице
     */
    @GetMapping("/latest")
    public String getLatestReviews(Model model, @RequestParam(defaultValue = "3") int limit) {
        List<ReviewListDTO> latestReviews = reviewService.getLatestReviews(limit);
        model.addAttribute("latestReviews", latestReviews);
        return "client/reviews/latest";
    }


    /**
     * Показать отзывы для конкретной услуги
     */
    @GetMapping("/service/{serviceId}")
    public String getServiceReviews(@PathVariable Long serviceId, Model model) {
        List<ReviewListDTO> reviews = reviewService.getReviewsByServiceId(serviceId);
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new IllegalArgumentException("Услуга не найдена"));

        // Создаем DTO с правильным serviceId
        ReviewCreateDTO newReview = new ReviewCreateDTO();
        newReview.setServiceId(serviceId);

        model.addAttribute("reviews", reviews);
        model.addAttribute("service", service);
        model.addAttribute("newReview", newReview);
        return "client/reviews/service";
    }

    /**
     * Показать детальную страницу отзыва
     */
    @GetMapping("/{id}")
    public String getReviewDetails(@PathVariable Long id, Model model) {
        ReviewDetailsDTO review = reviewService.getReviewById(id);
        model.addAttribute("review", review);
        return "client/reviews/details";
    }

    /**
     * Форма для создания нового отзыва
     */
    @GetMapping("/new")
    public String showNewReviewForm(Model model, @RequestParam(required = false) Long serviceId) {
        model.addAttribute("review", new ReviewCreateDTO());

        if (serviceId != null) {
            model.addAttribute("serviceId", serviceId);
            Service service = serviceRepository.findById(serviceId)
                    .orElseThrow(() -> new IllegalArgumentException("Услуга не найдена"));
            model.addAttribute("service", service);
        } else {
            // Загружаем все доступные услуги для выбора
            model.addAttribute("services", serviceRepository.findAll());
        }

        return "client/reviews/new";
    }

    /**
     * Обработка создания нового отзыва
     */

    @PostMapping("/new")
    public String createReview(@Valid @ModelAttribute("review") ReviewCreateDTO reviewDTO,
                               BindingResult bindingResult,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        System.out.println("ServiceId в контроллере: " + reviewDTO.getServiceId());

        if (bindingResult.hasErrors()) {
            // Загружаем данные для формы в случае ошибки
            if (reviewDTO.getServiceId() != null) {
                Service service = serviceRepository.findById(reviewDTO.getServiceId())
                        .orElseThrow(() -> new IllegalArgumentException("Услуга не найдена"));
                model.addAttribute("service", service);
            } else {
                model.addAttribute("services", serviceRepository.findAll());
            }
            return "client/reviews/new";
        }

        try {
            ReviewDetailsDTO createdReview = reviewService.createReview(reviewDTO, image);
            redirectAttributes.addFlashAttribute("successMessage", "Отзыв успешно создан!");
            return "redirect:/reviews/service/" + reviewDTO.getServiceId();
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при загрузке изображения: " + e.getMessage());
            return "redirect:/reviews/new?serviceId=" + reviewDTO.getServiceId();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при создании отзыва: " + e.getMessage());
            return "redirect:/reviews/new?serviceId=" + reviewDTO.getServiceId();
        }
    }


    /**
     * Обработка обновления отзыва (только для администратора)
     */
    @PostMapping("/{id}/edit")
    public String updateReview(@PathVariable Long id,
                               @Valid @ModelAttribute("review") ReviewCreateDTO reviewDTO,
                               BindingResult bindingResult,
                               @RequestParam(value = "image", required = false) MultipartFile image,
                               RedirectAttributes redirectAttributes,
                               Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("reviewId", id);
            model.addAttribute("services", serviceRepository.findAll());
            return "reviews/edit";
        }

        try {
            ReviewDetailsDTO updatedReview = reviewService.updateReview(id, reviewDTO, image);
            redirectAttributes.addFlashAttribute("successMessage", "Отзыв успешно обновлен!");
            return "redirect:/reviews/" + id;
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при загрузке изображения: " + e.getMessage());
            return "redirect:/reviews/" + id + "/edit";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при обновлении отзыва: " + e.getMessage());
            return "redirect:/reviews/" + id + "/edit";
        }
    }

}