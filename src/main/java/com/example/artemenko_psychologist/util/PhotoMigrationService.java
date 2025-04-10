package com.example.artemenko_psychologist.util;

import com.example.artemenko_psychologist.model.DocumentPhoto;
import com.example.artemenko_psychologist.model.Review;
import com.example.artemenko_psychologist.repository.ReviewRepository;
import com.example.artemenko_psychologist.service.impl.DocumentPhotoService;
import com.example.artemenko_psychologist.service.impl.ReviewPhotoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
@RequiredArgsConstructor
@Component
@Slf4j
public class PhotoMigrationService {

    private final ReviewRepository reviewRepository;
    private final ReviewPhotoService reviewPhotoService ;
    private final ImgurService imgurService;
    private final CloudinaryService cloudinaryService;
    private final RestTemplate restTemplate;




    /**
     * Метод для запуска миграции через CommandLineRunner или административный API
     */
    public void runMigration() {
        migrateReviewPhotos();
    }

    @Transactional
    public void migrateReviewPhotos() {
        log.info("Начинаем миграцию фотографий отзывов");

        // Получаем все отзывы с фотографиями
        List<Review> reviewsWithPhotos = reviewRepository.findAllByPreviewImageUrlIsNotNull();
        log.info("Найдено {} отзывов с фотографиями для миграции", reviewsWithPhotos.size());

        int success = 0;
        int failed = 0;

        for (Review review : reviewsWithPhotos) {

            // В начале цикла для каждого отзыва
            if (review.getPublicId() != null && review.getPublicId().matches("\\d+")) {
                log.info("Пропускаем отзыв с ID {}, так как он уже мигрирован (publicId: {})",
                        review.getId(), review.getPublicId());
                continue;
            }
            try {
                // Проверяем, что есть URL изображения
                if (review.getPreviewImageUrl() == null || review.getPreviewImageUrl().isEmpty()) {
                    log.warn("Пропускаем отзыв с ID {} - URL изображения отсутствует", review.getId());
                    continue;
                }

                log.info("Миграция фото для отзыва с ID: {}, URL: {}",
                        review.getId(), review.getPreviewImageUrl());

                // Загружаем изображение из удаленного URL
                byte[] imageData = downloadImageFromUrl(review.getPreviewImageUrl());
                if (imageData == null || imageData.length == 0) {
                    log.error("Не удалось загрузить изображение с URL: {}", review.getPreviewImageUrl());
                    failed++;
                    continue;
                }

                // Создаем MultipartFile из полученных данных
                MultipartFile multipartFile = createMultipartFile(imageData, "migrated_image.jpg");


                // Сохраняем через ReviewPhotoService
                Review reviewPhoto = reviewPhotoService.uploadPhoto(multipartFile);


                // Обновляем отзыв с новыми данными из результата загрузки
                // Обновляем отзыв с новыми данными
                review.setPreviewImageUrl(reviewPhoto.getPreviewImageUrl()); // Используем новый URL
                review.setPublicId(reviewPhoto.getPublicId()); // Используем новый publicId

                // Сохраняем обновленный отзыв
                reviewRepository.save(review);

                // Удаляем старое изображение
                deleteOldImage(review);

                success++;
                log.info("Успешно мигрировано изображение для отзыва с ID: {}", review.getId());

                // Добавляем задержку между обработкой фото (5 секунд)
                try {
                    log.info("Делаем паузу в 5 секунд перед обработкой следующего изображения");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.warn("Прерывание во время задержки: {}", e.getMessage());
                }

            } catch (Exception e) {
                log.error("Ошибка при миграции изображения для отзыва с ID {}: {}",
                        review.getId(), e.getMessage(), e);
                failed++;

                // Даже при ошибке делаем паузу перед обработкой следующего изображения
                try {
                    log.info("Делаем паузу в 5 секунд после ошибки");
                    Thread.sleep(5000);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Прерывание во время задержки: {}", ie.getMessage());
                }
            }
        }

        log.info("Миграция завершена. Успешно: {}, С ошибками: {}", success, failed);
    }

    /**
     * Определяет и удаляет старое изображение в зависимости от сервиса
     */
    private void deleteOldImage(Review review) {
        try {
            // Проверяем, использовался ли Imgur (по наличию deleteHash)
            if (review.getDeleteHash() != null && !review.getDeleteHash().isEmpty()) {
                log.info("Удаляем изображение из Imgur с deleteHash: {}", review.getDeleteHash());
                imgurService.deleteImage(review.getDeleteHash());
                review.setDeleteHash(null);
            }
            // Проверяем, использовался ли Cloudinary (по формату publicId)
            else if (review.getPublicId() != null && !review.getPublicId().isEmpty()
                    && !review.getPublicId().matches("\\d+")) {
                log.info("Удаляем изображение из Cloudinary с publicId: {}", review.getPublicId());
                cloudinaryService.deleteImage(review.getPublicId());
            }

            // Сохраняем отзыв с обновленными полями
            reviewRepository.save(review);
        } catch (Exception e) {
            log.warn("Ошибка при удалении старого изображения: {}", e.getMessage());
        }
    }

    /**
     * Загружает изображение по URL
     */
    private byte[] downloadImageFromUrl(String imageUrl) {
        try {
            log.info("Загрузка изображения с URL: {}", imageUrl);

            // Использовать RestTemplate для загрузки с поддержкой редиректов
            ResponseEntity<byte[]> response = restTemplate.getForEntity(imageUrl, byte[].class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                log.error("Не удалось загрузить изображение. Статус: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Ошибка при загрузке изображения по URL {}: {}", imageUrl, e.getMessage());
            return null;
        }
    }


    /**
     * Создает MultipartFile из массива байтов
     */

    private MultipartFile createMultipartFile(final byte[] fileContent, String fileName) {
        return new MultipartFile() {
            @Override
            public String getName() {
                return "file";
            }

            @Override
            public String getOriginalFilename() {
                return fileName;
            }

            @Override
            public String getContentType() {
                return "image/jpeg";
            }

            @Override
            public boolean isEmpty() {
                return fileContent == null || fileContent.length == 0;
            }

            @Override
            public long getSize() {
                return fileContent.length;
            }

            @Override
            public byte[] getBytes() throws IOException {
                return fileContent;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return new ByteArrayInputStream(fileContent);
            }

            @Override
            public void transferTo(File dest) throws IOException, IllegalStateException {
                Files.write(dest.toPath(), fileContent);
            }
        };
    }
}