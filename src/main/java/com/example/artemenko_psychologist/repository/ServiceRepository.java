package com.example.artemenko_psychologist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository  extends JpaRepository<com.example.artemenko_psychologist.entity.Service, Long> {
    List<com.example.artemenko_psychologist.entity.Service> findByActiveTrueOrderByDisplayOrderAsc();

}
