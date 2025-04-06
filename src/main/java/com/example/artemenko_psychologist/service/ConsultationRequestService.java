package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.dto.consultation.ConsultationRequestCreateDto;
import com.example.artemenko_psychologist.dto.consultation.ConsultationRequestDetailsDto;
import com.example.artemenko_psychologist.dto.consultation.ConsultationRequestListDto;
import com.example.artemenko_psychologist.enums.ConsultationStatus;
import com.example.artemenko_psychologist.maper.ConsultationRequestMapper;
import com.example.artemenko_psychologist.model.ConsultationRequest;
import com.example.artemenko_psychologist.repository.ConsultationRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsultationRequestService {

    private final ConsultationRequestRepository consultationRequestRepository;
    private final ConsultationRequestMapper consultationRequestMapper;
    private final EmailQueueService emailQueueService;

    /**
     * Создает новый запрос на консультацию
     * @param dto данные запроса
     * @return созданный запрос
     */
    @Transactional
    public ConsultationRequestDetailsDto createRequest(ConsultationRequestCreateDto dto) {
        ConsultationRequest request = consultationRequestMapper.toEntity(dto);
        request.setStatus(ConsultationStatus.NEW);

        ConsultationRequest savedRequest = consultationRequestRepository.save(request);

        // Отправляем уведомление о новом запросе
        sendNewRequestNotification(savedRequest);

        log.info("Создан новый запрос на консультацию ID: {}, клиент: {}",
                savedRequest.getId(), savedRequest.getClientName());

        return consultationRequestMapper.toDetailsDto(savedRequest);
    }

    /**
     * Получает запрос по ID
     * @param id ID запроса
     * @return детальное DTO запроса
     */
    @Transactional(readOnly = true)
    public ConsultationRequestDetailsDto getRequestById(Long id) {
        ConsultationRequest request = findRequestById(id);
        return consultationRequestMapper.toDetailsDto(request);
    }

    /**
     * Обновляет запрос
     * @param id ID запроса
     * @param dto обновленные данные
     * @return обновленный запрос
     */
    @Transactional
    public ConsultationRequestDetailsDto updateRequest(Long id, ConsultationRequestDetailsDto dto) {
        ConsultationRequest request = findRequestById(id);

        // Сохраняем предыдущий статус для проверки изменений
        ConsultationStatus previousStatus = request.getStatus();

        // Обновляем поля
        consultationRequestMapper.updateEntityFromDto(request, dto);

        ConsultationRequest updatedRequest = consultationRequestRepository.save(request);

//        // Если статус изменился, отправляем уведомление
//        if (previousStatus != updatedRequest.getStatus()) {
//            sendStatusChangeNotification(updatedRequest, previousStatus);
//        }

        log.info("Обновлен запрос на консультацию ID: {}, новый статус: {}",
                updatedRequest.getId(), updatedRequest.getStatus());

        return consultationRequestMapper.toDetailsDto(updatedRequest);
    }

    /**
     * Изменяет статус запроса
     * @param id ID запроса
     * @param status новый статус
     * @return обновленный запрос
     */
    @Transactional
    public ConsultationRequestDetailsDto updateStatus(Long id, ConsultationStatus status) {
        ConsultationRequest request = findRequestById(id);

        ConsultationStatus previousStatus = request.getStatus();
        request.setStatusWithTimestamp(status);

        ConsultationRequest updatedRequest = consultationRequestRepository.save(request);

        // Отправляем уведомление о смене статуса
//        sendStatusChangeNotification(updatedRequest, previousStatus);

        log.info("Изменен статус запроса ID: {} с {} на {}",
                id, previousStatus, status);

        return consultationRequestMapper.toDetailsDto(updatedRequest);
    }

    /**
     * Получает список всех запросов
     * @return список DTO запросов
     */
    @Transactional(readOnly = true)
    public List<ConsultationRequestListDto> getAllRequests() {
        List<ConsultationRequest> requests = consultationRequestRepository.findAll();
        return consultationRequestMapper.toListDto(requests);
    }

    /**
     * Получает страницу запросов с пагинацией
     * @param pageable параметры пагинации
     * @return страница DTO запросов
     */
    @Transactional(readOnly = true)
    public Page<ConsultationRequestListDto> getRequestsPage(Pageable pageable) {
        Page<ConsultationRequest> requestsPage = consultationRequestRepository.findAll(pageable);
        return requestsPage.map(consultationRequestMapper::toListDto);
    }

    /**
     * Получает страницу запросов по статусу
     * @param status статус запросов
     * @param pageable параметры пагинации
     * @return страница DTO запросов
     */
    @Transactional(readOnly = true)
    public Page<ConsultationRequestListDto> getRequestsByStatus(ConsultationStatus status, Pageable pageable) {
        Page<ConsultationRequest> requestsPage = consultationRequestRepository.findByStatus(status, pageable);
        return requestsPage.map(consultationRequestMapper::toListDto);
    }

    /**
     * Получает количество новых запросов
     * @return количество новых запросов
     */
    @Transactional(readOnly = true)
    public long countNewRequests() {
        return consultationRequestRepository.countByStatus(ConsultationStatus.NEW);
    }

    /**
     * Удаляет запрос
     * @param id ID запроса
     */
    @Transactional
    public void deleteRequest(Long id) {
        ConsultationRequest request = findRequestById(id);
        consultationRequestRepository.delete(request);
        log.info("Удален запрос на консультацию ID: {}", id);
    }

    /**
     * Находит запрос по ID или выбрасывает исключение
     * @param id ID запроса
     * @return найденный запрос
     * @throws NoSuchElementException если запрос не найден
     */
    private ConsultationRequest findRequestById(Long id) {
        return consultationRequestRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Запрос на консультацию с ID " + id + " не найден"));
    }

    /**
     * Отправляет уведомление о новом запросе
     * @param request новый запрос
     */
    private void sendNewRequestNotification(ConsultationRequest request) {
        // Здесь можно реализовать отправку email администратору о новом запросе
        try {
            String serviceName = request.getService() != null ? request.getService().getTitle() : "Не указано";

            // Подготавливаем контекст для письма
            java.util.Map<String, Object> context = new java.util.HashMap<>();
            context.put("requestId", request.getId());
            context.put("clientName", request.getClientName());
            context.put("phoneNumber", request.getPhoneNumber());
            context.put("serviceName", serviceName);
            context.put("message", request.getMessage());
            context.put("createdAt", request.getCreatedAt());

            // Отправляем уведомление администратору
            emailQueueService.queueEmail(
                    "vadimkh17@gmail.com", // Замените на ваш email
                    "Новый запрос на консультацию от " + request.getClientName(),
                    "admin-notification-template",
                    context
            );

            log.info("Отправлено уведомление о новом запросе ID: {}", request.getId());
        } catch (Exception e) {
            log.error("Ошибка при отправке уведомления о запросе: ", e);
        }
    }

//    /**
//     * Отправляет уведомление о смене статуса запроса
//     * @param request запрос с обновленным статусом
//     * @param previousStatus предыдущий статус
//     */
//    private void sendStatusChangeNotification(ConsultationRequest request, ConsultationStatus previousStatus) {
//        // Проверяем, есть ли email клиента
//        if (request.getClientEmail() != null && !request.getClientEmail().isEmpty() &&
//                (request.getStatus() == ConsultationStatus.CONTACTED ||
//                        request.getStatus() == ConsultationStatus.SCHEDULED)) {
//            try {
//                // Подготавливаем контекст для письма
//                java.util.Map<String, Object> context = new java.util.HashMap<>();
//                context.put("clientName", request.getClientName());
//                context.put("status", request.getStatus().getDisplayName());
//                context.put("serviceName", request.getService() != null ? request.getService().getTitle() : "консультация");
//
//                // Отправляем уведомление клиенту
//                emailQueueService.queueEmail(
//                        request.getClientEmail(), // Используем email клиента из запроса
//                        "Обновление статуса вашей заявки на консультацию",
//                        "client-status-update-template",
//                        context
//                );
//
//                log.info("Отправлено уведомление клиенту на email {} о смене статуса запроса ID: {}",
//                        request.getClientEmail(), request.getId());
//            } catch (Exception e) {
//                log.error("Ошибка при отправке уведомления о смене статуса: ", e);
//            }
//        }
//    }
}