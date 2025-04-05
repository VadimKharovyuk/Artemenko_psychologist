package com.example.artemenko_psychologist.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ImgurService {

    private static final String IMGUR_API_URL = "https://api.imgur.com/3/image";

    @Value("${imgur.client-id}")
    private String clientId;

    @Value("${imgur.access-token}")
    private String accessToken;

    private final RestTemplate restTemplate;



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
     *
     * @param file изображение для загрузки
     * @return объект с URL и deleteHash загруженного изображения
     * @throws IOException если произошла ошибка при чтении файла
     */
    public UploadResult uploadImage(MultipartFile file) throws IOException {
        // Проверка на null
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        // Проверка типа файла
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Файл должен быть изображением");
        }

        // Конвертируем файл в Base64
        byte[] fileBytes = file.getBytes();
        String base64Image = Base64.getEncoder().encodeToString(fileBytes);

        // Создаем заголовки запроса
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("Authorization", "Bearer " + accessToken);

        // Создаем body запроса
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("image", base64Image);

        // Создаем HTTP entity
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // Отправляем запрос к Imgur API
        ResponseEntity<Map> response = restTemplate.postForEntity(IMGUR_API_URL, requestEntity, Map.class);

        // Обрабатываем ответ
        if (response.getBody() != null && response.getBody().containsKey("data")) {
            Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
            if (data.containsKey("link") && data.containsKey("deletehash")) {
                String link = (String) data.get("link");
                String deleteHash = (String) data.get("deletehash");
                return new UploadResult(link, deleteHash);
            }
        }

        throw new IOException("Не удалось загрузить изображение в Imgur");
    }

    /**
     * Удаляет изображение из Imgur по его deletehash
     *
     * @param deleteHash хеш удаления изображения
     * @return true, если изображение было успешно удалено
     */
    public boolean deleteImage(String deleteHash) {
        if (deleteHash == null || deleteHash.isEmpty()) {
            return false;
        }

        String deleteUrl = IMGUR_API_URL + "/" + deleteHash;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    deleteUrl,
                    org.springframework.http.HttpMethod.DELETE,
                    requestEntity,
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("success")) {
                return (Boolean) response.getBody().get("success");
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }
}