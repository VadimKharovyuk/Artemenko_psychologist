package com.example.artemenko_psychologist.dto.faq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqUpdateDto {
    private String question;
    private String answer;
    private boolean active;
}
