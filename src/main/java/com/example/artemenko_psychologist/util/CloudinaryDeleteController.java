package com.example.artemenko_psychologist.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
@RequestMapping("/info")
@Controller
@Slf4j
public class CloudinaryDeleteController {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @GetMapping("/cloudinary/delete-samples")
    public String deleteSampleResources(Model model) {
        try {
            // Инициализация Cloudinary
            Cloudinary cloudinary = new Cloudinary(
                    "cloudinary://" + apiKey + ":" + apiSecret + "@" + cloudName
            );

            // Параметры запроса
            Map<String, Object> params = ObjectUtils.asMap(
                    "type", "upload",
                    "resource_type", "image",
                    "max_results", 500
            );

            // Получаем список ресурсов
            Map<String, Object> result = cloudinary.api().resources(params);
            List<Map<String, Object>> resources = (List<Map<String, Object>>) result.get("resources");

            // Счетчики
            int deletedCount = 0;
            int totalCount = resources.size();

            // Удаляем демо-изображения
            for (Map<String, Object> resource : resources) {
                String publicId = (String) resource.get("public_id");

                // Проверяем, является ли изображение демонстрационным
                if (publicId.contains("cld-sample") ||
                        publicId.contains("samples/") ||
                        publicId.startsWith("sample")) {

                    // Непосредственно удаление
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    deletedCount++;

                    log.info("Удалено демо-изображение: {}", publicId);
                }
            }

            // Формируем сообщение
            String message = String.format(
                    "Удалено %d демо-изображений из %d total",
                    deletedCount,
                    totalCount
            );

            model.addAttribute("message", message);
            return "cloudinary-resources";
        } catch (Exception e) {
            log.error("Ошибка при удалении ресурсов", e);
            model.addAttribute("error", "Не удалось удалить ресурсы: " + e.getMessage());
            return "cloudinary-resources";
        }
    }
}