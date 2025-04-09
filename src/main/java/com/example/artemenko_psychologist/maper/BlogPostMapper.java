package com.example.artemenko_psychologist.maper;
import com.example.artemenko_psychologist.dto.blog.BlogPostCreateDto;
import com.example.artemenko_psychologist.dto.blog.BlogPostListDto;
import com.example.artemenko_psychologist.dto.blog.BlogPostUpdateDto;
import com.example.artemenko_psychologist.model.BlogCategory;
import com.example.artemenko_psychologist.model.BlogPost;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BlogPostMapper {

    // Маппинг для создания нового поста
    public BlogPost toEntity(BlogPostCreateDto dto, BlogCategory category) {
        BlogPost blogPost = new BlogPost();

        // Базовые поля
        blogPost.setTitle(dto.getTitle());
        blogPost.setShortDescription(dto.getShortDescription());
        blogPost.setContent(dto.getContent());
        blogPost.setCategory(category);
        blogPost.setPublicationDate(LocalDateTime.now());

        return blogPost;
    }

    // Маппинг для обновления существующего поста
    public BlogPost toEntity(BlogPostUpdateDto dto, BlogCategory category, BlogPost existingPost) {
        // Обновление базовых полей
        existingPost.setTitle(dto.getTitle());
        existingPost.setShortDescription(dto.getShortDescription());
        existingPost.setContent(dto.getContent());
        existingPost.setCategory(category);

        return existingPost;
    }

    // Преобразование в DTO для списка
    public BlogPostListDto toListDto(BlogPost blogPost) {
        BlogPostListDto dto = new BlogPostListDto();
        dto.setId(blogPost.getId());
        dto.setTitle(blogPost.getTitle());
        dto.setPreviewImageUrl(blogPost.getPreviewImageUrl());
        dto.setShortDescription(blogPost.getShortDescription());
        dto.setContent(blogPost.getContent());

        // Установка имени категории, если категория существует
        if (blogPost.getCategory() != null) {
            dto.setCategoryName(blogPost.getCategory().getName());
        }

        dto.setPublicationDate(blogPost.getPublicationDate());

        return dto;
    }

    // Преобразование списка сущностей в список DTO
    public List<BlogPostListDto> toListDto(List<BlogPost> blogPosts) {
        return blogPosts.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }
}