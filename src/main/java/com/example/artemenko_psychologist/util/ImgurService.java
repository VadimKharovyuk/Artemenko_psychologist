package com.example.artemenko_psychologist.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ImgurService {

    private static final String IMGUR_API_URL = "https://api.imgur.com/3/image";
    private static final Logger logger = LoggerFactory.getLogger(ImgurService.class);

    @Value("${imgur.client-id}")
    private String clientId;

    @Value("${imgur.access-token}")
    private String accessToken;

    private final RestTemplate restTemplate;

    // Простой кэш для URL изображений
    private final Map<String, UploadResult> imageCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        logger.info("ImgurService инициализирован. Используется аутентификация с access token.");
    }



    /**
     * Результат загрузки изображения
     */
    @Getter
    public static class UploadResult {
        private String url;
        private String deleteHash;

        public UploadResult(String url, String deleteHash) {
            this.url = url;
            this.deleteHash = deleteHash;
        }
    }

    /**
     * Загружает изображение в Imgur и возвращает URL и deleteHash
     * с использованием access token для увеличения лимита запросов
     */
    public UploadResult uploadImage(MultipartFile file) throws IOException {
        // Проверки файла
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Файл должен быть изображением");
        }

        // Создаем ключ для кэша на основе хэша содержимого файла
        String cacheKey = DigestUtils.md5DigestAsHex(file.getBytes());

        // Проверяем кэш
        if (imageCache.containsKey(cacheKey)) {
            logger.info("Изображение найдено в кэше, возвращаем кэшированный URL");
            return imageCache.get(cacheKey);
        }

        // Кодируем файл в Base64
        byte[] fileBytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(fileBytes);

        // Создаем заголовки с access token для большего лимита запросов
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Используем Bearer token для увеличения лимита запросов
        headers.set("Authorization", "Bearer " + accessToken);

        // Также добавляем Client-ID для полноты
        headers.set("Client-ID", clientId);

        // Добавляем User-Agent для отслеживания
        headers.set("User-Agent", "Artemenko-Psychologist-App/1.0");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", base64Image);

        // Добавляем дополнительные параметры для лучшей организации
        body.add("title", "Artemenko Psychologist Document");
        body.add("description", "Uploaded document for Artemenko Psychologist service");

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Реализуем механизм повторных попыток с экспоненциальной задержкой
        int maxRetries = 3;
        int retryCount = 0;
        long retryDelay = 1000; // Начальная задержка 1 секунда

        while (true) {
            try {
                logger.info("Отправка запроса на загрузку изображения в Imgur (попытка {})", retryCount + 1);

                ResponseEntity<Map> response = restTemplate.postForEntity(
                        IMGUR_API_URL,
                        requestEntity,
                        Map.class
                );

                // Логируем информацию о лимитах из заголовков
                logRateLimitInfo(response.getHeaders());

                // Обрабатываем успешный ответ
                if (response.getBody() != null && response.getBody().containsKey("data")) {
                    Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                    if (data.containsKey("link") && data.containsKey("deletehash")) {
                        String link = (String) data.get("link");
                        String deleteHash = (String) data.get("deletehash");

                        UploadResult result = new UploadResult(link, deleteHash);

                        // Сохраняем в кэш
                        imageCache.put(cacheKey, result);

                        logger.info("Изображение успешно загружено на Imgur: {}", link);

                        return result;
                    }
                }

                throw new IOException("Неверный формат ответа от Imgur API");

            } catch (HttpClientErrorException e) {
                if (e.getStatusCode().value() == 429 && retryCount < maxRetries) {
                    retryCount++;

                    // Логируем информацию об ошибке
                    logger.warn("Получена ошибка 429 Too Many Requests от Imgur API. " +
                                    "Повторная попытка {} из {} через {} мс",
                            retryCount, maxRetries, retryDelay);

                    try {
                        Thread.sleep(retryDelay);
                        // Увеличиваем задержку для следующей попытки (экспоненциальная задержка)
                        retryDelay *= 2;
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IOException("Прерывание во время ожидания перед повторной попыткой", ie);
                    }
                } else {
                    logger.error("Ошибка при загрузке изображения в Imgur: {}", e.getMessage());
                    throw new IOException("Ошибка при загрузке изображения в Imgur: " + e.getMessage(), e);
                }
            }
        }
    }

    /**
     * Удаляет изображение из Imgur по его deleteHash
     */
    public boolean deleteImage(String deleteHash) {
        if (deleteHash == null || deleteHash.isEmpty()) {
            logger.warn("Попытка удаления изображения с пустым deleteHash");
            return false;
        }

        try {
            logger.info("Удаляем изображение из Imgur. DeleteHash: {}", deleteHash);

            String deleteUrl = "https://api.imgur.com/3/image/" + deleteHash;

            HttpHeaders headers = new HttpHeaders();
            // Используем Bearer token для авторизации
            headers.set("Authorization", "Bearer " + accessToken);
            // Также добавляем Client-ID для полноты
            headers.set("Client-ID", clientId);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    deleteUrl,
                    org.springframework.http.HttpMethod.DELETE,
                    requestEntity,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("success")) {
                boolean success = (boolean) response.getBody().get("success");

                if (success) {
                    logger.info("Изображение успешно удалено из Imgur. DeleteHash: {}", deleteHash);

                    // Удаляем из кэша все записи с этим deleteHash
                    imageCache.entrySet().removeIf(entry -> entry.getValue().getDeleteHash().equals(deleteHash));
                } else {
                    logger.warn("Не удалось удалить изображение из Imgur. DeleteHash: {}", deleteHash);
                }

                return success;
            }

            return false;
        } catch (Exception e) {
            logger.error("Ошибка при удалении изображения из Imgur. DeleteHash: {}, Ошибка: {}",
                    deleteHash, e.getMessage());
            return false;
        }
    }

    /**
     * Получает информацию о лимитах из заголовков ответа Imgur
     */
    private void logRateLimitInfo(HttpHeaders headers) {
        try {
            List<String> clientRemaining = headers.get("X-RateLimit-ClientRemaining");
            List<String> clientLimit = headers.get("X-RateLimit-ClientLimit");
            List<String> userRemaining = headers.get("X-RateLimit-UserRemaining");
            List<String> userLimit = headers.get("X-RateLimit-UserLimit");

            if (clientRemaining != null && clientLimit != null) {
                logger.info("Imgur API лимиты клиента: {} из {} запросов осталось",
                        clientRemaining.get(0), clientLimit.get(0));
            }

            if (userRemaining != null && userLimit != null) {
                logger.info("Imgur API лимиты пользователя: {} из {} запросов осталось",
                        userRemaining.get(0), userLimit.get(0));
            }
        } catch (Exception e) {
            logger.warn("Не удалось получить информацию о лимитах запросов из заголовков", e);
        }
    }



    /**
     * Получает информацию о текущих лимитах запросов Imgur API
     * @return Карта с информацией о лимитах запросов
     */
    public Map<String, Object> getRateLimitInfo() {
        logger.info("Запрос информации о лимитах Imgur API");

        // Инициализируем карту со всеми необходимыми ключами, чтобы избежать ошибок в шаблоне
        Map<String, Object> rateLimits = new HashMap<>();

        // Предустановка значений по умолчанию для всех ключей
        rateLimits.put("error", false);
        rateLimits.put("errorMessage", "");
        rateLimits.put("status", "UNKNOWN");
        rateLimits.put("clientLimit", "0");
        rateLimits.put("clientRemaining", "0");
        rateLimits.put("userLimit", "0");
        rateLimits.put("userRemaining", "0");
        rateLimits.put("resetClientRemaining", "N/A");
        rateLimits.put("resetUserRemaining", "N/A");
        rateLimits.put("clientUsedPercent", 0);
        rateLimits.put("userUsedPercent", 0);

        try {
            // Создаем заголовки с access token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.set("Client-ID", clientId);

            HttpEntity<String> requestEntity = new HttpEntity<>(headers);

            // Выполняем простой GET-запрос к API для получения информации о кредитах
            String creditUrl = "https://api.imgur.com/3/credits";
            ResponseEntity<Map> response = restTemplate.exchange(
                    creditUrl,
                    org.springframework.http.HttpMethod.GET,
                    requestEntity,
                    Map.class
            );

            // Логируем информацию о лимитах из заголовков
            List<String> clientRemaining = response.getHeaders().get("X-RateLimit-ClientRemaining");
            List<String> clientLimit = response.getHeaders().get("X-RateLimit-ClientLimit");
            List<String> userRemaining = response.getHeaders().get("X-RateLimit-UserRemaining");
            List<String> userLimit = response.getHeaders().get("X-RateLimit-UserLimit");

            // Сохраняем данные из заголовков
            if (clientRemaining != null && !clientRemaining.isEmpty()) {
                rateLimits.put("clientRemaining", clientRemaining.get(0));
            }
            if (clientLimit != null && !clientLimit.isEmpty()) {
                rateLimits.put("clientLimit", clientLimit.get(0));
            }
            if (userRemaining != null && !userRemaining.isEmpty()) {
                rateLimits.put("userRemaining", userRemaining.get(0));
            }
            if (userLimit != null && !userLimit.isEmpty()) {
                rateLimits.put("userLimit", userLimit.get(0));
            }

            // Если в ответе есть данные о лимитах, добавляем их
            if (response.getBody() != null && response.getBody().containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");

                // Добавляем подробную информацию из тела ответа
                if (data.containsKey("ClientLimit")) {
                    rateLimits.put("clientLimit", String.valueOf(data.get("ClientLimit")));
                }
                if (data.containsKey("ClientRemaining")) {
                    rateLimits.put("clientRemaining", String.valueOf(data.get("ClientRemaining")));
                }
                if (data.containsKey("UserLimit")) {
                    rateLimits.put("userLimit", String.valueOf(data.get("UserLimit")));
                }
                if (data.containsKey("UserRemaining")) {
                    rateLimits.put("userRemaining", String.valueOf(data.get("UserRemaining")));
                }

                // Добавляем дополнительную информацию о времени сброса лимитов
                if (data.containsKey("UserReset")) {
                    rateLimits.put("resetClientRemaining", formatResetTime(parseInteger(data.get("UserReset"))));
                    rateLimits.put("resetUserRemaining", formatResetTime(parseInteger(data.get("UserReset"))));
                }

                // Процент использования
                int clientUsedPercent = calculateUsedPercent(
                        parseInteger(data.get("ClientLimit")),
                        parseInteger(data.get("ClientRemaining"))
                );
                int userUsedPercent = calculateUsedPercent(
                        parseInteger(data.get("UserLimit")),
                        parseInteger(data.get("UserRemaining"))
                );

                rateLimits.put("clientUsedPercent", clientUsedPercent);
                rateLimits.put("userUsedPercent", userUsedPercent);

                // Определяем статус на основе оставшихся запросов
                String status = determineStatus(clientUsedPercent, userUsedPercent);
                rateLimits.put("status", status);

                logger.info("Imgur API лимиты: клиент {}/{}, пользователь {}/{}, статус: {}",
                        data.get("ClientRemaining"), data.get("ClientLimit"),
                        data.get("UserRemaining"), data.get("UserLimit"),
                        status);
            }

        } catch (Exception e) {
            logger.error("Ошибка при получении информации о лимитах Imgur API: {}", e.getMessage());

            // Добавляем информацию об ошибке
            rateLimits.put("error", true);
            rateLimits.put("errorMessage", "Не удалось получить информацию о лимитах: " + e.getMessage());
            rateLimits.put("status", "ERROR");
        }

        return rateLimits;
    }

    /**
     * Безопасно преобразует объект в Integer
     */
    private Integer parseInteger(Object value) {
        if (value == null) return 0;

        try {
            if (value instanceof Integer) {
                return (Integer) value;
            } else if (value instanceof String) {
                return Integer.parseInt((String) value);
            } else if (value instanceof Long) {
                return ((Long) value).intValue();
            } else if (value instanceof Double) {
                return ((Double) value).intValue();
            }
        } catch (Exception e) {
            logger.warn("Не удалось преобразовать значение в Integer: {}", value);
        }

        return 0;
    }

    /**
     * Форматирует время сброса в читаемый формат
     */
    private String formatResetTime(Integer resetTimestamp) {
        if (resetTimestamp == null || resetTimestamp == 0) return "N/A";

        try {
            // Конвертируем Unix timestamp в LocalDateTime
            Instant instant = Instant.ofEpochSecond(resetTimestamp);
            LocalDateTime resetTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());

            // Форматируем время
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
            return resetTime.format(formatter);
        } catch (Exception e) {
            logger.warn("Не удалось отформатировать время сброса: {}", e.getMessage());
            return "N/A";
        }
    }

    /**
     * Вычисляет процент использованных запросов
     */
    private int calculateUsedPercent(Integer limit, Integer remaining) {
        if (limit == null || remaining == null || limit == 0) return 0;

        int used = limit - remaining;
        return (int) Math.round((double) used / limit * 100);
    }

    /**
     * Определяет статус API на основе процента использования
     */
    private String determineStatus(int clientUsedPercent, int userUsedPercent) {
        int maxUsed = Math.max(clientUsedPercent, userUsedPercent);

        if (maxUsed >= 90) return "CRITICAL";
        if (maxUsed >= 75) return "WARNING";
        if (maxUsed >= 50) return "MODERATE";
        return "GOOD";
    }
}
