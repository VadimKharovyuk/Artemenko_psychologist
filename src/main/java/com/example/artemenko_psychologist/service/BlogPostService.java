////package com.example.artemenko_psychologist.service;
////
////
////import com.example.artemenko_psychologist.dto.blog.BlogPostCreateDto;
////import com.example.artemenko_psychologist.dto.blog.BlogPostListDto;
////import com.example.artemenko_psychologist.dto.blog.BlogPostUpdateDto;
////import com.example.artemenko_psychologist.maper.BlogPostMapper;
////import com.example.artemenko_psychologist.model.BlogCategory;
////import com.example.artemenko_psychologist.model.BlogPost;
////import com.example.artemenko_psychologist.repository.BlogCategoryRepository;
////import com.example.artemenko_psychologist.repository.BlogPostRepository;
////import com.example.artemenko_psychologist.util.ImgurService;
////import lombok.RequiredArgsConstructor;
////import org.springframework.data.domain.PageRequest;
////import org.springframework.data.domain.Sort;
////import org.springframework.stereotype.Service;
////import org.springframework.transaction.annotation.Transactional;
////
////import java.io.IOException;
////import java.util.List;
////
////@Service
////@RequiredArgsConstructor
////public class BlogPostService {
////    private final BlogPostRepository blogPostRepository;
////    private final BlogCategoryRepository categoryRepository;
////    private final BlogPostMapper blogPostMapper;
////    private final ImgurService imgurService;
////
////    @Transactional
////    public BlogPostListDto createBlogPost(BlogPostCreateDto createDto) throws IOException {
////        // Находим категорию
////        BlogCategory category = categoryRepository.findById(createDto.getCategoryId())
////                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
////
////        // Преобразуем DTO в сущность с загрузкой изображения
////        BlogPost blogPost = blogPostMapper.toEntity(createDto, category);
////
////        // Сохраняем
////        BlogPost savedPost = blogPostRepository.save(blogPost);
////
////        // Возвращаем DTO
////        return blogPostMapper.toListDto(savedPost);
////    }
////
////    @Transactional(readOnly = true)
////    public List<BlogPostListDto> getAllBlogPosts() {
////        List<BlogPost> posts = blogPostRepository.findAll(
////                PageRequest.of(0, 100, Sort.by("publicationDate").descending())
////        ).getContent();
////        return blogPostMapper.toListDto(posts);
////    }
////
////    @Transactional(readOnly = true)
////    public BlogPostListDto getBlogPostById(Long id) {
////        BlogPost post = blogPostRepository.findById(id)
////                .orElseThrow(() -> new RuntimeException("Блог-пост не найден"));
////        return blogPostMapper.toListDto(post);
////    }
////
////    @Transactional
////    public BlogPostListDto updateBlogPost(Long id, BlogPostUpdateDto updateDto) throws IOException {
////        // Находим существующий пост
////        BlogPost existingPost = blogPostRepository.findById(id)
////                .orElseThrow(() -> new RuntimeException("Блог-пост не найден"));
////
////        // Находим категорию
////        BlogCategory category = categoryRepository.findById(updateDto.getCategoryId())
////                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
////
////        // Обновляем сущность
////        BlogPost updatedPost = blogPostMapper.toEntity(updateDto, category, existingPost);
////
////        // Сохраняем
////        BlogPost savedPost = blogPostRepository.save(updatedPost);
////
////        // Возвращаем DTO
////        return blogPostMapper.toListDto(savedPost);
////    }
////
////    @Transactional
////    public void deleteBlogPost(Long id) {
////        BlogPost post = blogPostRepository.findById(id)
////                .orElseThrow(() -> new RuntimeException("Блог-пост не найден"));
////
////        // Удаляем изображение из Imgur, если оно есть
////        if (post.getDeleteHash() != null) {
////            imgurService.deleteImage(post.getDeleteHash());
////        }
////
////        // Удаляем пост
////        blogPostRepository.delete(post);
////    }
////
////    @Transactional(readOnly = true)
////    public List<BlogPostListDto> getLatestBlogPosts(int limit) {
////        List<BlogPost> latestPosts = blogPostRepository.findAll(
////                PageRequest.of(0, limit, Sort.by("publicationDate").descending())
////        ).getContent();
////        return blogPostMapper.toListDto(latestPosts);
////    }
////
////    @Transactional(readOnly = true)
////    public List<BlogPostListDto> getBlogPostsByCategory(Long categoryId) {
////        List<BlogPost> posts = blogPostRepository.findAll(
////                        PageRequest.of(0, 100, Sort.by("publicationDate").descending())
////                ).getContent().stream()
////                .filter(post -> post.getCategory() != null &&
////                        post.getCategory().getId().equals(categoryId))
////                .toList();
////        return blogPostMapper.toListDto(posts);
////    }
////
////    public long countPublishedPosts() {
////      return   blogPostRepository.count();
////    }
////}
//package com.example.artemenko_psychologist.service;
//
//import com.example.artemenko_psychologist.dto.blog.BlogPostCreateDto;
//import com.example.artemenko_psychologist.dto.blog.BlogPostListDto;
//import com.example.artemenko_psychologist.dto.blog.BlogPostUpdateDto;
//import com.example.artemenko_psychologist.maper.BlogPostMapper;
//import com.example.artemenko_psychologist.model.BlogCategory;
//import com.example.artemenko_psychologist.model.BlogPost;
//import com.example.artemenko_psychologist.repository.BlogCategoryRepository;
//import com.example.artemenko_psychologist.repository.BlogPostRepository;
//import com.example.artemenko_psychologist.util.ImgurService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.data.domain.Sort;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//public class BlogPostService {
//    private final BlogPostRepository blogPostRepository;
//    private final BlogCategoryRepository categoryRepository;
//    private final BlogPostMapper blogPostMapper;
//    private final ImgurService imgurService;
//
//    @Transactional
//    public BlogPostListDto createBlogPost(BlogPostCreateDto createDto) throws IOException {
//        // Находим категорию
//        BlogCategory category = categoryRepository.findById(createDto.getCategoryId())
//                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
//
//        // Преобразуем DTO в сущность
//        BlogPost blogPost = blogPostMapper.toEntity(createDto, category);
//
//        // Обработка изображения, если оно есть
//        if (createDto.getImageFile() != null && !createDto.getImageFile().isEmpty()) {
//            // Загрузка изображения в Imgur
//            ImgurService.UploadResult uploadResult = imgurService.uploadImage(createDto.getImageFile());
//
//            // Установка URL и deleteHash
//            blogPost.setPreviewImageUrl(uploadResult.getUrl());
//            blogPost.setDeleteHash(uploadResult.getDeleteHash());
//        }
//
//        // Сохраняем
//        BlogPost savedPost = blogPostRepository.save(blogPost);
//
//        // Возвращаем DTO
//        return blogPostMapper.toListDto(savedPost);
//    }
//
//    @Transactional(readOnly = true)
//    public List<BlogPostListDto> getAllBlogPosts() {
//        List<BlogPost> posts = blogPostRepository.findAll(
//                PageRequest.of(0, 100, Sort.by("publicationDate").descending())
//        ).getContent();
//        return blogPostMapper.toListDto(posts);
//    }
//
//    @Transactional(readOnly = true)
//    public BlogPostListDto getBlogPostById(Long id) {
//        BlogPost post = blogPostRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Блог-пост не найден"));
//        return blogPostMapper.toListDto(post);
//    }
//
//    @Transactional
//    public BlogPostListDto updateBlogPost(Long id, BlogPostUpdateDto updateDto) throws IOException {
//        // Находим существующий пост
//        BlogPost existingPost = blogPostRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Блог-пост не найден"));
//
//        // Находим категорию
//        BlogCategory category = categoryRepository.findById(updateDto.getCategoryId())
//                .orElseThrow(() -> new RuntimeException("Категория не найдена"));
//
//        // Обновляем базовые поля сущности
//        BlogPost updatedPost = blogPostMapper.toEntity(updateDto, category, existingPost);
//
//        // Обработка изображения, если новое изображение загружено
//        if (updateDto.getImageFile() != null && !updateDto.getImageFile().isEmpty()) {
//            // Удаление старого изображения, если оно существует
//            if (existingPost.getDeleteHash() != null) {
//                imgurService.deleteImage(existingPost.getDeleteHash());
//            }
//
//            // Загрузка нового изображения
//            ImgurService.UploadResult uploadResult = imgurService.uploadImage(updateDto.getImageFile());
//
//            // Обновление URL и deleteHash
//            updatedPost.setPreviewImageUrl(uploadResult.getUrl());
//            updatedPost.setDeleteHash(uploadResult.getDeleteHash());
//        }
//
//        // Сохраняем
//        BlogPost savedPost = blogPostRepository.save(updatedPost);
//
//        // Возвращаем DTO
//        return blogPostMapper.toListDto(savedPost);
//    }
//
//    @Transactional
//    public void deleteBlogPost(Long id) {
//        BlogPost post = blogPostRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Блог-пост не найден"));
//
//        // Удаляем изображение из Imgur, если оно есть
//        if (post.getDeleteHash() != null) {
//            imgurService.deleteImage(post.getDeleteHash());
//        }
//
//        // Удаляем пост
//        blogPostRepository.delete(post);
//    }
//
//    @Transactional(readOnly = true)
//    public List<BlogPostListDto> getLatestBlogPosts(int limit) {
//        List<BlogPost> latestPosts = blogPostRepository.findAll(
//                PageRequest.of(0, limit, Sort.by("publicationDate").descending())
//        ).getContent();
//        return blogPostMapper.toListDto(latestPosts);
//    }
//
//    @Transactional(readOnly = true)
//    public List<BlogPostListDto> getBlogPostsByCategory(Long categoryId) {
//        List<BlogPost> posts = blogPostRepository.findAll(
//                        PageRequest.of(0, 100, Sort.by("publicationDate").descending())
//                ).getContent().stream()
//                .filter(post -> post.getCategory() != null &&
//                        post.getCategory().getId().equals(categoryId))
//                .toList();
//        return blogPostMapper.toListDto(posts);
//    }
//
//    public long countPublishedPosts() {
//        return blogPostRepository.count();
//    }
//}

package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.dto.blog.BlogPostCreateDto;
import com.example.artemenko_psychologist.dto.blog.BlogPostListDto;
import com.example.artemenko_psychologist.dto.blog.BlogPostUpdateDto;
import com.example.artemenko_psychologist.maper.BlogPostMapper;
import com.example.artemenko_psychologist.model.BlogCategory;
import com.example.artemenko_psychologist.model.BlogPost;
import com.example.artemenko_psychologist.model.DocumentPhoto;
import com.example.artemenko_psychologist.repository.BlogCategoryRepository;
import com.example.artemenko_psychologist.repository.BlogPostRepository;
import com.example.artemenko_psychologist.service.impl.DocumentPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogPostService {
    private final BlogPostRepository blogPostRepository;
    private final BlogCategoryRepository categoryRepository;
    private final BlogPostMapper blogPostMapper;
    private final DocumentPhotoService documentPhotoService;

    @Transactional
    public BlogPostListDto createBlogPost(BlogPostCreateDto createDto) throws IOException {
        // Находим категорию
        BlogCategory category = categoryRepository.findById(createDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        // Преобразуем DTO в сущность
        BlogPost blogPost = blogPostMapper.toEntity(createDto, category);

        // Обработка изображения, если оно есть
        if (createDto.getImageFile() != null && !createDto.getImageFile().isEmpty()) {
            // Загрузка изображения через DocumentPhotoService
            DocumentPhoto photo = documentPhotoService.uploadPhoto(createDto.getImageFile());

            // Установка URL и id фото
            blogPost.setPreviewImageUrl(photo.getPhotoUrl()); // Предполагается, что в DocumentPhoto есть метод getUrl()
            blogPost.setPhotoId(photo.getId()); // Предполагается, что в BlogPost добавлено поле photoId
        }

        // Сохраняем
        BlogPost savedPost = blogPostRepository.save(blogPost);

        // Возвращаем DTO
        return blogPostMapper.toListDto(savedPost);
    }

    @Transactional(readOnly = true)
    public List<BlogPostListDto> getAllBlogPosts() {
        List<BlogPost> posts = blogPostRepository.findAll(
                PageRequest.of(0, 100, Sort.by("publicationDate").descending())
        ).getContent();
        return blogPostMapper.toListDto(posts);
    }

    @Transactional(readOnly = true)
    public BlogPostListDto getBlogPostById(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Блог-пост не найден"));
        return blogPostMapper.toListDto(post);
    }

    @Transactional
    public BlogPostListDto updateBlogPost(Long id, BlogPostUpdateDto updateDto) throws IOException {
        // Находим существующий пост
        BlogPost existingPost = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Блог-пост не найден"));

        // Находим категорию
        BlogCategory category = categoryRepository.findById(updateDto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Категория не найдена"));

        // Обновляем базовые поля сущности
        BlogPost updatedPost = blogPostMapper.toEntity(updateDto, category, existingPost);

        // Обработка изображения, если новое изображение загружено
        if (updateDto.getImageFile() != null && !updateDto.getImageFile().isEmpty()) {
            // Удаление старого изображения, если оно существует
            if (existingPost.getPhotoId() != null) {
                documentPhotoService.deletePhoto(existingPost.getPhotoId());
            }

            // Загрузка нового изображения
            DocumentPhoto photo = documentPhotoService.uploadPhoto(updateDto.getImageFile());

            // Обновление URL и id фото
            updatedPost.setPreviewImageUrl(photo.getPhotoUrl());
            updatedPost.setPhotoId(photo.getId());
        }

        // Сохраняем
        BlogPost savedPost = blogPostRepository.save(updatedPost);

        // Возвращаем DTO
        return blogPostMapper.toListDto(savedPost);
    }

    @Transactional
    public void deleteBlogPost(Long id) {
        BlogPost post = blogPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Блог-пост не найден"));

        // Удаляем изображение, если оно есть
        if (post.getPhotoId() != null) {
            documentPhotoService.deletePhoto(post.getPhotoId());
        }

        // Удаляем пост
        blogPostRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<BlogPostListDto> getLatestBlogPosts(int limit) {
        List<BlogPost> latestPosts = blogPostRepository.findAll(
                PageRequest.of(0, limit, Sort.by("publicationDate").descending())
        ).getContent();
        return blogPostMapper.toListDto(latestPosts);
    }

    @Transactional(readOnly = true)
    public List<BlogPostListDto> getBlogPostsByCategory(Long categoryId) {
        List<BlogPost> posts = blogPostRepository.findAll(
                        PageRequest.of(0, 100, Sort.by("publicationDate").descending())
                ).getContent().stream()
                .filter(post -> post.getCategory() != null &&
                        post.getCategory().getId().equals(categoryId))
                .toList();
        return blogPostMapper.toListDto(posts);
    }

    public long countPublishedPosts() {
        return blogPostRepository.count();
    }
}