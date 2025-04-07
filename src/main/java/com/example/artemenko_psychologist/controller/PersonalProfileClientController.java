
package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.model.PersonalProfile;
import com.example.artemenko_psychologist.service.PersonalProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/profiles")
@RequiredArgsConstructor
public class PersonalProfileClientController {

    private final PersonalProfileService personalProfileService;

    /**
     * Выводит последний созданный профиль
     */
    @GetMapping
    public String listPublicProfiles(Model model) {
        List<PersonalProfile> profiles = personalProfileService.getAllProfiles();

        // Находим последний профиль (с максимальным ID)
        Optional<PersonalProfile> latestProfile = profiles.stream()
                .max((p1, p2) -> p1.getId().compareTo(p2.getId()));

        if (latestProfile.isPresent()) {
            model.addAttribute("profile", latestProfile.get());
            return "client/profiles/details"; // Возвращаем страницу деталей последнего профиля
        } else {
            // Если профилей нет, можно redirectнуть на страницу с сообщением
            return "redirect:/";
        }
    }


    @GetMapping("/about")
    public String about(Model model) {
        List<PersonalProfile> profiles = personalProfileService.getAllProfiles();

        // Находим последний профиль (с максимальным ID)
        Optional<PersonalProfile> latestProfile = profiles.stream()
                .max((p1, p2) -> p1.getId().compareTo(p2.getId()));

        if (latestProfile.isPresent()) {
            model.addAttribute("profile", latestProfile.get());
        } else {
            // Если профилей нет, можно redirectнуть на страницу с сообщением
            return "redirect:/";
        }
        return "client/profiles/about";
    }
}