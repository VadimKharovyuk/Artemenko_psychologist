package com.example.artemenko_psychologist.maper;

import com.example.artemenko_psychologist.dto.ServiceCardDTO;
import com.example.artemenko_psychologist.dto.ServiceCreateFormDTO;
import com.example.artemenko_psychologist.dto.ServiceDTO;
import com.example.artemenko_psychologist.dto.ServiceUpdateFormDTO;
import com.example.artemenko_psychologist.entity.Service;
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
                // Удален дублирующийся imageUrl
                .build();
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
                .displayOrder(entity.getDisplayOrder())
                .active(entity.isActive())
                // Удален дублирующийся imageUrl
                .build();
    }

    /**
     * Преобразует Entity в DTO для карточки услуги (для главной страницы)
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
     * Преобразует список Entity в список DTO
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
     * Преобразует список Entity в список DTO для карточек
     */
    public List<ServiceCardDTO> toCardDtoList(List<Service> entities) {
        if (entities == null) {
            return null;
        }

        return entities.stream()
                .map(this::toCardDto)
                .collect(Collectors.toList());
    }

    /**
     * Преобразует DTO формы создания в Entity
     */
    public Service toEntityFromForm(ServiceCreateFormDTO formDTO) {
        if (formDTO == null) {
            return null;
        }

        return Service.builder()
                .id(formDTO.getId())
                .title(formDTO.getTitle())
                .description(formDTO.getDescription())
                .shortDescription(formDTO.getShortDescription())
                .price(formDTO.getPrice())
                .durationMinutes(formDTO.getDurationMinutes())
                .iconClass(formDTO.getIconClass())
                .displayOrder(formDTO.getDisplayOrder())
                .active(formDTO.isActive())
                .imageUrl(formDTO.getImageUrl()) // Добавлен imageUrl
                .build();
    }

    /**
     * Преобразует Entity в DTO для формы создания/редактирования
     */
    public ServiceCreateFormDTO toCreateFormDto(Service entity) {
        if (entity == null) {
            return null;
        }

        String shortDescription = ensureValidShortDescription(entity.getShortDescription());

        return ServiceCreateFormDTO.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .shortDescription(shortDescription)
                .price(entity.getPrice())
                .durationMinutes(entity.getDurationMinutes())
                .iconClass(entity.getIconClass())
                .displayOrder(entity.getDisplayOrder())
                .active(entity.isActive())
                .imageUrl(entity.getImageUrl()) // Добавлен imageUrl
                .build();
    }

    /**
     * Обновляет Entity из DTO формы создания
     */
    public void updateServiceFromForm(ServiceCreateFormDTO formDTO, Service service) {
        if (formDTO == null || service == null) {
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
        // Не обновляем imageUrl, так как это делается отдельно при обработке загрузки файла
    }

    /**
     * Преобразует Entity в DTO для формы обновления
     */
    public ServiceUpdateFormDTO toUpdateFormDto(Service service) {
        if (service == null) {
            return null;
        }

        String shortDescription = ensureValidShortDescription(service.getShortDescription());

        return ServiceUpdateFormDTO.builder()
                .id(service.getId())
                .title(service.getTitle())
                .description(service.getDescription())
                .shortDescription(shortDescription)
                .price(service.getPrice())
                .durationMinutes(service.getDurationMinutes())
                .iconClass(service.getIconClass())
                .displayOrder(service.getDisplayOrder())
                .active(service.isActive())
                .imageUrl(service.getImageUrl())
                .build();
    }

    /**
     * Обновляет Entity из DTO формы обновления
     */
    public void updateServiceFromDto(Service service, ServiceUpdateFormDTO updateForm) {
        if (updateForm == null || service == null) {
            return;
        }

        service.setTitle(updateForm.getTitle());
        service.setDescription(updateForm.getDescription());
        service.setShortDescription(updateForm.getShortDescription());
        service.setPrice(updateForm.getPrice());
        service.setDurationMinutes(updateForm.getDurationMinutes());
        service.setIconClass(updateForm.getIconClass());
        service.setDisplayOrder(updateForm.getDisplayOrder());
        service.setActive(updateForm.isActive());
        // Не обновляем imageUrl, так как это делается отдельно при обработке загрузки файла
    }

    /**
     * Обеспечивает, что краткое описание соответствует требованиям валидации
     */
    private String ensureValidShortDescription(String shortDescription) {
        if (shortDescription == null || shortDescription.isEmpty()) {
            return "Краткое описание услуги";
        } else if (shortDescription.length() < 10) {
            // Дополняем до минимальной длины
            return shortDescription + " " + "дополнительная информация".substring(0, 10 - shortDescription.length());
        }
        return shortDescription;
    }
}