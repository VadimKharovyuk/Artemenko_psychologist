package com.example.artemenko_psychologist.service.impl;

import com.example.artemenko_psychologist.model.DocumentPhoto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface DocumentPhotoService {


    DocumentPhoto uploadPhoto(MultipartFile file) throws IOException;

    Optional<DocumentPhoto> getPhotoById(Long id);

    List<DocumentPhoto> getAllActivePhotos();


    boolean deletePhoto(Long id);

    DocumentPhoto deactivatePhoto(Long id);


    DocumentPhoto activatePhoto(Long id);

    List<DocumentPhoto> getAllPhotos();

}