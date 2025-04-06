package com.example.artemenko_psychologist.controller.admin;

import com.example.artemenko_psychologist.model.BlogCategory;
import com.example.artemenko_psychologist.service.BlogCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/blog-categories")
@RequiredArgsConstructor
public class BlogCategoryAdminController {

    private final BlogCategoryService blogCategoryService;

    // Список всех категорий
    @GetMapping
    public String listCategories(Model model) {
        model.addAttribute("categories", blogCategoryService.getAllCategories());
        return "admin/blog-categories/list";
    }

    // Форма создания новой категории
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("category", new BlogCategory());
        return "admin/blog-categories/create";
    }

    // Обработка создания категории
    @PostMapping("/create")
    public String createCategory(@ModelAttribute BlogCategory category) {
        blogCategoryService.createCategory(category);
        return "redirect:/admin/blog-categories";
    }

    // Форма редактирования категории
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        BlogCategory category = blogCategoryService.getCategoryById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
        model.addAttribute("category", category);
        return "admin/blog-categories/edit";
    }

    // Обработка обновления категории
    @PostMapping("/edit/{id}")
    public String updateCategory(@PathVariable Long id,
                                 @ModelAttribute BlogCategory categoryDetails) {
        blogCategoryService.updateCategory(id, categoryDetails);
        return "redirect:/admin/blog-categories";
    }

    // Удаление категории
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        blogCategoryService.deleteCategory(id);
        return "redirect:/admin/blog-categories";
    }

    // Обработка ошибок
    @ExceptionHandler(RuntimeException.class)
    public String handleException(RuntimeException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "admin/error";
    }
}