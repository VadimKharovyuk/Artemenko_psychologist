package com.example.artemenko_psychologist.repository;

import com.example.artemenko_psychologist.model.Faq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FaqRepository extends JpaRepository<Faq, Long> {

    List<Faq> findByActiveIsTrueOrderByIdAsc();

    List<Faq> findAllByOrderByIdAsc();
}
