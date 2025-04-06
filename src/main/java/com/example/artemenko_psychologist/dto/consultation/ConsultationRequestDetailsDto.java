package com.example.artemenko_psychologist.dto.consultation;

import com.example.artemenko_psychologist.enums.ConsultationStatus;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequestDetailsDto {
    private Long id;
    private String clientName;
    private String phoneNumber;
    private String message;
    private Long serviceId;
    private String serviceName;
    private String serviceDescription;
    private ConsultationStatus status;
    private String adminNotes;
    private LocalDateTime createdAt;
    private LocalDateTime processedAt;
    private String clientEmail;
}