package com.example.artemenko_psychologist.repository;

import com.example.artemenko_psychologist.model.DocumentPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentPhotoRepository extends JpaRepository<DocumentPhoto, Long> {
    List<DocumentPhoto> findByIsActiveTrue();
}
