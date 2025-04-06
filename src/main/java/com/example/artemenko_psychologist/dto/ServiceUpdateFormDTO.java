package com.example.artemenko_psychologist.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

/**
 * DTO для формы редактирования услуги
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceUpdateFormDTO {
    @NotNull(message = "ID услуги обязателен для редактирования")
    private Long id;

    @NotBlank(message = "Название услуги не может быть пустым")
    @Size(max = 100, message = "Название услуги не может превышать 100 символов")
    private String title;

    @NotBlank(message = "Описание услуги не может быть пустым")
    @Size(min = 10, max = 30000, message = "Описание должно содержать от 10 до 30000 символов")
    private String description;

    @NotBlank(message = "Краткое описание не может быть пустым")

    private String shortDescription;

    @NotNull(message = "Цена не может быть пустой")
    @Min(value = 1, message = "Минимальная цена должна быть больше 0")
    private BigDecimal price;

    private Integer durationMinutes;

    private String iconClass;

    private Integer displayOrder;

    private boolean active;

    // Существующее изображение
    private String imageUrl;

    // Новое изображение (если загружается)
    private MultipartFile imageFile;
}