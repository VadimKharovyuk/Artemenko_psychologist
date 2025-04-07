package com.example.artemenko_psychologist.dto.reviews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewCreateDTO {
    private String previewImageUrl;
    private String deleteHash;    // Может быть не нужен, если генерируется автоматически
    private String description;
    private String username;
    private Long serviceId;
}