package com.example.artemenko_psychologist.repository;

import com.example.artemenko_psychologist.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    // Проверка существования подписки по email
    boolean existsByEmail(String email);

    // Найти подписку по email
    Optional<Subscription> findByEmail(String email);
}