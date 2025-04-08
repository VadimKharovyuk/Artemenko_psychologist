package com.example.artemenko_psychologist.service;

import com.example.artemenko_psychologist.model.DocumentPhoto;
import com.example.artemenko_psychologist.repository.DocumentPhotoRepository;

import com.example.artemenko_psychologist.service.impl.DocumentPhotoService;
import com.example.artemenko_psychologist.util.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentPhotoServiceImpl implements DocumentPhotoService {

    private final DocumentPhotoRepository documentPhotoRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public DocumentPhoto uploadPhoto(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Файл не может быть пустым");
        }

        log.info("Начинаем загрузку фотографии: {}", file.getOriginalFilename());

        try {
            // Загружаем файл в Cloudinary
            CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadImage(file);

            // Создаем и сохраняем запись о фотографии
            DocumentPhoto documentPhoto = new DocumentPhoto();
            documentPhoto.setPhotoUrl(uploadResult.getUrl());
            documentPhoto.setPublicId(uploadResult.getPublicId());
            documentPhoto.setActive(true);

            DocumentPhoto savedPhoto = documentPhotoRepository.save(documentPhoto);

            return savedPhoto;
        } catch (IOException e) {
            log.error("Ошибка при загрузке фотографии: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Непредвиденная ошибка при работе с фотографией: {}", e.getMessage());
            throw new RuntimeException("Ошибка при обработке фотографии", e);
        }
    }

    @Override
    public Optional<DocumentPhoto> getPhotoById(Long id) {
        return documentPhotoRepository.findById(id);
    }

    @Override
    public List<DocumentPhoto> getAllActivePhotos() {
        return documentPhotoRepository.findByIsActiveTrue();
    }

    @Override
    public List<DocumentPhoto> getAllPhotos() {
        return documentPhotoRepository.findAll();
    }

    @Override
    @Transactional
    public boolean deletePhoto(Long id) {

        Optional<DocumentPhoto> photoOptional = documentPhotoRepository.findById(id);
        if (photoOptional.isEmpty()) {
            log.warn("Фотография с ID {} не найдена", id);
            return false;
        }

        DocumentPhoto photo = photoOptional.get();

        try {
            // Удаляем файл из Cloudinary
            boolean cloudinarySuccess = cloudinaryService.deleteImage(photo.getPublicId());

            if (!cloudinarySuccess) {
                log.warn("Не удалось удалить файл из Cloudinary: {}", photo.getPublicId());
            }

            // Удаляем запись из базы данных
            documentPhotoRepository.delete(photo);
            return true;
        } catch (Exception e) {
            log.error("Ошибка при удалении фотографии с ID {}: {}", id, e.getMessage());
            return false;
        }
    }

    @Override
    @Transactional
    public DocumentPhoto deactivatePhoto(Long id) {
        DocumentPhoto photo = documentPhotoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Фотография с ID " + id + " не найдена"));

        photo.setActive(false);
        DocumentPhoto savedPhoto = documentPhotoRepository.save(photo);

        return savedPhoto;
    }

    @Override
    @Transactional
    public DocumentPhoto activatePhoto(Long id) {
        DocumentPhoto photo = documentPhotoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Фотография с ID " + id + " не найдена"));
        photo.setActive(true);
        DocumentPhoto savedPhoto = documentPhotoRepository.save(photo);
        return savedPhoto;
    }
}