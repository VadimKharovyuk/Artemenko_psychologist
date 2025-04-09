package com.example.artemenko_psychologist.dto.faq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqDto {
    private Long id;
    private String question;
    private String answer;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}