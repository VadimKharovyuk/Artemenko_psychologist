package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.dto.reviews.ReviewCreateDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewDetailsDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewListDTO;
import com.example.artemenko_psychologist.maper.ReviewMapper;
import com.example.artemenko_psychologist.model.Review;
import com.example.artemenko_psychologist.model.Service;
import com.example.artemenko_psychologist.repository.ReviewRepository;
import com.example.artemenko_psychologist.repository.ServiceRepository;
import com.example.artemenko_psychologist.service.impl.ReviewPhotoService;
import com.example.artemenko_psychologist.util.CloudinaryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
@Slf4j
@RequiredArgsConstructor
public class ReviewPhotoServiceImpl implements ReviewPhotoService {

    private final ReviewRepository reviewRepository;
    private final ServiceRepository serviceRepository;
    private final ReviewMapper reviewMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public Review uploadPhoto(MultipartFile file) throws IOException {
        // Проверяем файл
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        // Используем CloudinaryService для загрузки фото
        CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadImage(file);

        // Создаем временный объект Review для передачи данных о фото
        Review reviewWithPhoto = new Review();
        reviewWithPhoto.setPreviewImageUrl(uploadResult.getUrl());
        reviewWithPhoto.setPublicId(uploadResult.getPublicId());
        reviewWithPhoto.setCreatedAt(LocalDateTime.now());

        log.info("Фото для отзыва успешно загружено. URL: {}, PublicId: {}",
                uploadResult.getUrl(), uploadResult.getPublicId());

        return reviewWithPhoto;
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDetailsDTO getReviewById(Long id) {
        return reviewRepository.findById(id)
                .map(reviewMapper::toDetailsDto)
                .orElseThrow(() -> new RuntimeException("Отзыв с ID " + id + " не найден"));
    }

    @Override
    @Transactional
    public boolean deletePhoto(Long id) {
        log.info("Удаление фото для отзыва с ID: {}", id);

        try {
            // Находим отзыв
            Optional<Review> reviewOptional = reviewRepository.findById(id);
            if (reviewOptional.isEmpty()) {
                log.warn("Отзыв с ID {} не найден", id);
                return false;
            }

            Review review = reviewOptional.get();

            // Важно! Сохраняем publicId в локальную переменную
            String publicId = review.getPublicId();
            log.info("Пытаемся удалить изображение с publicId: {}", publicId);

            // Проверяем, есть ли у отзыва фото
            if (publicId == null || publicId.isEmpty()) {
                log.warn("У отзыва с ID {} нет фото", id);
                return false;
            }

            // Удаляем фото из Cloudinary, используя правильный publicId из отзыва
            boolean deleted = cloudinaryService.deleteImage(review.getPublicId());
            log.info("Пытаемся удалить изображение с publicId: {}", review.getPublicId());

            if (deleted) {
                // Очищаем ссылки на фото в отзыве
                review.setPreviewImageUrl(null);
                review.setPublicId(null);
                reviewRepository.save(review);
                log.info("Пытаемся удалить изображение с publicId: {}", review.getPublicId());
                log.info("Фото для отзыва с ID {} успешно удалено", id);
                return true;
            } else {
                log.warn("Не удалось удалить фото из Cloudinary для отзыва с ID {}", id);
                return false;
            }
        } catch (Exception e) {
            log.error("Ошибка при удалении фото для отзыва с ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewListDTO> getAllPhotos() {
        List<Review> reviewsWithPhotos = reviewRepository.findAllByPreviewImageUrlIsNotNull();
        return reviewMapper.toListDto(reviewsWithPhotos);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewListDTO> getLatestReviews(int limit) {
        List<Review> latestReviews = reviewRepository.findAll(
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();

        return reviewMapper.toListDto(latestReviews);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewListDTO> getReviewsByServiceId(Long serviceId) {
        log.info("Получение отзывов для услуги с ID: {}", serviceId);

        // Проверяем существование услуги
        Service service = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new RuntimeException("Услуга с ID " + serviceId + " не найдена"));

        List<Review> reviews = reviewRepository.findAllByServiceIdOrderByCreatedAtDesc(serviceId);
        return reviewMapper.toListDto(reviews);
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewsByServiceId() {
        log.info("Подсчет количества отзывов по услугам");

        return reviewRepository.count();
    }


    @Override
    @Transactional
    public ReviewDetailsDTO createReview(@Valid ReviewCreateDTO reviewDTO, MultipartFile image) {
        log.info("Создание нового отзыва с ID сервиса: {}", reviewDTO.getServiceId());

        // Находим сервис по ID из DTO
        Service service = serviceRepository.findById(reviewDTO.getServiceId())
                .orElseThrow(() -> new EntityNotFoundException("Услуга с ID " + reviewDTO.getServiceId() + " не найдена"));

        // Если есть изображение, загружаем его
        if (image != null && !image.isEmpty()) {
            try {
                log.info("Загрузка изображения для отзыва, размер файла: {} байт", image.getSize());

                // Используем CloudinaryService для загрузки фото
                CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadImage(image);

                // Устанавливаем URL и publicId из результата загрузки
                reviewDTO.setPreviewImageUrl(uploadResult.getUrl()); // Используем URL для отображения
                reviewDTO.setPublicId(uploadResult.getPublicId()); // Используем publicId для идентификации

                log.info("Изображение успешно загружено. URL: {}, PublicId: {}",
                        uploadResult.getUrl(), uploadResult.getPublicId());
            } catch (IOException e) {
                // Обрабатываем исключение - логируем и выбрасываем RuntimeException
                log.error("Ошибка при загрузке изображения: {}", e.getMessage(), e);
                throw new RuntimeException("Не удалось загрузить изображение", e);
            }
        } else {
            log.info("Отзыв создается без изображения");
        }

        // Преобразуем DTO в Entity
        Review review = reviewMapper.toEntity(reviewDTO, service);

        // Устанавливаем дату создания
        review.setCreatedAt(LocalDateTime.now());

        // Сохраняем отзыв
        Review savedReview = reviewRepository.save(review);
        log.info("Отзыв успешно сохранен с ID: {}", savedReview.getId());

        // Возвращаем DTO с деталями
        return reviewMapper.toDetailsDto(savedReview);
    }

    @Transactional
    @Override
    public void deleteReview(Long id) {
        log.info("Запрос на удаление отзыва с ID: {}", id);

        try {
            // Находим отзыв перед удалением
            Optional<Review> reviewOptional = reviewRepository.findById(id);
            if (reviewOptional.isEmpty()) {
                log.warn("Отзыв с ID {} не найден", id);
                throw new EntityNotFoundException("Отзыв с ID " + id + " не найден");
            }

            Review review = reviewOptional.get();

            // Если у отзыва есть фото, удаляем его из Cloudinary
            if (review.getPublicId() != null && !review.getPublicId().isEmpty()) {
                String publicId = review.getPublicId();
                log.info("Удаление фото перед удалением отзыва. PublicId: {}", publicId);

                // Проверяем, является ли publicId числом (старый формат)
                if (publicId.matches("\\d+")) {
                    log.warn("Обнаружен числовой PublicId: {}. Это устаревший формат ID. Пропускаем удаление фото.", publicId);
                } else {
                    // Пытаемся удалить фото из Cloudinary (новый формат)
                    boolean photoDeleted = cloudinaryService.deleteImage(publicId);
                    if (photoDeleted) {
                        log.info("Фото отзыва успешно удалено из Cloudinary");
                    } else {
                        log.warn("Не удалось удалить фото отзыва из Cloudinary. PublicId: {}", publicId);
                    }
                }
            }

            // Удаляем отзыв из базы данных
            reviewRepository.deleteById(id);
            log.info("Отзыв успешно удален. ID: {}", id);
        } catch (EntityNotFoundException e) {
            log.error("Ошибка: {}", e.getMessage());
            throw e; // Пробрасываем исключение дальше
        } catch (Exception e) {
            log.error("Ошибка при удалении отзыва с ID {}: {}", id, e.getMessage(), e);
            throw new RuntimeException("Не удалось удалить отзыв: " + e.getMessage(), e);
        }
    }
}