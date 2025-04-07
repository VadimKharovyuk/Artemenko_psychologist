package com.example.artemenko_psychologist.controller;

import com.example.artemenko_psychologist.dto.blog.BlogPostListDto;
import com.example.artemenko_psychologist.service.BlogPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/blog")
@RequiredArgsConstructor
public class BlogPostClientController {

    private final BlogPostService blogPostService;

    // Список всех блог-постов
    @GetMapping
    public String listBlogPosts(Model model) {
        List<BlogPostListDto> posts = blogPostService.getAllBlogPosts();
        model.addAttribute("posts", posts);
        return "client/blog/list";
    }


    // Детальный просмотр конкретного поста
    @GetMapping("/{id}")
    public String viewBlogPost(@PathVariable Long id, Model model) {
        BlogPostListDto post = blogPostService.getBlogPostById(id);
        model.addAttribute("post", post);
        return "client/blog/detail";
    }

    // Список постов по категории
    @GetMapping("/category/{categoryId}")
    public String listBlogPostsByCategory(
            @PathVariable Long categoryId,
            Model model
    ) {
        List<BlogPostListDto> posts = blogPostService.getBlogPostsByCategory(categoryId);
        model.addAttribute("posts", posts);
        model.addAttribute("categoryId", categoryId);
        return "client/blog/category-list";
    }

    // Последние посты (например, для главной страницы)
    @GetMapping("/latest")
    public String latestBlogPosts(Model model) {
        List<BlogPostListDto> latestPosts = blogPostService.getLatestBlogPosts(3);
        model.addAttribute("posts", latestPosts);
        return "client/blog/latest";
    }
}