package com.example.artemenko_psychologist.maper;

import com.example.artemenko_psychologist.dto.blog.BlogPostCreateDto;
import com.example.artemenko_psychologist.dto.blog.BlogPostListDto;
import com.example.artemenko_psychologist.dto.blog.BlogPostUpdateDto;
import com.example.artemenko_psychologist.model.BlogCategory;
import com.example.artemenko_psychologist.model.BlogPost;
import com.example.artemenko_psychologist.util.ImgurService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BlogPostMapper {

    private final ImgurService imgurService;

    // Маппинг для создания нового поста
    public BlogPost toEntity(BlogPostCreateDto dto, BlogCategory category) throws IOException {
        BlogPost blogPost = new BlogPost();

        // Базовые поля
        blogPost.setTitle(dto.getTitle());
        blogPost.setShortDescription(dto.getShortDescription());
        blogPost.setContent(dto.getContent());
        blogPost.setCategory(category);
        blogPost.setPublicationDate(LocalDateTime.now());

        // Обработка изображения, если оно есть
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            // Загрузка изображения в Imgur
            ImgurService.UploadResult uploadResult = imgurService.uploadImage(dto.getImageFile());

            // Установка URL и deleteHash
            blogPost.setPreviewImageUrl(uploadResult.getUrl());
            blogPost.setDeleteHash(uploadResult.getDeleteHash());
        }

        return blogPost;
    }

    // Маппинг для обновления существующего поста
    public BlogPost toEntity(BlogPostUpdateDto dto, BlogCategory category, BlogPost existingPost) throws IOException {
        // Обновление базовых полей
        existingPost.setTitle(dto.getTitle());
        existingPost.setShortDescription(dto.getShortDescription());
        existingPost.setContent(dto.getContent());
        existingPost.setCategory(category);

        // Обработка изображения, если новое изображение загружено
        if (dto.getImageFile() != null && !dto.getImageFile().isEmpty()) {
            // Удаление старого изображения, если оно существует
            if (existingPost.getDeleteHash() != null) {
                imgurService.deleteImage(existingPost.getDeleteHash());
            }

            // Загрузка нового изображения
            ImgurService.UploadResult uploadResult = imgurService.uploadImage(dto.getImageFile());

            // Обновление URL и deleteHash
            existingPost.setPreviewImageUrl(uploadResult.getUrl());
            existingPost.setDeleteHash(uploadResult.getDeleteHash());
        }

        return existingPost;
    }

    // Преобразование в DTO для списка
    public BlogPostListDto toListDto(BlogPost blogPost) {
        BlogPostListDto dto = new BlogPostListDto();
        dto.setId(blogPost.getId());
        dto.setTitle(blogPost.getTitle());
        dto.setPreviewImageUrl(blogPost.getPreviewImageUrl());
        dto.setShortDescription(blogPost.getShortDescription());
        dto.setCategoryName(blogPost.getCategory() != null ? blogPost.getCategory().getName() : null);
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