package com.example.artemenko_psychologist.controller.admin;

import com.example.artemenko_psychologist.dto.blog.BlogPostCreateDto;
import com.example.artemenko_psychologist.dto.blog.BlogPostListDto;
import com.example.artemenko_psychologist.dto.blog.BlogPostUpdateDto;
import com.example.artemenko_psychologist.model.BlogCategory;
import com.example.artemenko_psychologist.service.BlogCategoryService;
import com.example.artemenko_psychologist.service.BlogPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/admin/blog-posts")
@RequiredArgsConstructor
public class BlogPostAdminController {

    private final BlogPostService blogPostService;
    private final BlogCategoryService blogCategoryService;

    // Список всех блог-постов
    @GetMapping
    public String listBlogPosts(Model model) {
        List<BlogPostListDto> posts = blogPostService.getAllBlogPosts();
        model.addAttribute("posts", posts);
        return "admin/blog-posts/list";
    }

    // Форма создания нового блог-поста
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        // Если модель не содержит атрибута, добавляем
        if (!model.containsAttribute("blogPostCreateDto")) {
            model.addAttribute("blogPostCreateDto", new BlogPostCreateDto());
        }

        // Загрузка категорий для выпадающего списка
        List<BlogCategory> categories = blogCategoryService.getAllCategories();
        model.addAttribute("categories", categories);

        return "admin/blog-posts/create";
    }

    // Обработка создания блог-поста
    @PostMapping("/create")
    public String createBlogPost(
            @ModelAttribute("blogPostCreateDto") BlogPostCreateDto blogPostCreateDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // Базовая валидация
        if (bindingResult.hasErrors()) {
            // Сохраняем ошибки для отображения на форме
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.blogPostCreateDto", bindingResult);
            redirectAttributes.addFlashAttribute("blogPostCreateDto", blogPostCreateDto);
            return "redirect:/admin/blog-posts/create";
        }

        try {
            // Создание поста
            BlogPostListDto createdPost = blogPostService.createBlogPost(blogPostCreateDto);

            // Уведомление об успешном создании
            redirectAttributes.addFlashAttribute("successMessage", "Блог-пост успешно создан");

            return "redirect:/admin/blog-posts";
        } catch (IOException e) {
            // Обработка ошибок загрузки изображения
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка загрузки изображения: " + e.getMessage());
            return "redirect:/admin/blog-posts/create";
        } catch (RuntimeException e) {
            // Обработка других ошибок (например, категория не найдена)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/blog-posts/create";
        }
    }

    // Форма редактирования блог-поста
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        try {
            // Получаем существующий пост
            BlogPostListDto existingPost = blogPostService.getBlogPostById(id);

            // Подготавливаем DTO для редактирования
            BlogPostUpdateDto updateDto = new BlogPostUpdateDto();
            updateDto.setTitle(existingPost.getTitle());
            updateDto.setShortDescription(existingPost.getShortDescription());

            // Добавляем атрибуты в модель
            model.addAttribute("blogPostUpdateDto", updateDto);
            model.addAttribute("postId", id);

            // Загрузка категорий для выпадающего списка
            List<BlogCategory> categories = blogCategoryService.getAllCategories();
            model.addAttribute("categories", categories);

            // Передаем текущий URL изображения
            model.addAttribute("currentImageUrl", existingPost.getPreviewImageUrl());

            return "admin/blog-posts/edit";
        } catch (RuntimeException e) {
            // Обработка ошибки, если пост не найден
            return "redirect:/admin/blog-posts?error=PostNotFound";
        }
    }

    // Обработка обновления блог-поста
    @PostMapping("/edit/{id}")
    public String updateBlogPost(
            @PathVariable Long id,
            @ModelAttribute("blogPostUpdateDto") BlogPostUpdateDto blogPostUpdateDto,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        // Базовая валидация
        if (bindingResult.hasErrors()) {
            // Сохраняем ошибки для отображения на форме
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.blogPostUpdateDto", bindingResult);
            redirectAttributes.addFlashAttribute("blogPostUpdateDto", blogPostUpdateDto);
            return "redirect:/admin/blog-posts/edit/" + id;
        }

        try {
            // Обновление поста
            BlogPostListDto updatedPost = blogPostService.updateBlogPost(id, blogPostUpdateDto);

            // Уведомление об успешном обновлении
            redirectAttributes.addFlashAttribute("successMessage", "Блог-пост успешно обновлен");

            return "redirect:/admin/blog-posts";
        } catch (IOException e) {
            // Обработка ошибок загрузки изображения
            redirectAttributes.addFlashAttribute("errorMessage", "Ошибка загрузки изображения: " + e.getMessage());
            return "redirect:/admin/blog-posts/edit/" + id;
        } catch (RuntimeException e) {
            // Обработка других ошибок (например, пост или категория не найдены)
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/admin/blog-posts/edit/" + id;
        }
    }

    // Удаление блог-поста
    @GetMapping("/delete/{id}")
    public String deleteBlogPost(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {
        try {
            // Удаление поста
            blogPostService.deleteBlogPost(id);

            // Уведомление об успешном удалении
            redirectAttributes.addFlashAttribute("successMessage", "Блог-пост успешно удален");
        } catch (RuntimeException e) {
            // Обработка ошибок при удалении
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/blog-posts";
    }
}