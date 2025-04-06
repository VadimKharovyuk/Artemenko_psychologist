package com.example.artemenko_psychologist.dto.consultation;

import com.example.artemenko_psychologist.enums.ConsultationStatus;
import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequestCreateDto {
    @NotBlank(message = "Ім'я обов'язкове")
    @Size(min = 2, max = 100, message = "Ім'я повинно містити від 2 до 100 символів")
    private String clientName;

    @NotBlank(message = "Телефон обов'язковий")
    @Pattern(regexp = "^\\+?[0-9\\s-()]{7,20}$", message = "Некоректний формат телефону")
    private String phoneNumber;

    @NotNull(message = "Послуга обов'язкова")
    private Long serviceId;

    @Size(max = 1000, message = "Повідомлення не може бути довшим за 1000 символів")
    private String message;

    @Column(name = "client_email", length = 100)
    private String clientEmail;
}