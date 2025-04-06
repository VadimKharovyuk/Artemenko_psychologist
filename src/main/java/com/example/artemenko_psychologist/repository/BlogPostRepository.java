package com.example.artemenko_psychologist.repository;

import com.example.artemenko_psychologist.model.BlogPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {
    // Найти посты по категории
    List<BlogPost> findByCategoryId(Long categoryId);

    // Найти последние N опубликованных постов
    @Query("SELECT bp FROM BlogPost bp ORDER BY bp.publicationDate DESC")
    List<BlogPost> findLatestPosts(org.springframework.data.domain.Pageable pageable);
}