package com.example.artemenko_psychologist.repository;

import com.example.artemenko_psychologist.model.Service;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ServiceRepository  extends JpaRepository<Service, Long> {
    List<Service> findByActiveTrueOrderByDisplayOrderAsc();

    List<Service> findByActiveTrue();

}
