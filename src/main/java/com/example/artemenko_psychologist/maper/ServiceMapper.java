package com.example.artemenko_psychologist.maper;

import com.example.artemenko_psychologist.dto.service.ServiceCardDTO;
import com.example.artemenko_psychologist.dto.service.ServiceCreateFormDTO;
import com.example.artemenko_psychologist.dto.service.ServiceDTO;
import com.example.artemenko_psychologist.dto.service.ServiceUpdateFormDTO;
import com.example.artemenko_psychologist.model.Service;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Маппер для преобразования между сущностью Service и различными DTO
 */
@Component
public class ServiceMapper {

    /**
     * Преобразует DTO в Entity
     */
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
                .publicId(dto.getPublicId())
                .build();
    }

    /**
     * Преобразует форму создания в Entity
     */
    public Service toEntity(ServiceCreateFormDTO formDTO) {
        if (formDTO == null) {
            return null;
        }

        return Service.builder()
                .title(formDTO.getTitle())
                .description(formDTO.getDescription())
                .shortDescription(formDTO.getShortDescription())
                .price(formDTO.getPrice())
                .durationMinutes(formDTO.getDurationMinutes())
                .iconClass(formDTO.getIconClass())
                .imageUrl(formDTO.getImageUrl())
                .publicId(formDTO.getPublicId())
                .displayOrder(formDTO.getDisplayOrder())
                .active(formDTO.isActive())
                .build();
    }

    /**
     * Обновляет существующую сущность данными из формы обновления
     */
    public void updateEntity(Service service, ServiceUpdateFormDTO formDTO) {
        if (service == null || formDTO == null) {
            return;
        }

        service.setTitle(formDTO.getTitle());
        service.setDescription(formDTO.getDescription());
        service.setShortDescription(formDTO.getShortDescription());
        service.setPrice(formDTO.getPrice());
        service.setDurationMinutes(formDTO.getDurationMinutes());
        service.setIconClass(formDTO.getIconClass());
        service.setDisplayOrder(formDTO.getDisplayOrder());
        service.setActive(formDTO.isActive());

        // Обновляем URL изображения только если он предоставлен в DTO
        if (formDTO.getImageUrl() != null) {
            service.setImageUrl(formDTO.getImageUrl());
        }

        // Обновляем publicId только если он предоставлен в DTO
        if (formDTO.getPublicId() != null) {
            service.setPublicId(formDTO.getPublicId());
        }
    }

    /**
     * Преобразует Entity в DTO
     */
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
                .publicId(entity.getPublicId())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.isActive())
                .build();
    }

    /**
     * Преобразует Entity в DTO карточки услуги
     */
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

    /**
     * Преобразует Entity в DTO для формы обновления
     */
    public ServiceUpdateFormDTO toUpdateFormDto(Service entity) {
        if (entity == null) {
            return null;
        }

        return ServiceUpdateFormDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .shortDescription(entity.getShortDescription())
                .price(entity.getPrice())
                .durationMinutes(entity.getDurationMinutes())
                .iconClass(entity.getIconClass())
                .imageUrl(entity.getImageUrl())
                .publicId(entity.getPublicId())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.isActive())
                .build();
    }

    /**
     * Преобразует список сущностей в список DTO
     */
    public List<ServiceDTO> toDtoList(List<Service> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует список сущностей в список карточек DTO
     */
    public List<ServiceCardDTO> toCardDtoList(List<Service> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toCardDto)
                .collect(Collectors.toList());
    }
}