package com.example.artemenko_psychologist.controller.admin;

import com.example.artemenko_psychologist.model.DocumentPhoto;

import com.example.artemenko_psychologist.service.impl.DocumentPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/photos")
@RequiredArgsConstructor
@Slf4j
public class DocumentPhotoAdminController {

    private final DocumentPhotoService documentPhotoService;

    /**
     * Отображает административную панель управления фотографиями
     */
    @GetMapping
    public String showAdminPhotos(Model model) {
        // Получаем все фотографии (как активные, так и неактивные)
        List<DocumentPhoto> activePhotos = documentPhotoService.getAllActivePhotos();
        List<DocumentPhoto> allPhotos = documentPhotoService.getAllPhotos();

        model.addAttribute("activePhotos", activePhotos);
        model.addAttribute("allPhotos", allPhotos);
        return "admin/photos/list";
    }

    /**
     * Отображает форму загрузки новой фотографии
     */
    @GetMapping("/upload")
    public String showUploadForm() {
        return "admin/photos/photo-upload";
    }

    /**
     * Обрабатывает загрузку новой фотографии
     */
    @PostMapping("/upload")
    public String uploadPhoto(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) {
        try {
            DocumentPhoto savedPhoto = documentPhotoService.uploadPhoto(file);
            redirectAttributes.addFlashAttribute("success",
                    "Фотография успешно загружена с ID: " + savedPhoto.getId());
        } catch (Exception e) {
            log.error("Ошибка при загрузке фотографии: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при загрузке фотографии: " + e.getMessage());
        }

        return "redirect:/admin/photos";
    }

    /**
     * Отображает страницу с детальной информацией о фотографии
     */
    @GetMapping("/{id}")
    public String showPhotoDetails(@PathVariable Long id, Model model) {
        Optional<DocumentPhoto> photoOptional = documentPhotoService.getPhotoById(id);

        if (photoOptional.isEmpty()) {
            model.addAttribute("error", "Фотография с ID " + id + " не найдена");
            return "admin/error";
        }

        model.addAttribute("photo", photoOptional.get());
        return "admin/photos/photo-details";
    }

    /**
     * Обрабатывает удаление фотографии
     */
    @PostMapping("/{id}/delete")
    public String deletePhoto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = documentPhotoService.deletePhoto(id);

        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Фотография успешно удалена");
        } else {
            redirectAttributes.addFlashAttribute("error", "Не удалось удалить фотографию");
        }

        return "redirect:/admin/photos";
    }

    /**
     * Обрабатывает деактивацию фотографии
     */
    @PostMapping("/{id}/deactivate")
    public String deactivatePhoto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            documentPhotoService.deactivatePhoto(id);
            redirectAttributes.addFlashAttribute("success", "Фотография деактивирована");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при деактивации фотографии: " + e.getMessage());
        }

        return "redirect:/admin/photos";
    }

    /**
     * Обрабатывает активацию фотографии
     */
    @PostMapping("/{id}/activate")
    public String activatePhoto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            documentPhotoService.activatePhoto(id);
            redirectAttributes.addFlashAttribute("success", "Фотография активирована");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Ошибка при активации фотографии: " + e.getMessage());
        }

        return "redirect:/admin/photos";
    }
}