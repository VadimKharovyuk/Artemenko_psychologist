package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.model.BlogCategory;
import com.example.artemenko_psychologist.repository.BlogCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BlogCategoryService {
    private final BlogCategoryRepository categoryRepository;

    @Transactional
    public BlogCategory createCategory(BlogCategory category) {
        // Проверка на существование категории с таким именем
        if (categoryRepository.existsByName(category.getName())) {
            throw new RuntimeException("Категория с таким именем уже существует");
        }
        return categoryRepository.save(category);
    }

    public List<BlogCategory> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Optional<BlogCategory> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional
    public BlogCategory updateCategory(Long id, BlogCategory categoryDetails) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        category.setName(categoryDetails.getName());
        return categoryRepository.save(category);
    }

    @Transactional
    public void deleteCategory(Long id) {
        BlogCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        categoryRepository.delete(category);
    }
}