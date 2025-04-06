package com.example.artemenko_psychologist.repository;

import com.example.artemenko_psychologist.model.BlogCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogCategoryRepository extends JpaRepository<BlogCategory, Long> {
    // Можно добавить пользовательские методы запросов, если необходимо
    BlogCategory findByName(String name);
    boolean existsByName(String name);
}
