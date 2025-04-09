package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.dto.faq.FaqCreateDto;
import com.example.artemenko_psychologist.dto.faq.FaqDto;
import com.example.artemenko_psychologist.dto.faq.FaqUpdateDto;
import com.example.artemenko_psychologist.maper.FaqMapper;
import com.example.artemenko_psychologist.model.Faq;
import com.example.artemenko_psychologist.repository.FaqRepository;

import com.example.artemenko_psychologist.service.impl.FaqService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Primary // Указывает, что эта реализация будет использоваться по умолчанию
@RequiredArgsConstructor
public class FaqServiceStandardImpl implements FaqService {
    private final FaqRepository faqRepository;
    private final FaqMapper faqMapper;

    @Override
    @Transactional
    public FaqDto createFaq(FaqCreateDto createDto) {
        Faq faq = faqMapper.toEntity(createDto);
        Faq savedFaq = faqRepository.save(faq);
        return faqMapper.toDto(savedFaq);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqDto> getAllFaqs() {
        List<Faq> faqs = faqRepository.findAllByOrderByIdAsc();
        return faqMapper.toDtoList(faqs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqDto> getActiveFaqs() {
        List<Faq> activeFaqs = faqRepository.findByActiveIsTrueOrderByIdAsc();
        return faqMapper.toDtoList(activeFaqs);
    }

    @Override
    @Transactional(readOnly = true)
    public FaqDto getFaqById(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ с ID " + id + " не найден"));
        return faqMapper.toDto(faq);
    }

    @Override
    @Transactional
    public FaqDto updateFaq(Long id, FaqUpdateDto updateDto) {
        Faq existingFaq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ с ID " + id + " не найден"));

        Faq updatedFaq = faqMapper.updateEntityFromDto(updateDto, existingFaq);
        Faq savedFaq = faqRepository.save(updatedFaq);

        return faqMapper.toDto(savedFaq);
    }

    @Override
    @Transactional
    public FaqDto toggleActive(Long id) {
        Faq faq = faqRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("FAQ с ID " + id + " не найден"));

        faq.setActive(!faq.isActive());
        Faq savedFaq = faqRepository.save(faq);

        return faqMapper.toDto(savedFaq);
    }

    @Override
    @Transactional
    public void deleteFaq(Long id) {
        if (!faqRepository.existsById(id)) {
            throw new RuntimeException("FAQ с ID " + id + " не найден");
        }
        faqRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, FaqDto> getFaqsAsMap() {
        return faqRepository.findAll().stream()
                .map(faqMapper::toDto)
                .collect(Collectors.toMap(FaqDto::getId, Function.identity()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FaqDto> searchFaqsByQuestion(String questionSubstring) {
        return faqRepository.findAll().stream()
                .filter(faq -> faq.getQuestion().toLowerCase().contains(questionSubstring.toLowerCase()))
                .map(faqMapper::toDto)
                .collect(Collectors.toList());
    }
}