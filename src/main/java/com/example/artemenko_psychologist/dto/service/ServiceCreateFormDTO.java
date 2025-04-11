package com.example.artemenko_psychologist.dto.service;

import jakarta.persistence.Column;
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
 * DTO для формы создания и редактирования услуги
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCreateFormDTO {
    private Long id;

    @NotBlank(message = "Название услуги не может быть пустым")
    @Size(max = 100, message = "Название услуги не может превышать 100 символов")
    private String title;

    @NotBlank(message = "Описание услуги не может быть пустым")
    @Size(min = 10, max = 30000, message = "Описание должно содержать от 10 до 30000 символов")
    private String description;

    @NotBlank(message = "Краткое описание не может быть пустым")
    @Size(min = 10, max = 100, message = "Краткое описание должно содержать от 10 до 100 символов")
    private String shortDescription;

    @NotNull(message = "Цена не может быть пустой")
    @Min(value = 1, message = "Минимальная цена должна быть больше 0")
    private BigDecimal price;

    private Integer durationMinutes;

    private String iconClass;

    private Integer displayOrder;

    private boolean active = true;

    private MultipartFile imageFile;
    private String imageUrl;
    private String publicId;
}