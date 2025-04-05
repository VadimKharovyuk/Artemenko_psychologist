package com.example.artemenko_psychologist.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceDTO {
    private Long id;
    private String title;
    private String description;
    private String shortDescription;
    private BigDecimal price;
    private Integer durationMinutes;
    private String iconClass;
    private Integer displayOrder;
    private boolean active;
    private String imageUrl;
    private MultipartFile imageFile;
}