package com.example.artemenko_psychologist.util;//package com.example.artemenko_psychologist.controller;
//
//import com.example.artemenko_psychologist.util.CloudinaryUsageService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import java.util.Map;
//import java.util.Optional;
//
//@Controller
//@RequestMapping("/cloudinary")
//@RequiredArgsConstructor
//public class CloudinaryUsageController {
//
//    private final CloudinaryUsageService cloudinaryUsageService;
//
//    @GetMapping("/usage")
//    public String getCloudinaryUsage(Model model) {
//        try {
//            Map<String, Object> usage = cloudinaryUsageService.getAccountUsage();
//
//            // Безопасное получение значений с обработкой null
//            model.addAttribute("totalStorage", formatBytes(getLongValue(usage, "total_storage")));
//            model.addAttribute("storageLimitTotal", formatBytes(getLongValue(usage, "storage_limit")));
//            model.addAttribute("totalBandwidth", formatBytes(getLongValue(usage, "total_bandwidth")));
//            model.addAttribute("bandwidthLimitTotal", formatBytes(getLongValue(usage, "bandwidth_limit")));
//
//            // Прямые значения
//            model.addAttribute("transformations",
//                    Optional.ofNullable(usage.get("transformations")).orElse("Н/Д"));
//
//            // Расчет процентов использования с обработкой null
//            long totalStorage = getLongValue(usage, "total_storage");
//            long storageLimit = getLongValue(usage, "storage_limit");
//            long totalBandwidth = getLongValue(usage, "total_bandwidth");
//            long bandwidthLimit = getLongValue(usage, "bandwidth_limit");
//
//            model.addAttribute("storageUsagePercent", calculatePercent(totalStorage, storageLimit));
//            model.addAttribute("bandwidthUsagePercent", calculatePercent(totalBandwidth, bandwidthLimit));
//
//            // Добавим сырые данные для отладки
//            model.addAttribute("rawUsageData", usage);
//
//            return "cloudinary-usage";
//        } catch (Exception e) {
//            model.addAttribute("error", "Не удалось получить статистику: " + e.getMessage());
//            return "cloudinary-usage";
//        }
//    }
//
//    // Безопасный метод получения Long значения
//    private long getLongValue(Map<String, Object> map, String key) {
//        Object value = map.get(key);
//        if (value instanceof Number) {
//            return ((Number) value).longValue();
//        }
//        return 0L;
//    }
//
//    // Вспомогательный метод для форматирования байт
//    private String formatBytes(long bytes) {
//        if (bytes <= 0) return "0 B";
//        String[] units = {"B", "KB", "MB", "GB", "TB"};
//        int digitGroups = (int) (Math.log10(Math.max(bytes, 1)) / Math.log10(1024));
//        return String.format("%.2f %s",
//                bytes / Math.pow(1024, digitGroups),
//                units[Math.min(digitGroups, units.length - 1)]);
//    }
//
//    // Вспомогательный метод для расчета процента использования
//    private double calculatePercent(long used, long total) {
//        if (total <= 0) return 0;
//        return (double) used / total * 100;
//    }
//}



import com.example.artemenko_psychologist.util.CloudinaryUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/cloudinary")
@RequiredArgsConstructor
public class CloudinaryUsageController {

    private final CloudinaryUsageService cloudinaryUsageService;

    @GetMapping("/usage")
    public String getCloudinaryUsage(Model model) {
        try {
            Map<String, Object> usage = cloudinaryUsageService.getAccountUsage();

            // Обработка хранилища
            Map<String, Object> storageData = (Map<String, Object>) usage.get("storage");
            long storageUsage = storageData != null ?
                    Long.parseLong(storageData.get("usage").toString()) : 0L;

            // Обработка кредитов
            Map<String, Object> creditsData = (Map<String, Object>) usage.get("credits");
            double creditsUsedPercent = creditsData != null ?
                    Double.parseDouble(creditsData.get("used_percent").toString()) : 0.0;

            // Обработка объектов
            Map<String, Object> objectsData = (Map<String, Object>) usage.get("objects");
            int objectsUsage = objectsData != null ?
                    Integer.parseInt(objectsData.get("usage").toString()) : 0;

            // Обработка трансформаций
            Map<String, Object> transformationsData = (Map<String, Object>) usage.get("transformations");
            int transformationsUsage = transformationsData != null ?
                    Integer.parseInt(transformationsData.get("usage").toString()) : 0;

            // Медиа лимиты
            Map<String, Object> mediaLimitsData = (Map<String, Object>) usage.get("media_limits");

            // Подготовка данных для модели
            model.addAttribute("totalStorage", formatBytes(storageUsage));
            model.addAttribute("storageUsagePercent", formatStorageUsage(storageUsage));
            model.addAttribute("creditsUsedPercent", creditsUsedPercent);
            model.addAttribute("objectsCount", objectsUsage);
            model.addAttribute("transformationsCount", transformationsUsage);
            model.addAttribute("planType", usage.get("plan"));

            // Лимиты медиафайлов
            if (mediaLimitsData != null) {
                model.addAttribute("imageMaxSizeBytes",
                        formatBytes(Long.parseLong(mediaLimitsData.get("image_max_size_bytes").toString())));
                model.addAttribute("videoMaxSizeBytes",
                        formatBytes(Long.parseLong(mediaLimitsData.get("video_max_size_bytes").toString())));
            }

            // Добавим сырые данные для отладки
            model.addAttribute("rawUsageData", usage);

            return "cloudinary-usage";
        } catch (Exception e) {
            model.addAttribute("error", "Не удалось получить статистику: " + e.getMessage());
            return "cloudinary-usage";
        }
    }

    // Форматирование байт с использованием подходящих единиц
    private String formatBytes(long bytes) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        if (bytes == 0) return "0 B";

        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        return String.format("%.2f %s",
                bytes / Math.pow(1024, digitGroups),
                units[Math.min(digitGroups, units.length - 1)]);
    }

    // Расчет процента использования хранилища
    private double formatStorageUsage(long storageUsage) {
        // Для Free плана обычно лимит около 10 ГБ (10 * 1024 * 1024 * 1024)
        long storageLimitBytes = 10_737_418_240L;
        return (double) storageUsage / storageLimitBytes * 100;
    }
}