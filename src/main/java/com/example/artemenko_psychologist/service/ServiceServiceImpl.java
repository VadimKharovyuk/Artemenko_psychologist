package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.dto.service.ServiceCardDTO;
import com.example.artemenko_psychologist.dto.service.ServiceCreateFormDTO;
import com.example.artemenko_psychologist.dto.service.ServiceDTO;
import com.example.artemenko_psychologist.dto.service.ServiceUpdateFormDTO;
import com.example.artemenko_psychologist.maper.ServiceMapper;
import com.example.artemenko_psychologist.model.Service;
import com.example.artemenko_psychologist.repository.ServiceRepository;

import com.example.artemenko_psychologist.service.impl.ServiceService;
import com.example.artemenko_psychologist.util.CloudinaryService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Реализация сервиса для работы с услугами
 */
@Slf4j
@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceServiceImpl implements ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final CloudinaryService cloudinaryService;

    @Override
    public List<ServiceDTO> getAllActiveServices() {
        List<Service> services = serviceRepository.findByActiveTrue();
        return serviceMapper.toDtoList(services);
    }

    @Override
    public List<ServiceDTO> getAllServices() {
        List<Service> services = serviceRepository.findAll();
        return serviceMapper.toDtoList(services);
    }

    @Override
    public List<ServiceCardDTO> getAllActiveServiceCards() {
        List<Service> services = serviceRepository.findByActiveTrueOrderByDisplayOrderAsc();
        return serviceMapper.toCardDtoList(services);
    }

    @Override
    public ServiceDTO getServiceById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Услуга с ID " + id + " не найдена"));
        return serviceMapper.toDto(service);
    }

    @Override
    public ServiceUpdateFormDTO getServiceUpdateFormById(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Услуга с ID " + id + " не найдена"));
        return serviceMapper.toUpdateFormDto(service);
    }

    @Override
    @Transactional
    public ServiceDTO createService(ServiceCreateFormDTO formDTO, MultipartFile imageFile) throws IOException {
        // Загружаем изображение, если оно предоставлено
        if (imageFile != null && !imageFile.isEmpty()) {
            CloudinaryService.UploadResult uploadResult = uploadServiceImage(imageFile);
            formDTO.setImageUrl(uploadResult.getUrl());
            formDTO.setPublicId(uploadResult.getPublicId());
        }

        // Создаем и сохраняем новую услугу
        Service service = serviceMapper.toEntity(formDTO);
        service.setCreatedAt(LocalDateTime.now());
        Service savedService = serviceRepository.save(service);

        log.info("Создана новая услуга с ID: {}", savedService.getId());
        return serviceMapper.toDto(savedService);
    }

    @Override
    @Transactional
    public ServiceDTO createOrUpdateService(@Valid ServiceCreateFormDTO serviceForm) {
        MultipartFile imageFile = serviceForm.getImageFile();
        try {
            // Если id существует, обновляем
            if (serviceForm.getId() != null) {
                ServiceUpdateFormDTO updateFormDTO = new ServiceUpdateFormDTO();
                updateFormDTO.setId(serviceForm.getId());
                updateFormDTO.setTitle(serviceForm.getTitle());
                updateFormDTO.setDescription(serviceForm.getDescription());
                updateFormDTO.setShortDescription(serviceForm.getShortDescription());
                updateFormDTO.setPrice(serviceForm.getPrice());
                updateFormDTO.setDurationMinutes(serviceForm.getDurationMinutes());
                updateFormDTO.setIconClass(serviceForm.getIconClass());
                updateFormDTO.setDisplayOrder(serviceForm.getDisplayOrder());
                updateFormDTO.setActive(serviceForm.isActive());

                return updateService(updateFormDTO, imageFile);
            }
            // Иначе создаём новую услугу
            else {
                return createService(serviceForm, imageFile);
            }
        } catch (IOException e) {
            log.error("Ошибка при загрузке изображения: {}", e.getMessage());
            throw new RuntimeException("Ошибка при загрузке изображения: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ServiceCardDTO> getTopActiveServiceCards(int limit) {
        return serviceRepository.findByActiveTrue().stream()
                .limit(limit)
                .map(serviceMapper::toCardDto)
                .collect(Collectors.toList());
    }

    @Override
    public long countActiveServices() {
       return serviceRepository.count();
    }

    @Override
    public List<ServiceDTO> getLatestServices(int limit) {
        return serviceRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .limit(limit)
                .map(serviceMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ServiceDTO updateService(ServiceUpdateFormDTO formDTO, MultipartFile imageFile) throws IOException {
        Service service = serviceRepository.findById(formDTO.getId())
                .orElseThrow(() -> new EntityNotFoundException("Услуга с ID " + formDTO.getId() + " не найдена"));

        // Обрабатываем изображение, если предоставлено новое
        if (imageFile != null && !imageFile.isEmpty()) {
            // Если есть старое изображение, удаляем его
            if (service.getPublicId() != null && !service.getPublicId().isEmpty()) {
                deleteServiceImage(service.getPublicId());
            }

            // Загружаем новое изображение
            CloudinaryService.UploadResult uploadResult = uploadServiceImage(imageFile);
            formDTO.setImageUrl(uploadResult.getUrl());
            formDTO.setPublicId(uploadResult.getPublicId());
        } else {
            // Сохраняем существующие данные изображения
            formDTO.setImageUrl(service.getImageUrl());
            formDTO.setPublicId(service.getPublicId());
        }

        // Обновляем сущность и сохраняем изменения
        serviceMapper.updateEntity(service, formDTO);
        service.setUpdatedAt(LocalDateTime.now());
        Service updatedService = serviceRepository.save(service);

        log.info("Обновлена услуга с ID: {}", updatedService.getId());
        return serviceMapper.toDto(updatedService);
    }

    @Override
    @Transactional
    public void deleteService(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Услуга с ID " + id + " не найдена"));

        // Удаляем изображение, если оно есть
        if (service.getPublicId() != null && !service.getPublicId().isEmpty()) {
            deleteServiceImage(service.getPublicId());
        }

        // Удаляем услугу из базы данных
        serviceRepository.delete(service);
        log.info("Удалена услуга с ID: {}", id);
    }

    @Override
    @Transactional
    public ServiceDTO toggleServiceActive(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Услуга с ID " + id + " не найдена"));

        // Инвертируем текущий статус активности
        boolean newActiveStatus = !service.isActive();
        service.setActive(newActiveStatus);

        service.setUpdatedAt(LocalDateTime.now());
        Service updatedService = serviceRepository.save(service);

        log.info("Изменен статус активности услуги с ID: {}. Новый статус: {}", id, newActiveStatus);
        return serviceMapper.toDto(updatedService);
    }

    @Override
    public CloudinaryService.UploadResult uploadServiceImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл изображения не может быть пустым");
        }

        log.info("Начало загрузки изображения услуги: {}", file.getOriginalFilename());

        // Используем CloudinaryService для загрузки изображения
        CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadImage(file);

        log.info("Изображение услуги успешно загружено. URL: {}, Public ID: {}",
                uploadResult.getUrl(), uploadResult.getPublicId());

        return uploadResult;
    }

    @Override
    public boolean deleteServiceImage(String publicId) {
        if (publicId == null || publicId.isEmpty()) {
            log.warn("Попытка удаления изображения с пустым publicId");
            return false;
        }

        log.info("Удаление изображения услуги с Public ID: {}", publicId);
        return cloudinaryService.deleteImage(publicId);
    }
}