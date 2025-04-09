package com.example.artemenko_psychologist.dto.faq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqCreateDto {
    private String question;
    private String answer;
    private boolean active;
}