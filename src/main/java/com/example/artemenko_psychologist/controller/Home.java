package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.dto.consultation.ConsultationRequestCreateDto;
import com.example.artemenko_psychologist.dto.service.ServiceCardDTO;
import com.example.artemenko_psychologist.dto.blog.BlogPostListDto;
import com.example.artemenko_psychologist.dto.service.ServiceDTO;
import com.example.artemenko_psychologist.service.BlogPostService;
import com.example.artemenko_psychologist.service.ConsultationRequestService;
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
        // Получение услуг для карточек
        List<ServiceCardDTO> serviceCardDTO = serviceService.getTopActiveServiceCards(6);
        model.addAttribute("serviceCardDTO", serviceCardDTO);

        // Получение последних блогов
        List<BlogPostListDto> blogPostList = blogPostService.getLatestBlogPosts(3);
        model.addAttribute("blogPostList", blogPostList);

        // Список всех активных услуг для выпадающего списка в форме
        List<ServiceDTO> allActiveServices = serviceService.getAllActiveServices();
        model.addAttribute("allServices", allActiveServices);

        // DTO для формы записи на консультацию
        model.addAttribute("consultationRequestDto", new ConsultationRequestCreateDto());


        //для футера серисы
        List<ServiceDTO> allAServicesFoter = serviceService.getLatestServices(3);
        model.addAttribute("footerServices",allAServicesFoter);

        return "home";
    }
}
