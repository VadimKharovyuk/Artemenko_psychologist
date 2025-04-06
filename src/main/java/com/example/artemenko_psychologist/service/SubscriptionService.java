package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.dto.subscription.SubscriptionDto;
import com.example.artemenko_psychologist.model.Subscription;
import com.example.artemenko_psychologist.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public boolean subscribe(SubscriptionDto subscriptionDto) {
        // Проверяем, существует ли уже подписка с таким email
        if (subscriptionRepository.existsByEmail(subscriptionDto.getEmail())) {
            return false; // Подписка уже существует
        }
        Subscription subscription = Subscription.builder()
                .email(subscriptionDto.getEmail())
                .build();

        subscriptionRepository.save(subscription);
        return true;
    }

    @Transactional
    public void unsubscribe(String email) {
        subscriptionRepository.findByEmail(email)
                .ifPresent(subscriptionRepository::delete);
    }
}