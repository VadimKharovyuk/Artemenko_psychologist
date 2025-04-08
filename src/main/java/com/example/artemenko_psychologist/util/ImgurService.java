package com.example.artemenko_psychologist.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ImgurService {

//    private static final String IMGUR_API_URL = "https://api.imgur.com/3/image";
//
//    @Value("${imgur.client-id}")
//    private String clientId;
//
//    @Value("${imgur.access-token}")
//    private String accessToken;
//
//    private final RestTemplate restTemplate;
//
//
//
//    /**
//     * Результат загрузки изображения
//     */
//    @Getter
//    public static class UploadResult {
//        private String url;
//        private String deleteHash;
//
//        public UploadResult(String url, String deleteHash) {
//            this.url = url;
//            this.deleteHash = deleteHash;
//        }
//
//    }
//
//    /**
//     * Загружает изображение в Imgur и возвращает URL и deleteHash
//     *
//     * @param file изображение для загрузки
//     * @return объект с URL и deleteHash загруженного изображения
//     * @throws IOException если произошла ошибка при чтении файла
//     */
//    public UploadResult uploadImage(MultipartFile file) throws IOException {
//        // Проверка на null
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("Файл не может быть пустым");
//        }
//
//        // Проверка типа файла
//        String contentType = file.getContentType();
//        if (contentType == null || !contentType.startsWith("image/")) {
//            throw new IllegalArgumentException("Файл должен быть изображением");
//        }
//
//        // Конвертируем файл в Base64
//        byte[] fileBytes = file.getBytes();
//        String base64Image = Base64.getEncoder().encodeToString(fileBytes);
//
//        // Создаем заголовки запроса
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        headers.set("Authorization", "Bearer " + accessToken);
//
//        // Создаем body запроса
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        body.add("image", base64Image);
//
//        // Создаем HTTP entity
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//
//        // Отправляем запрос к Imgur API
//        ResponseEntity<Map> response = restTemplate.postForEntity(IMGUR_API_URL, requestEntity, Map.class);
//
//        // Обрабатываем ответ
//        if (response.getBody() != null && response.getBody().containsKey("data")) {
//            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
//            if (data.containsKey("link") && data.containsKey("deletehash")) {
//                String link = (String) data.get("link");
//                String deleteHash = (String) data.get("deletehash");
//                return new UploadResult(link, deleteHash);
//            }
//        }
//
//        throw new IOException("Не удалось загрузить изображение в Imgur");
//    }
//
//    /**
//     * Удаляет изображение из Imgur по его deletehash
//     *
//     * @param deleteHash хеш удаления изображения
//     * @return true, если изображение было успешно удалено
//     */
//    public boolean deleteImage(String deleteHash) {
//        if (deleteHash == null || deleteHash.isEmpty()) {
//            return false;
//        }
//
//        String deleteUrl = IMGUR_API_URL + "/" + deleteHash;
//
//        HttpHeaders headers = new HttpHeaders();
//        headers.set("Authorization", "Bearer " + accessToken);
//
//        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
//
//        try {
//            ResponseEntity<Map> response = restTemplate.exchange(
//                    deleteUrl,
//                    org.springframework.http.HttpMethod.DELETE,
//                    requestEntity,
//                    Map.class
//            );
//
//            if (response.getBody() != null && response.getBody().containsKey("success")) {
//                return (Boolean) response.getBody().get("success");
//            }
//        } catch (Exception e) {
//            return false;
//        }
//
//        return false;
//    }
private static final String IMGUR_API_URL = "https://api.imgur.com/3/image";
    private static final Logger logger = LoggerFactory.getLogger(ImgurService.class);

    @Value("${imgur.client-id}")
    private String clientId;

    @Value("${imgur.access-token}")
    private String accessToken;

    private final RestTemplate restTemplate;

    // Простой кэш для URL изображений
    private final Map<String, UploadResult> imageCache = new ConcurrentHashMap<>();

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
     * с поддержкой кэширования и повторных попыток
     */
    public UploadResult uploadImage(MultipartFile file) throws IOException {
        // Проверки файла (как в вашем коде)
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

        // Создаем заголовки с правильной аутентификацией
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // Используем Client-ID вместо Bearer token для API Imgur
        headers.set("Authorization", "Client-ID " + clientId);

        // Добавляем User-Agent для отслеживания
        headers.set("User-Agent", "MyApp/1.0");

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", base64Image);

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

    private void logRateLimitInfo(HttpHeaders headers) {
        logger.info("Imgur API Rate Limits: " +
                        "UserLimit={}, UserRemaining={}, UserReset={}, " +
                        "ClientLimit={}, ClientRemaining={}",
                headers.getFirst("X-RateLimit-UserLimit"),
                headers.getFirst("X-RateLimit-UserRemaining"),
                headers.getFirst("X-RateLimit-UserReset"),
                headers.getFirst("X-RateLimit-ClientLimit"),
                headers.getFirst("X-RateLimit-ClientRemaining"));
    }

    // Метод удаления (с теми же улучшениями)
    public boolean deleteImage(String deleteHash) {
        if (deleteHash == null || deleteHash.isEmpty()) {
            return false;
        }

        String deleteUrl = IMGUR_API_URL + "/" + deleteHash;

        HttpHeaders headers = new HttpHeaders();
        // Используем Client-ID вместо Bearer token
        headers.set("Authorization", "Client-ID " + clientId);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    deleteUrl,
                    org.springframework.http.HttpMethod.DELETE,
                    requestEntity,
                    Map.class
            );

            logRateLimitInfo(response.getHeaders());

            if (response.getBody() != null && response.getBody().containsKey("success")) {
                boolean success = (Boolean) response.getBody().get("success");

                // Если успешно удалили, удаляем и из кэша (все записи с этим deleteHash)
                if (success) {
                    imageCache.entrySet().removeIf(entry ->
                            entry.getValue().getDeleteHash().equals(deleteHash));
                }

                return success;
            }
        } catch (Exception e) {
            logger.error("Ошибка при удалении изображения из Imgur: {}", e.getMessage());
            return false;
        }

        return false;
    }

    /**
     * Метод для проверки оставшихся лимитов запросов Imgur API
     * @return Map с информацией о лимитах
     */
    public Map<String, Object> getRateLimitInfo() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Client-ID " + clientId);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        Map<String, Object> rateLimits = new HashMap<>();

        try {
            // Отправляем простой GET-запрос на конечную точку API
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://api.imgur.com/3/credits",
                    HttpMethod.GET,
                    entity,
                    String.class);

            // Получаем заголовки лимитов
            HttpHeaders responseHeaders = response.getHeaders();

            // Извлекаем основную информацию из тела ответа
            try {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response.getBody());
                if (root.has("data")) {
                    JsonNode data = root.get("data");

                    // Безопасное получение значений с проверкой на null
                    if (data.has("ClientRemaining")) {
                        rateLimits.put("ClientRemaining", data.get("ClientRemaining").asInt());
                    }
                    if (data.has("ClientLimit")) {
                        rateLimits.put("ClientLimit", data.get("ClientLimit").asInt());
                    }
                    if (data.has("UserRemaining")) {
                        rateLimits.put("UserRemaining", data.get("UserRemaining").asInt());
                    }
                    if (data.has("UserLimit")) {
                        rateLimits.put("UserLimit", data.get("UserLimit").asInt());
                    }
                    if (data.has("UserUploadRemaining")) {
                        rateLimits.put("UserUploadRemaining", data.get("UserUploadRemaining").asInt());
                    }
                    if (data.has("UserReset")) {
                        long resetTime = data.get("UserReset").asLong();
                        rateLimits.put("ResetTimeSeconds", resetTime - Instant.now().getEpochSecond());
                        rateLimits.put("ResetTime", new Date(resetTime * 1000).toString());
                    }
                }
            } catch (Exception e) {
                rateLimits.put("error", "Ошибка при парсинге ответа: " + e.getMessage());
            }

            // Также собираем информацию из заголовков ответа
            addHeaderToRateLimits(responseHeaders, rateLimits, "X-RateLimit-ClientLimit");
            addHeaderToRateLimits(responseHeaders, rateLimits, "X-RateLimit-ClientRemaining");
            addHeaderToRateLimits(responseHeaders, rateLimits, "X-RateLimit-UserLimit");
            addHeaderToRateLimits(responseHeaders, rateLimits, "X-RateLimit-UserRemaining");
            addHeaderToRateLimits(responseHeaders, rateLimits, "X-RateLimit-UserReset");
        } catch (Exception e) {
            rateLimits.put("error", "Ошибка при запросе к API: " + e.getMessage());
        }

        return rateLimits;
    }

    // Вспомогательный метод для безопасного добавления значений заголовков
    private void addHeaderToRateLimits(HttpHeaders headers, Map<String, Object> rateLimits, String headerName) {
        String value = headers.getFirst(headerName);
        rateLimits.put(headerName, value);
    }
}