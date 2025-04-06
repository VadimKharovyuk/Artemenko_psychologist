package com.example.artemenko_psychologist.dto.consultation;

import com.example.artemenko_psychologist.enums.ConsultationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequestListDto {
    private Long id;
    private String clientName;
    private String phoneNumber;
    private String serviceName;
    private ConsultationStatus status;
    private LocalDateTime createdAt;
    private boolean isProcessed;
}