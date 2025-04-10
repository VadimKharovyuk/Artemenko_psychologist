package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.dto.reviews.ReviewCreateDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewDetailsDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewListDTO;
import com.example.artemenko_psychologist.maper.ReviewMapper;
import com.example.artemenko_psychologist.model.DocumentPhoto;
import com.example.artemenko_psychologist.model.Review;
import com.example.artemenko_psychologist.model.Service;
import com.example.artemenko_psychologist.repository.ReviewRepository;
import com.example.artemenko_psychologist.repository.ServiceRepository;
import com.example.artemenko_psychologist.service.impl.DocumentPhotoService;
import com.example.artemenko_psychologist.util.CloudinaryService;
import com.example.artemenko_psychologist.util.ImgurService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ServiceRepository serviceRepository;
    private final ReviewMapper reviewMapper;
    private final DocumentPhotoService documentPhotoService;


    /**
     * Получение всех отзывов
     */
    public List<ReviewListDTO> getAllReviews() {
        List<Review> reviews = reviewRepository.findAll();
        return reviewMapper.toListDto(reviews);
    }

    /**
     * Получение последних отзывов с указанным лимитом (через stream API)
     *
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
     *
     * @param serviceId ID услуги
     * @param limit     количество отзывов для получения
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

        if (image != null && !image.isEmpty()) {
            DocumentPhoto documentPhoto = documentPhotoService.uploadPhoto(image);
            dto.setPreviewImageUrl(documentPhoto.getPhotoUrl()); // Assuming DocumentPhoto has getUrl method
            dto.setPublicId(documentPhoto.getId().toString()); // Store document photo ID as publicId
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
            if (review.getPublicId() != null && !review.getPublicId().isEmpty()) {
                // Convert publicId string to Long and delete the photo
                try {
                    Long photoId = Long.parseLong(review.getPublicId());
                    documentPhotoService.deletePhoto(photoId);
                } catch (NumberFormatException e) {
                    // Handle the case where publicId is not a valid Long
                    // Maybe log the error or take appropriate action
                }
            }

            // Загружаем новое изображение
            DocumentPhoto documentPhoto = documentPhotoService.uploadPhoto(image);
            dto.setPreviewImageUrl(documentPhoto.getPhotoUrl()); // Assuming DocumentPhoto has getUrl method
            dto.setPublicId(documentPhoto.getId().toString()); // Store document photo ID as publicId
        } else {
            // Сохраняем существующие данные изображения
            dto.setPreviewImageUrl(review.getPreviewImageUrl());
            dto.setPublicId(review.getPublicId());
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

        if (review.getPublicId() != null && !review.getPublicId().isEmpty()) {
            try {
                Long photoId = Long.parseLong(review.getPublicId());
                documentPhotoService.deletePhoto(photoId);
            } catch (NumberFormatException e) {
                 log.error("Неверный формат publicId: " + review.getPublicId(), e);
            }
        }

        reviewRepository.delete(review);
    }


    public long countReviewsByServiceId(Long serviceId) {
        return reviewRepository.countByServiceId(serviceId);
    }
}