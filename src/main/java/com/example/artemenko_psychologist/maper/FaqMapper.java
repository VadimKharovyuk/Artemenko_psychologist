package com.example.artemenko_psychologist.maper;

import com.example.artemenko_psychologist.dto.faq.FaqCreateDto;
import com.example.artemenko_psychologist.dto.faq.FaqDto;
import com.example.artemenko_psychologist.dto.faq.FaqUpdateDto;
import com.example.artemenko_psychologist.model.Faq;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class FaqMapper {

    // Преобразование Entity в DTO
    public FaqDto toDto(Faq faq) {
        return FaqDto.builder()
                .id(faq.getId())
                .question(faq.getQuestion())
                .answer(faq.getAnswer())
                .active(faq.isActive())
                .createdAt(faq.getCreatedAt())
                .updatedAt(faq.getUpdatedAt())
                .build();
    }

    // Преобразование CreateDto в Entity
    public Faq toEntity(FaqCreateDto createDto) {
        return Faq.builder()
                .question(createDto.getQuestion())
                .answer(createDto.getAnswer())
                .active(createDto.isActive())
                .build();
    }

    // Обновление Entity из UpdateDto
    public Faq updateEntityFromDto(FaqUpdateDto updateDto, Faq existingFaq) {
        existingFaq.setQuestion(updateDto.getQuestion());
        existingFaq.setAnswer(updateDto.getAnswer());
        existingFaq.setActive(updateDto.isActive());
        return existingFaq;
    }

    // Преобразование списка Entity в список DTO
    public List<FaqDto> toDtoList(List<Faq> faqs) {
        return faqs.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}