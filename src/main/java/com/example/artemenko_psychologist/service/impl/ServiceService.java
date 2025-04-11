package com.example.artemenko_psychologist.service.impl;

import com.example.artemenko_psychologist.model.DocumentPhoto;
import com.example.artemenko_psychologist.model.Service;
import com.example.artemenko_psychologist.util.CloudinaryService;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


import com.example.artemenko_psychologist.dto.service.ServiceCardDTO;
import com.example.artemenko_psychologist.dto.service.ServiceCreateFormDTO;
import com.example.artemenko_psychologist.dto.service.ServiceDTO;
import com.example.artemenko_psychologist.dto.service.ServiceUpdateFormDTO;
import com.example.artemenko_psychologist.model.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;


public interface ServiceService {

    /**
     * Получение списка всех активных услуг
     * @return список DTO услуг
     *
     */
    List<ServiceDTO> getAllActiveServices();

    /**
     * Получение списка всех услуг
     * @return список DTO услуг
     */
    List<ServiceDTO> getAllServices();

    /**
     * Получение списка карточек всех активных услуг
     * @return список DTO карточек услуг
     */
    List<ServiceCardDTO> getAllActiveServiceCards();

    /**
     * Получение услуги по идентификатору
     * @param id идентификатор услуги
     * @return DTO услуги
     */
    ServiceDTO getServiceById(Long id);

    /**
     * Создание новой услуги
     * @param formDTO данные для создания услуги
     * @param imageFile файл изображения (может быть null)
     * @return созданная услуга
     * @throws IOException при ошибке загрузки изображения
     */
    ServiceDTO createService(ServiceCreateFormDTO formDTO, MultipartFile imageFile) throws IOException;

    /**
     * Обновление существующей услуги
     * @param formDTO данные для обновления услуги
     * @param imageFile новый файл изображения (может быть null)
     * @return обновленная услуга
     * @throws IOException при ошибке загрузки изображения
     */
    ServiceDTO updateService(ServiceUpdateFormDTO formDTO, MultipartFile imageFile) throws IOException;

    /**
     * Удаление услуги по идентификатору
     * @param id идентификатор услуги
     */
    void deleteService(Long id);

    /**
     * Изменение статуса активности услуги
     * @param id идентификатор услуги
     * @param active новый статус активности
     * @return обновленная услуга
     */
    ServiceDTO toggleServiceActive(Long id);

    /**
     * Загрузка изображения для услуги
     * @param file файл изображения
     * @return URL загруженного изображения и его publicId
     * @throws IOException при ошибке загрузки изображения
     */
    CloudinaryService.UploadResult uploadServiceImage(MultipartFile file) throws IOException;

    /**
     * Удаление изображения услуги
     * @param publicId идентификатор изображения в Cloudinary
     * @return успешно ли выполнено удаление
     */
    boolean deleteServiceImage(String publicId);


    ServiceUpdateFormDTO getServiceUpdateFormById(Long id);

    ServiceDTO createOrUpdateService(@Valid ServiceCreateFormDTO serviceForm);


    List<ServiceCardDTO> getTopActiveServiceCards(int i);


   long countActiveServices();

    List<ServiceDTO> getLatestServices(int i);

}