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

@Controller
@RequestMapping("/cloudinary")
@Slf4j
public class CloudinaryResourceController {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @GetMapping("/resources")
    public String listCloudinaryResources(Model model) {
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

            // Извлекаем список ресурсов
            List<Map<String, Object>> resources = (List<Map<String, Object>>) result.get("resources");

            // Логируем полную информацию о первом ресурсе
            if (!resources.isEmpty()) {
                Map<String, Object> firstResource = resources.get(0);
                for (Map.Entry<String, Object> entry : firstResource.entrySet()) {

                }
            }

            // Добавляем в модель
            model.addAttribute("resourceList", resources);
            model.addAttribute("resourceCount", resources.size());

            return "cloudinary-resources";
        } catch (Exception e) {
            log.error("Ошибка при получении ресурсов", e);
            model.addAttribute("error", "Не удалось получить ресурсы: " + e.getMessage());
            return "cloudinary-resources";
        }
    }

    @GetMapping("/delete-samples")
    public String deleteSampleResources(Model model) {
        try {
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

            // Удаляем демо-изображения
            for (Map<String, Object> resource : resources) {
                String publicId = (String) resource.get("public_id");

                // Проверяем, является ли изображение демонстрационным
                if (publicId.contains("cld-sample") || publicId.contains("samples/")) {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    log.info("Удалено демо-изображение: {}", publicId);
                }
            }

            model.addAttribute("message", "Демо-изображения удалены");
            return "cloudinary-delete-result";
        } catch (Exception e) {
            log.error("Ошибка при удалении ресурсов", e);
            model.addAttribute("error", "Не удалось удалить ресурсы: " + e.getMessage());
            return "cloudinary-delete-result";
        }
    }
}