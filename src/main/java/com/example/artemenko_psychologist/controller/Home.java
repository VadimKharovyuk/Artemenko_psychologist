package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.dto.ServiceCardDTO;
import com.example.artemenko_psychologist.dto.blog.BlogPostListDto;
import com.example.artemenko_psychologist.service.BlogPostService;
import com.example.artemenko_psychologist.service.ServiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RequiredArgsConstructor
@Controller
public class Home {
    private final ServiceService serviceService ;
    private final BlogPostService blogPostService ;


    @GetMapping
    public String home(Model model) {
        List<ServiceCardDTO> serviceCardDTO = serviceService.getTopActiveServiceCards(6);
        model.addAttribute("serviceCardDTO", serviceCardDTO);

        List<BlogPostListDto> blogPostList =blogPostService.getLatestBlogPosts(3);
        model.addAttribute("blogPostList", blogPostList);

        return "home";
    }
}
