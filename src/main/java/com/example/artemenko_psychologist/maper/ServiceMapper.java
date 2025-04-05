package com.example.artemenko_psychologist.maper;

import com.example.artemenko_psychologist.dto.ServiceCardDTO;
import com.example.artemenko_psychologist.dto.ServiceDTO;
import com.example.artemenko_psychologist.entity.Service;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceMapper {

    // DTO -> Entity
    public Service toEntity(ServiceDTO dto) {
        if (dto == null) {
            return null;
        }

        return Service.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .shortDescription(dto.getShortDescription())
                .price(dto.getPrice())
                .durationMinutes(dto.getDurationMinutes())
                .iconClass(dto.getIconClass())
                .imageUrl(dto.getImageUrl())
                .displayOrder(dto.getDisplayOrder())
                .active(dto.isActive())
                .build();
    }

    // Entity -> DTO
    public ServiceDTO toDto(Service entity) {
        if (entity == null) {
            return null;
        }

        return ServiceDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .shortDescription(entity.getShortDescription())
                .price(entity.getPrice())
                .durationMinutes(entity.getDurationMinutes())
                .iconClass(entity.getIconClass())
                .imageUrl(entity.getImageUrl())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.isActive())
                .build();
    }

    // Entity -> ServiceCardDTO (для главной страницы)
    public ServiceCardDTO toCardDto(Service entity) {
        if (entity == null) {
            return null;
        }

        return ServiceCardDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .shortDescription(entity.getShortDescription())
                .iconClass(entity.getIconClass())
                .build();
    }

    // Список Entity -> Список DTO
    public List<ServiceDTO> toDtoList(List<Service> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    // Список Entity -> Список ServiceCardDTO
    public List<ServiceCardDTO> toCardDtoList(List<Service> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toCardDto)
                .collect(Collectors.toList());
    }
}