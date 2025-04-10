package com.example.artemenko_psychologist.service.impl;

import com.example.artemenko_psychologist.dto.reviews.ReviewCreateDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewDetailsDTO;
import com.example.artemenko_psychologist.dto.reviews.ReviewListDTO;
import com.example.artemenko_psychologist.model.DocumentPhoto;
import com.example.artemenko_psychologist.model.Review;
import jakarta.validation.Valid;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;


public interface ReviewPhotoService {
    Review uploadPhoto(MultipartFile file) throws IOException;

    ReviewDetailsDTO getReviewById(Long id);


    boolean deletePhoto(Long id);

    List<ReviewListDTO> getAllPhotos();

    List<ReviewListDTO> getLatestReviews(int limit);

    List<ReviewListDTO> getReviewsByServiceId(Long serviceId);

    long countReviewsByServiceId();


    ReviewDetailsDTO createReview(@Valid ReviewCreateDTO reviewDTO, MultipartFile image);


    void deleteReview(Long id);

}
