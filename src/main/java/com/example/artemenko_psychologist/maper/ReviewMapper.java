package com.example.artemenko_psychologist.maper;

import com.example.artemenko_psychologist.dto.reviews.ReviewCreateDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewDetailsDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewListDTO;
import com.example.artemenko_psychologist.model.Review;
import com.example.artemenko_psychologist.model.Service;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ReviewMapper {

    // Конвертация Entity в ListDTO
    public ReviewListDTO toListDto(Review review) {
        return ReviewListDTO.builder()
                .id(review.getId())
                .previewImageUrl(review.getPreviewImageUrl())
                .description(review.getDescription())
                .createdAt(review.getCreatedAt())
                .username(review.getUsername())
                .serviceId(review.getService() != null ? review.getService().getId() : null)
                .build();
    }

    // Конвертация списка Entity в список ListDTO
    public List<ReviewListDTO> toListDto(List<Review> reviews) {
        return reviews.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }

    // Конвертация Entity в DetailsDTO
    public ReviewDetailsDTO toDetailsDto(Review review) {
        return ReviewDetailsDTO.builder()
                .id(review.getId())
                .previewImageUrl(review.getPreviewImageUrl())
                .description(review.getDescription())
                .createdAt(review.getCreatedAt())
                .username(review.getUsername())
                .serviceId(review.getService() != null ? review.getService().getId() : null)
                .serviceName(review.getService() != null ? review.getService().getTitle() : null)
                .build();
    }

    // Конвертация CreateDTO в Entity
    public Review toEntity(ReviewCreateDTO dto, Service service) {
        return Review.builder()
                .previewImageUrl(dto.getPreviewImageUrl())
                .publicId(dto.getPublicId())
                .description(dto.getDescription())
                .createdAt(LocalDateTime.now()) // Устанавливаем текущее время
                .username(dto.getUsername())
                .service(service)
                .build();
    }

    // Обновление существующего Entity из CreateDTO
    public void updateEntity(Review review, ReviewCreateDTO dto, Service service) {
        review.setPreviewImageUrl(dto.getPreviewImageUrl());
        review.setPublicId(dto.getPublicId());
        review.setDescription(dto.getDescription());
        review.setUsername(dto.getUsername());
        review.setService(service);
    }
}