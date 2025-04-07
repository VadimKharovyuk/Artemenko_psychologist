package com.example.artemenko_psychologist.controller.admin;

import com.example.artemenko_psychologist.model.PersonalProfile;
import com.example.artemenko_psychologist.service.PersonalProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

@Controller
@RequestMapping("/admin/profiles")
@RequiredArgsConstructor
public class PersonalProfileAdminController {

    private final PersonalProfileService personalProfileService;

    /**
     * Страница со списком профилей
     */
    @GetMapping
    public String listProfiles(Model model) {
        model.addAttribute("profiles", personalProfileService.getAllProfiles());
        return "admin/profiles/list";
    }

    /**
     * Страница создания нового профиля
     */
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("profile", new PersonalProfile());
        return "admin/profiles/create";
    }

    /**
     * Обработка создания профиля
     */
    @PostMapping("/create")
    public String createProfile(
            @RequestParam("fullName") String fullName,
            @RequestParam("personalHistory") String personalHistory,
            @RequestParam("professionalJourney") String professionalJourney,
            @RequestParam("personalReflections") String personalReflections,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            PersonalProfile createdProfile = personalProfileService.createProfile(
                    fullName,
                    personalHistory,
                    professionalJourney,
                    personalReflections,
                    profilePhoto
            );
            redirectAttributes.addFlashAttribute("successMessage", "Профиль успешно создан");
            return "redirect:/admin/profiles";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при загрузке фото: " + e.getMessage());
            return "redirect:/admin/profiles/create";
        }
    }

    /**
     * Страница редактирования профиля
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        PersonalProfile profile = personalProfileService.getProfileById(id);
        model.addAttribute("profile", profile);
        return "admin/profiles/edit";
    }

    /**
     * Обработка обновления профиля
     */
    @PostMapping("/edit/{id}")
    public String updateProfile(
            @PathVariable Long id,
            @RequestParam("fullName") String fullName,
            @RequestParam("personalHistory") String personalHistory,
            @RequestParam("professionalJourney") String professionalJourney,
            @RequestParam("personalReflections") String personalReflections,
            @RequestParam(value = "profilePhoto", required = false) MultipartFile profilePhoto,
            RedirectAttributes redirectAttributes
    ) {
        try {
            PersonalProfile updatedProfile = personalProfileService.updateProfile(
                    id,
                    fullName,
                    personalHistory,
                    professionalJourney,
                    personalReflections,
                    profilePhoto
            );
            redirectAttributes.addFlashAttribute("successMessage", "Профиль успешно обновлен");
            return "redirect:/admin/profiles";
        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при загрузке фото: " + e.getMessage());
            return "redirect:/admin/profiles/edit/" + id;
        }
    }

    /**
     * Удаление профиля
     */
    @GetMapping("/delete/{id}")
    public String deleteProfile(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            personalProfileService.deleteProfile(id);
            redirectAttributes.addFlashAttribute("successMessage", "Профиль успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка при удалении профиля: " + e.getMessage());
        }
        return "redirect:/admin/profiles";
    }
}