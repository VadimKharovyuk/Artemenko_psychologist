package com.example.artemenko_psychologist.service.impl;

import com.example.artemenko_psychologist.dto.faq.FaqCreateDto;
import com.example.artemenko_psychologist.dto.faq.FaqDto;
import com.example.artemenko_psychologist.dto.faq.FaqUpdateDto;

import java.util.List;
import java.util.Map;

public interface FaqService {

    // Создание нового FAQ
    FaqDto createFaq(FaqCreateDto createDto);

    // Получение списка всех FAQ
    List<FaqDto> getAllFaqs();

    // Получение только активных FAQ
    List<FaqDto> getActiveFaqs();

    // Получение FAQ по ID
    FaqDto getFaqById(Long id);

    // Обновление существующего FAQ
    FaqDto updateFaq(Long id, FaqUpdateDto updateDto);

    // Переключение статуса активности FAQ
    FaqDto toggleActive(Long id);

    // Удаление FAQ
    void deleteFaq(Long id);

    // Получение FAQ в виде Map (id -> FAQ)
    Map<Long, FaqDto> getFaqsAsMap();

    // Поиск FAQ по вопросу
    List<FaqDto> searchFaqsByQuestion(String questionSubstring);
}