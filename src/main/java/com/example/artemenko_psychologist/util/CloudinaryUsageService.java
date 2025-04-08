package com.example.artemenko_psychologist.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;

@Service
@Slf4j
public class CloudinaryUsageService {
    private final Cloudinary cloudinary;

    public CloudinaryUsageService(@Value("${cloudinary.cloud-name}") String cloudName,
                                  @Value("${cloudinary.api-key}") String apiKey,
                                  @Value("${cloudinary.api-secret}") String apiSecret) {
        String cloudinaryUrl = "cloudinary://" + apiKey + ":" + apiSecret + "@" + cloudName;
        this.cloudinary = new Cloudinary(cloudinaryUrl);
    }

    /**
     * Получает информацию об использовании аккаунта Cloudinary с расширенной отладкой
     * @return Карта с данными об использовании
     */
    public Map<String, Object> getAccountUsage() {
        try {

            // Получаем данные об использовании
            Map<String, Object> usage = cloudinary.api().usage(ObjectUtils.emptyMap());

            // Логируем полный ответ для отладки
            for (Map.Entry<String, Object> entry : usage.entrySet()) {

            }

            return usage;
        } catch (Exception e) {
            log.error("Ошибка при получении статистики использования Cloudinary", e);

            // Выводим подробную информацию об ошибке
            log.error("Детали исключения:", e);

            // Возвращаем пустую карту вместо выброса исключения
            return Collections.emptyMap();
        }
    }

    /**
     * Безопасный метод получения числового значения из карты
     * @param map Исходная карта
     * @param key Ключ для поиска
     * @return Числовое значение или 0
     */
    public Long getSafeLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        log.warn("Значение для ключа '{}' не найдено или не является числом", key);
        return 0L;
    }
}