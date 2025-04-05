package com.example.artemenko_psychologist.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AdminController {

    @Value("${admin.username}")
    private String adminUsername;

    @Value("${admin.password}")
    private String adminPassword;

    @GetMapping("/admin/login")
    public String loginPage() {
        return "admin/login";
    }

    @PostMapping("/admin/login-process")
    public String processLogin(@RequestParam String username,
                               @RequestParam String password,
                               HttpSession session) {
        // Используем свойства из конфигурации вместо хардкода
        if (adminUsername.equals(username) && adminPassword.equals(password)) {
            session.setAttribute("adminAuthenticated", true);
            return "redirect:/admin/dashboard";
        }

        return "redirect:/admin/login?error=true";
    }


    @GetMapping("/admin/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/admin/login?logout=true";
    }
}