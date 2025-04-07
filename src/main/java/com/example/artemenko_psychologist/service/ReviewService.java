package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.dto.reviews.ReviewCreateDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewDetailsDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewListDTO;
import com.example.artemenko_psychologist.maper.ReviewMapper;
import com.example.artemenko_psychologist.model.Review;
import com.example.artemenko_psychologist.model.Service;
import com.example.artemenko_psychologist.repository.ReviewRepository;
import com.example.artemenko_psychologist.repository.ServiceRepository;
import com.example.artemenko_psychologist.util.ImgurService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ServiceRepository serviceRepository;
    private final ReviewMapper reviewMapper;
    private final ImgurService imgurService;

    /**
     * Получение всех отзывов
     */
    public List<ReviewListDTO> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        return reviewMapper.toListDto(reviews);
    }

    /**
     * Получение последних отзывов с указанным лимитом (через stream API)
     * @param limit количество отзывов для получения
     */
    public List<ReviewListDTO> getLatestReviews(int limit) {
        return reviewRepository.findAll(Sort.by("createdAt").descending())
                .stream()
                .limit(limit)
                .map(reviewMapper::toListDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение последних отзывов для конкретной услуги с указанным лимитом (через stream API)
     * @param serviceId ID услуги
     * @param limit количество отзывов для получения
     */
    public List<ReviewListDTO> getLatestReviewsByServiceId(Long serviceId, int limit) {
        return reviewRepository.findByServiceId(serviceId, Sort.by("createdAt").descending())
                .stream()
                .limit(limit)
                .map(reviewMapper::toListDto)
                .collect(Collectors.toList());
    }

    /**
     * Получение отзывов для конкретной услуги
     */
    public List<ReviewListDTO> getReviewsByServiceId(Long serviceId) {
        List<Review> reviews = reviewRepository.findByServiceId(serviceId);
        return reviewMapper.toListDto(reviews);
    }

    /**
     * Получение детальной информации об отзыве
     */
    public ReviewDetailsDTO getReviewById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Отзыв с ID " + id + " не найден"));
        return reviewMapper.toDetailsDto(review);
    }

    /**
     * Создание отзыва с загрузкой изображения
     */
    @Transactional
    public ReviewDetailsDTO createReview(ReviewCreateDTO dto, MultipartFile image) throws IOException {
        Service service = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Услуга с ID " + dto.getServiceId() + " не найдена"));

        // Если есть изображение, загружаем его в Imgur
        if (image != null && !image.isEmpty()) {
            ImgurService.UploadResult uploadResult = imgurService.uploadImage(image);
            dto.setPreviewImageUrl(uploadResult.getUrl());
            dto.setDeleteHash(uploadResult.getDeleteHash());
        }

        Review review = reviewMapper.toEntity(dto, service);
        review.setCreatedAt(LocalDateTime.now());
        Review savedReview = reviewRepository.save(review);

        return reviewMapper.toDetailsDto(savedReview);
    }

    /**
     * Обновление отзыва с возможностью загрузки нового изображения
     */
    @Transactional
    public ReviewDetailsDTO updateReview(Long id, ReviewCreateDTO dto, MultipartFile image) throws IOException {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Отзыв с ID " + id + " не найден"));

        Service service = serviceRepository.findById(dto.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Услуга с ID " + dto.getServiceId() + " не найдена"));

        // Если есть новое изображение, загружаем его и удаляем старое
        if (image != null && !image.isEmpty()) {
            // Удаляем старое изображение, если оно существует
            if (review.getDeleteHash() != null && !review.getDeleteHash().isEmpty()) {
                imgurService.deleteImage(review.getDeleteHash());
            }

            // Загружаем новое изображение
            ImgurService.UploadResult uploadResult = imgurService.uploadImage(image);
            dto.setPreviewImageUrl(uploadResult.getUrl());
            dto.setDeleteHash(uploadResult.getDeleteHash());
        } else {
            // Сохраняем существующие данные изображения
            dto.setPreviewImageUrl(review.getPreviewImageUrl());
            dto.setDeleteHash(review.getDeleteHash());
        }

        reviewMapper.updateEntity(review, dto, service);
        Review updatedReview = reviewRepository.save(review);

        return reviewMapper.toDetailsDto(updatedReview);
    }

    /**
     * Удаление отзыва
     */
    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Отзыв с ID " + id + " не найден"));

        // Удаляем изображение из Imgur
        if (review.getDeleteHash() != null && !review.getDeleteHash().isEmpty()) {
            imgurService.deleteImage(review.getDeleteHash());
        }

        reviewRepository.delete(review);
    }
}