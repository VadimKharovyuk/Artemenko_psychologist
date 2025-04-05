package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.dto.ServiceCardDTO;
import com.example.artemenko_psychologist.dto.ServiceDTO;
import com.example.artemenko_psychologist.entity.Service;

import com.example.artemenko_psychologist.exception.ResourceNotFoundException;
import com.example.artemenko_psychologist.maper.ServiceMapper;
import com.example.artemenko_psychologist.repository.ServiceRepository;
import com.example.artemenko_psychologist.util.ImgurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final ImgurService imgurService;

    /**
     * Получает список всех активных услуг для отображения на главной странице
     * @return список DTO карточек услуг
     */
    public List<ServiceCardDTO> getAllActiveServiceCards() {
        return serviceMapper.toCardDtoList(
                serviceRepository.findByActiveTrueOrderByDisplayOrderAsc()
        );
    }


    public List<ServiceDTO> getAllServices() {
        return serviceMapper.toDtoList(serviceRepository.findAll());
    }


    public ServiceDTO getServiceById(Long id) throws ResourceNotFoundException {
        return serviceMapper.toDto(
                serviceRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Услуга с ID " + id + " не найдена"))
        );
    }

    @Transactional
    public ServiceDTO saveService(ServiceDTO serviceDTO) throws IOException {
        // Преобразуем DTO в сущность
        Service service = serviceMapper.toEntity(serviceDTO);

        // Обрабатываем загрузку изображения, если оно есть
        MultipartFile imageFile = serviceDTO.getImageFile();
        if (imageFile != null && !imageFile.isEmpty()) {
            // Если обновляем существующую услугу и у нее есть изображение, удаляем старое
            if (service.getId() != null) {
                Optional<Service> existingService = serviceRepository.findById(service.getId());
                if (existingService.isPresent() && existingService.get().getDeleteHash() != null) {
                    imgurService.deleteImage(existingService.get().getDeleteHash());
                }
            }

            // Загружаем новое изображение
            ImgurService.UploadResult uploadResult = imgurService.uploadImage(imageFile);
            service.setImageUrl(uploadResult.getUrl());
            service.setDeleteHash(uploadResult.getDeleteHash());
        } else if (service.getId() != null) {
            // Если изображение не загружается, но услуга обновляется,
            // сохраняем существующее изображение
            Optional<Service> existingService = serviceRepository.findById(service.getId());
            if (existingService.isPresent()) {
                service.setImageUrl(existingService.get().getImageUrl());
                service.setDeleteHash(existingService.get().getDeleteHash());
            }
        }

        // Сохраняем сущность
        Service savedService = serviceRepository.save(service);

        // Преобразуем обратно в DTO и возвращаем
        return serviceMapper.toDto(savedService);
    }

    @Transactional
    public void deleteService(Long id) throws ResourceNotFoundException {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Услуга с ID " + id + " не найдена"));

        // Удаляем изображение, если оно есть
        if (service.getDeleteHash() != null) {
            imgurService.deleteImage(service.getDeleteHash());
        }

        serviceRepository.delete(service);
    }

    @Transactional
    public ServiceDTO toggleServiceActive(Long id) throws ResourceNotFoundException {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Услуга с ID " + id + " не найдена"));

        service.setActive(!service.isActive());
        Service savedService = serviceRepository.save(service);

        return serviceMapper.toDto(savedService);
    }
}