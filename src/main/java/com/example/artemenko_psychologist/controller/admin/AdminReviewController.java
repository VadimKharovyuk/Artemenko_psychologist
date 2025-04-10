package com.example.artemenko_psychologist.controller.admin;

import com.example.artemenko_psychologist.dto.reviews.ReviewDetailsDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewListDTO;
import com.example.artemenko_psychologist.service.ReviewService;
import com.example.artemenko_psychologist.service.ServiceService;
import com.example.artemenko_psychologist.service.impl.ReviewPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
public class AdminReviewController {

    private final ReviewPhotoService reviewService;
    private final ServiceService serviceService;

    /**
     * Отображение списка всех отзывов
     */
    @GetMapping
    public String listAllReviews(Model model) {
        List<ReviewListDTO> reviews = reviewService.getAllPhotos();

        // Получение названий услуг для каждого отзыва
        Map<Long, String> serviceNames = reviews.stream()
                .collect(Collectors.toMap(
                        ReviewListDTO::getId,
                        review -> serviceService.getServiceById(review.getServiceId()).getTitle(),
                        (existing, replacement) -> existing
                ));

        model.addAttribute("reviews", reviews);
        model.addAttribute("serviceNames", serviceNames);
        model.addAttribute("pageTitle", "Управление отзывами");

        return "admin/reviews/list";
    }

    /**
     * Просмотр деталей отзыва по ID
     */
    @GetMapping("/{id}")
    public String viewReview(@PathVariable Long id, Model model) {
        ReviewDetailsDTO review = reviewService.getReviewById(id);
        String serviceName = serviceService.getServiceById(review.getServiceId()).getTitle();

        model.addAttribute("review", review);
        model.addAttribute("serviceName", serviceName);
        model.addAttribute("pageTitle", "Просмотр отзыва");

        return "admin/reviews/view";
    }

    /**
     * Удаление отзыва
     */
    @PostMapping("/{id}/delete")
    public String deleteReview(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            reviewService.deleteReview(id);
            redirectAttributes.addFlashAttribute("successMessage", "Отзыв успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении отзыва: " + e.getMessage());
        }

        return "redirect:/admin/reviews";
    }

    /**
     * Подтверждение удаления отзыва
     */
    @GetMapping("/{id}/delete")
    public String confirmDeleteReview(@PathVariable Long id, Model model) {
        ReviewDetailsDTO review = reviewService.getReviewById(id);
        String serviceName = serviceService.getServiceById(review.getServiceId()).getTitle();

        model.addAttribute("review", review);
        model.addAttribute("serviceName", serviceName);
        model.addAttribute("pageTitle", "Подтверждение удаления отзыва");

        return "admin/reviews/delete-confirm";
    }
}