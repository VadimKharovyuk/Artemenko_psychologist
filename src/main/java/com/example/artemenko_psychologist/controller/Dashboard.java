package com.example.artemenko_psychologist.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/admin")
@Controller
public class Dashboard {

  @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }
}
