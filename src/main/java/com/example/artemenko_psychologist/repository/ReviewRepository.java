package com.example.artemenko_psychologist.repository;


import com.example.artemenko_psychologist.model.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * Находит все отзывы для конкретной услуги
     */
    List<Review> findByServiceId(Long serviceId);

    /**
     * Находит отзывы для конкретной услуги с сортировкой
     */
    List<Review> findByServiceId(Long serviceId, Sort sort);

    /**
     * Находит все отзывы, созданные пользователем с определенным именем
     */
    List<Review> findByUsername(String username);

    long countByServiceId(Long serviceId);

}