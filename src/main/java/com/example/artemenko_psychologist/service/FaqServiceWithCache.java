package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.dto.faq.FaqCreateDto;
import com.example.artemenko_psychologist.dto.faq.FaqDto;
import com.example.artemenko_psychologist.dto.faq.FaqUpdateDto;
import com.example.artemenko_psychologist.maper.FaqMapper;
import com.example.artemenko_psychologist.model.Faq;
import com.example.artemenko_psychologist.repository.FaqRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FaqServiceWithCache {
    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;

    // Кэш FAQ в памяти
    private final Map<Long, FaqDto> faqCache = new HashMap<>();

    // Инициализация кэша при старте сервиса
    @PostConstruct
    public void initCache() {
        refreshCache();
    }

    // Обновление всего кэша
    private void refreshCache() {
        faqCache.clear();
        faqRepository.findAll().forEach(faq ->
                faqCache.put(faq.getId(), faqMapper.toDto(faq))
        );
    }

    @Transactional
    public FaqDto createFaq(FaqCreateDto createDto) {
        Faq faq = faqMapper.toEntity(createDto);
        Faq savedFaq = faqRepository.save(faq);
        FaqDto faqDto = faqMapper.toDto(savedFaq);

        // Обновляем кэш
        faqCache.put(faqDto.getId(), faqDto);

        return faqDto;
    }

    @Transactional(readOnly = true)
    public List<FaqDto> getAllFaqs() {
        // Используем кэш вместо обращения к базе
        return faqCache.values().stream()
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FaqDto> getActiveFaqs() {
        // Фильтруем активные из кэша
        return faqCache.values().stream()
                .filter(FaqDto::isActive)
                .sorted((a, b) -> a.getId().compareTo(b.getId()))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FaqDto getFaqById(Long id) {
        // Ищем в кэше
        FaqDto faqDto = faqCache.get(id);
        if (faqDto == null) {
            throw new RuntimeException("FAQ с ID " + id + " не найден");
        }
        return faqDto;
    }

    @Transactional
    public FaqDto updateFaq(Long id, FaqUpdateDto updateDto) {
        Faq existingFaq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ с ID " + id + " не найден"));

        Faq updatedFaq = faqMapper.updateEntityFromDto(updateDto, existingFaq);
        Faq savedFaq = faqRepository.save(updatedFaq);
        FaqDto faqDto = faqMapper.toDto(savedFaq);

        // Обновляем кэш
        faqCache.put(id, faqDto);

        return faqDto;
    }

    @Transactional
    public FaqDto toggleActive(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ с ID " + id + " не найден"));

        faq.setActive(!faq.isActive());
        Faq savedFaq = faqRepository.save(faq);
        FaqDto faqDto = faqMapper.toDto(savedFaq);

        // Обновляем кэш
        faqCache.put(id, faqDto);

        return faqDto;
    }

    @Transactional
    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new RuntimeException("FAQ с ID " + id + " не найден");
        }
        faqRepository.deleteById(id);

        // Удаляем из кэша
        faqCache.remove(id);
    }

    // Получение всех FAQ в виде Map
    public Map<Long, FaqDto> getFaqsAsMap() {
        return new HashMap<>(faqCache);
    }

    // Поиск по вопросу (по подстроке)
    public List<FaqDto> searchFaqsByQuestion(String questionSubstring) {
        return faqCache.values().stream()
                .filter(faq -> faq.getQuestion().toLowerCase().contains(questionSubstring.toLowerCase()))
                .collect(Collectors.toList());
    }

    // Принудительное обновление кэша из базы данных
    public void forceRefreshCache() {
        refreshCache();
    }
}