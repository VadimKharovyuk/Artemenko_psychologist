package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.dto.service.ServiceCardDTO;
import com.example.artemenko_psychologist.dto.service.ServiceDTO;
import com.example.artemenko_psychologist.dto.service.ServiceCreateFormDTO;
import com.example.artemenko_psychologist.dto.service.ServiceUpdateFormDTO;
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
import java.util.stream.Collectors;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
@Slf4j
public class ServiceService {

    private final ServiceRepository serviceRepository;
    private final ServiceMapper serviceMapper;
    private final ImgurService imgurService;



    /**
     * Получает список всех услуг
     * @return список всех услуг
     */
    public List<ServiceDTO> getAllServices() {
        return serviceMapper.toDtoList(serviceRepository.findAll());
    }

    /**
     * Получает услугу по ID
     * @param id идентификатор услуги
     * @return DTO услуги
     * @throws ResourceNotFoundException если услуга не найдена
     */
    public ServiceDTO getServiceById(Long id) throws ResourceNotFoundException {
        return serviceMapper.toDto(findServiceById(id));
    }

    /**
     * Получение данных для формы редактирования услуги
     * @param id идентификатор услуги
     * @return DTO для формы редактирования
     * @throws ResourceNotFoundException если услуга не найдена
     */
    public ServiceCreateFormDTO getServiceFormById(Long id) {
        Service service = findServiceById(id);
        ServiceCreateFormDTO formDTO = serviceMapper.toCreateFormDto(service);
        // Добавляем URL изображения (отсутствует в стандартном маппере)
        formDTO.setImageUrl(service.getImageUrl());
        return formDTO;
    }

    /**
     * Получение данных формы для редактирования с валидацией данных
     * @param id идентификатор услуги
     * @return DTO для формы редактирования с валидированными данными
     * @throws ResourceNotFoundException если услуга не найдена
     */
    public ServiceUpdateFormDTO getServiceUpdateFormById(Long id) {
        return serviceMapper.toUpdateFormDto(findServiceById(id));
    }

    /**
     * Создание или обновление услуги
     * @param formDTO данные формы
     * @return DTO созданной/обновленной услуги
     * @throws IOException если произошла ошибка при обработке изображения
     */
    @Transactional
    public ServiceDTO createOrUpdateService(ServiceCreateFormDTO formDTO) throws IOException {
        // Получаем или создаем сущность
        Service service;
        if (formDTO.getId() != null) {
            // Обновление существующей услуги
            service = findServiceById(formDTO.getId());
            serviceMapper.updateServiceFromForm(formDTO, service);
        } else {
            // Создание новой услуги
            service = serviceMapper.toEntityFromForm(formDTO);
        }

        // Обрабатываем загрузку изображения
        processImageUpload(service, formDTO.getImageFile());

        // Сохраняем сущность
        Service savedService = serviceRepository.save(service);

        // Преобразуем обратно в DTO и возвращаем
        return serviceMapper.toDto(savedService);
    }

    /**
     * Обновление услуги через форму обновления
     * @param updateForm данные формы обновления
     * @throws IOException если произошла ошибка при обработке изображения
     */
    @Transactional
    public void updateService(ServiceUpdateFormDTO updateForm) throws IOException {
        Service service = findServiceById(updateForm.getId());

        // Обновляем поля через маппер
        serviceMapper.updateServiceFromDto(service, updateForm);

        // Обрабатываем загрузку изображения
        processImageUpload(service, updateForm.getImageFile());

        serviceRepository.save(service);
    }

    /**
     * Удаление услуги
     * @param id идентификатор услуги
     * @throws ResourceNotFoundException если услуга не найдена
     */
    @Transactional
    public void deleteService(Long id) throws ResourceNotFoundException {
        Service service = findServiceById(id);

        // Удаляем изображение, если оно есть
        if (service.getDeleteHash() != null) {
            imgurService.deleteImage(service.getDeleteHash());
        }

        serviceRepository.delete(service);
    }

    /**
     * Изменение статуса активности услуги
     * @param id идентификатор услуги
     * @return DTO обновленной услуги
     * @throws ResourceNotFoundException если услуга не найдена
     */
    @Transactional
    public ServiceDTO toggleServiceActive(Long id) throws ResourceNotFoundException {
        Service service = findServiceById(id);

        service.setActive(!service.isActive());
        Service savedService = serviceRepository.save(service);

        return serviceMapper.toDto(savedService);
    }

    /**
     * Вспомогательный метод для поиска услуги по ID
     * @param id идентификатор услуги
     * @return найденная услуга
     * @throws ResourceNotFoundException если услуга не найдена
     */
    private Service findServiceById(Long id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Услуга с ID " + id + " не найдена"));
    }

    /**
     * Обработка загрузки изображения
     * @param service сущность услуги
     * @param imageFile файл изображения
     * @throws IOException если произошла ошибка при обработке изображения
     */
    private void processImageUpload(Service service, MultipartFile imageFile) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            // Если обновляем существующую услугу и у нее есть изображение, удаляем старое
            if (service.getId() != null && service.getDeleteHash() != null) {
                imgurService.deleteImage(service.getDeleteHash());
            }

            // Загружаем новое изображение
            ImgurService.UploadResult uploadResult = imgurService.uploadImage(imageFile);
            service.setImageUrl(uploadResult.getUrl());
            service.setDeleteHash(uploadResult.getDeleteHash());
        } else if (service.getId() == null && (service.getImageUrl() == null || service.getImageUrl().isEmpty())) {
            // Если создаем новую услугу без изображения
            log.warn("Создание услуги без изображения: {}", service.getTitle());
        }
        // В остальных случаях оставляем текущее изображение без изменений
    }

    /**
     * Получает первые N активных услуг для отображения на главной странице
     * @param limit максимальное количество услуг для отображения
     * @return список DTO карточек услуг, ограниченный указанным количеством
     */
    public List<ServiceCardDTO> getTopActiveServiceCards(int limit) {
        return serviceRepository.findByActiveTrueOrderByDisplayOrderAsc()
                .stream()
                .limit(limit)
                .map(serviceMapper::toCardDto)
                .collect(Collectors.toList());
    }

    /**
     * Получает список всех активных услуг
     * @return список DTO активных услуг
     */
    public List<ServiceDTO> getAllActiveServices() {
        // Получаем все услуги с фильтром по полю active = true
        List<Service> activeServices = serviceRepository.findByActiveTrue();

        // Преобразуем сущности в DTO
        return activeServices.stream()
                .map(serviceMapper::toDto)
                .collect(Collectors.toList());
    }

    public long countActiveServices() {
        return serviceRepository.count();
    }
}