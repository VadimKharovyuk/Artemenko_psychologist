package com.example.artemenko_psychologist.repository;

import com.example.artemenko_psychologist.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    // Проверка существования подписки по email
    boolean existsByEmail(String email);

    // Найти подписку по email
    Optional<Subscription> findByEmail(String email);

    // Найти все активные email для рассылки
    @Query("SELECT s.email FROM Subscription s")
    List<String> findAllSubscriberEmails();

    // Подсчет количества подписчиков
    long count();
}