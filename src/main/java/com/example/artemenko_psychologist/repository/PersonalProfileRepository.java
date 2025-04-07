package com.example.artemenko_psychologist.repository;

import com.example.artemenko_psychologist.model.PersonalProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PersonalProfileRepository extends JpaRepository<PersonalProfile, Long> {
}
