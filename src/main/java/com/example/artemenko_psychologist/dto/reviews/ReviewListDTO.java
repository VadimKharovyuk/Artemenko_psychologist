package com.example.artemenko_psychologist.dto.reviews;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewListDTO {
    private Long id;
    private String previewImageUrl;
    private String description;
    private LocalDateTime createdAt;
    private String username;
    private Long serviceId;
}