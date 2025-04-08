package com.example.artemenko_psychologist.repository;

import com.example.artemenko_psychologist.model.ContentPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentPhotoRepository extends JpaRepository<ContentPhoto, Long> {
}
