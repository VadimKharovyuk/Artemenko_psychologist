package com.example.artemenko_psychologist.maper;

import com.example.artemenko_psychologist.dto.consultation.ConsultationRequestCreateDto;
import com.example.artemenko_psychologist.dto.consultation.ConsultationRequestDetailsDto;
import com.example.artemenko_psychologist.dto.consultation.ConsultationRequestListDto;
import com.example.artemenko_psychologist.entity.Service;
import com.example.artemenko_psychologist.enums.ConsultationStatus;
import com.example.artemenko_psychologist.model.ConsultationRequest;
import com.example.artemenko_psychologist.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ConsultationRequestMapper {

    private final ServiceRepository serviceRepository;

    // Преобразование из сущности в DTO списка
    public ConsultationRequestListDto toListDto(ConsultationRequest request) {
        return ConsultationRequestListDto.builder()
                .id(request.getId())
                .clientName(request.getClientName())
                .phoneNumber(request.getPhoneNumber())
                .serviceName(request.getService() != null ? request.getService().getTitle() : "Не указано")
                .status(request.getStatus())
                .createdAt(request.getCreatedAt())
                .isProcessed(request.getProcessedAt() != null)
                .build();
    }

    // Преобразование из сущности в детальное DTO
    public ConsultationRequestDetailsDto toDetailsDto(ConsultationRequest request) {
        return ConsultationRequestDetailsDto.builder()
                .id(request.getId())
                .clientName(request.getClientName())
                .phoneNumber(request.getPhoneNumber())
                .message(request.getMessage())
                .serviceId(request.getService() != null ? request.getService().getId() : null)
                .serviceName(request.getService() != null ? request.getService().getTitle() : "Не указано")
                .serviceDescription(request.getService() != null ? request.getService().getDescription() : "")
                .status(request.getStatus())
                .adminNotes(request.getAdminNotes())
                .createdAt(request.getCreatedAt())
                .processedAt(request.getProcessedAt())
                .clientEmail(request.getClientEmail())
                .build();
    }

    // Преобразование из DTO создания в сущность
    public ConsultationRequest toEntity(ConsultationRequestCreateDto dto) {
        Service service = null;
        if (dto.getServiceId() != null) {
            service = serviceRepository.findById(dto.getServiceId()).orElse(null);
        }

        return ConsultationRequest.builder()
                .clientName(dto.getClientName())
                .phoneNumber(dto.getPhoneNumber())
                .message(dto.getMessage())
                .service(service)
                .status(ConsultationStatus.NEW)
                .clientEmail(dto.getClientEmail())
                .build();
    }

    // Обновление существующей сущности из DTO
    public void updateEntityFromDto(ConsultationRequest entity, ConsultationRequestDetailsDto dto) {
        if (dto.getServiceId() != null) {
            Service service = serviceRepository.findById(dto.getServiceId()).orElse(null);
            entity.setService(service);
        }

        entity.setClientName(dto.getClientName());
        entity.setPhoneNumber(dto.getPhoneNumber());
        entity.setMessage(dto.getMessage());
        entity.setStatus(dto.getStatus());
        entity.setAdminNotes(dto.getAdminNotes());
        entity.setClientEmail(dto.getClientEmail());


        // Обновляем время обработки в зависимости от статуса
        if (dto.getStatus() != ConsultationStatus.NEW && entity.getProcessedAt() == null) {
            entity.setStatusWithTimestamp(dto.getStatus());
        }
    }

    // Преобразование списка сущностей в список DTO
    public List<ConsultationRequestListDto> toListDto(List<ConsultationRequest> requests) {
        return requests.stream()
                .map(this::toListDto)
                .collect(Collectors.toList());
    }
}