package com.example.artemenko_psychologist.dto.reviews;

import lombok.*;

import java.time.LocalDateTime;
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailsDTO {
    private Long id;
    private String previewImageUrl;
    private String description;
    private LocalDateTime createdAt;
    private String username;
    private Long serviceId;
    private String serviceName;
}