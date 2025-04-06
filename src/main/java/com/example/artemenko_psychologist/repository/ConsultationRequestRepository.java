package com.example.artemenko_psychologist.repository;

import com.example.artemenko_psychologist.enums.ConsultationStatus;
import com.example.artemenko_psychologist.model.ConsultationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRequestRepository extends JpaRepository<ConsultationRequest, Long> {
    Page<ConsultationRequest> findByStatus(ConsultationStatus status, Pageable pageable);

    long countByStatus(ConsultationStatus consultationStatus);
}
