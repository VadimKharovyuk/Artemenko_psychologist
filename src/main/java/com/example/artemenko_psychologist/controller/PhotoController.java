package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.model.ContentPhoto;
import com.example.artemenko_psychologist.repository.ContentPhotoRepository;
import com.example.artemenko_psychologist.util.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final ContentPhotoRepository photoRepository;
    private final CloudinaryService cloudinaryService;

    // Список всех фото
    @GetMapping
    public String listPhotos(Model model) {
        List<ContentPhoto> photos = photoRepository.findAll();
        model.addAttribute("photos", photos);
        return "photos/list";
    }

    // Форма добавления нового фото
    @GetMapping("/add")
    public String addPhotoForm(Model model) {
        model.addAttribute("photo", new ContentPhoto());
        return "photos/add";
    }

    // Загрузка нового фото
    @PostMapping("/add")
    public String addPhoto(@ModelAttribute ContentPhoto photo,
                           @RequestParam("imageFile") MultipartFile imageFile,
                           RedirectAttributes redirectAttributes) {

        try {
            if (imageFile.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Пожалуйста, выберите файл");
                return "redirect:/photos/add";
            }

            // Загружаем изображение в Cloudinary
            CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadImage(imageFile);

            // Заполняем данные фото
            photo.setPhotoUrl(uploadResult.getUrl());
            photo.setPublicId(uploadResult.getPublicId());

            // Сохраняем в БД
            photoRepository.save(photo);

            redirectAttributes.addFlashAttribute("success", "Фото успешно загружено");
            return "redirect:/photos";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке: " + e.getMessage());
            return "redirect:/photos/add";
        }
    }

    // Просмотр фото
    @GetMapping("/{id}")
    public String viewPhoto(@PathVariable Long id, Model model) {
        ContentPhoto photo = photoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Фото не найдено"));

        model.addAttribute("photo", photo);
        return "photos/view";
    }

    // Удаление фото
    @PostMapping("/{id}/delete")
    public String deletePhoto(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            ContentPhoto photo = photoRepository.findById(id)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Фото не найдено"));

            // Удаляем из Cloudinary
            if (photo.getPublicId() != null) {
                cloudinaryService.deleteImage(photo.getPublicId());
            }

            // Удаляем из БД
            photoRepository.delete(photo);

            redirectAttributes.addFlashAttribute("success", "Фото успешно удалено");
            return "redirect:/photos";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при удалении: " + e.getMessage());
            return "redirect:/photos";
        }
    }
}