package com.example.artemenko_psychologist.util;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private Cloudinary cloudinary;

    @PostConstruct
    public void init() {
        try {
            String cloudinaryUrl = "cloudinary://" + apiKey + ":" + apiSecret + "@" + cloudName;
            cloudinary = new Cloudinary(cloudinaryUrl);
            log.info("Cloudinary инициализирован через URL-строку");
        } catch (Exception e) {
            log.error("Ошибка инициализации Cloudinary: {}", e.getMessage());
        }
    }

    /**
     * Результат загрузки изображения
     */
    @Getter
    public static class UploadResult {
        private String url;
        private String publicId;

        public UploadResult(String url, String publicId) {
            this.url = url;
            this.publicId = publicId;
        }
    }

    /**
     * Загружает изображение в Cloudinary и возвращает URL и publicId
     * с оптимизированными параметрами загрузки и обработкой ошибок
     */
    public UploadResult uploadImage(MultipartFile file) throws IOException {
        // Проверки входных данных остаются без изменений

        try {
            // Формируем уникальный идентификатор (без изменений)
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String originalFilename = file.getOriginalFilename();
            String fileBaseName = "image";

            if (originalFilename != null && !originalFilename.isEmpty()) {
                fileBaseName = originalFilename.replaceAll("[^a-zA-Z0-9.-]", "_");
                int dotIndex = fileBaseName.lastIndexOf('.');
                if (dotIndex > 0) {
                    fileBaseName = fileBaseName.substring(0, dotIndex);
                }
            }

            // Изменяем параметры загрузки - убираем проблемную трансформацию
            Map<String, Object> params = ObjectUtils.asMap(
                    "resource_type", "auto",
                    "public_id", timestamp + "_" + fileBaseName,
                    "overwrite", true,
                    "tags", "user_upload"

            );

            log.info("Начинаем загрузку изображения в Cloudinary: {}", fileBaseName);

            // Загружаем изображение
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            // Остальная часть метода без изменений
            String url = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            log.info("Изображение успешно загружено в Cloudinary. Public ID: {}", publicId);

            return new UploadResult(url, publicId);
        } catch (IOException e) {
            log.error("Ошибка ввода-вывода при загрузке изображения: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при загрузке изображения: {}", e.getMessage());
            throw new IOException("Ошибка при загрузке изображения в Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Удаляет изображение из Cloudinary по его publicId
     * с улучшенной обработкой ошибок и логированием
     */
    public boolean deleteImage(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            log.warn("Попытка удаления изображения с пустым publicId");
            return false;
        }

        try {
            log.info("Удаляем изображение из Cloudinary. Public ID: {}", publicId);

            Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            boolean success = "ok".equals(result.get("result"));

            if (success) {
                log.info("Изображение успешно удалено из Cloudinary. Public ID: {}", publicId);
            } else {
                log.warn("Не удалось удалить изображение из Cloudinary. Public ID: {}, Результат: {}",
                        publicId, result);
            }

            return success;
        } catch (Exception e) {
            log.error("Ошибка при удалении изображения из Cloudinary. Public ID: {}, Ошибка: {}",
                    publicId, e.getMessage());
            return false;
        }
    }

    /**
     * Получает информацию об изображении по его publicId
     */
    public Map<String, Object> getImageInfo(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            throw new IllegalArgumentException("Public ID не может быть пустым");
        }

        try {
            log.info("Получаем информацию об изображении. Public ID: {}", publicId);
            return cloudinary.api().resource(publicId, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.error("Ошибка при получении информации об изображении. Public ID: {}, Ошибка: {}",
                    publicId, e.getMessage());
            return Collections.emptyMap();
        }
    }
}