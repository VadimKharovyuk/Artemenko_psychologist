package com.example.artemenko_psychologist.service;
import com.example.artemenko_psychologist.model.PersonalProfile;
import com.example.artemenko_psychologist.repository.PersonalProfileRepository;
import com.example.artemenko_psychologist.util.ImgurService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PersonalProfileService {

    private final PersonalProfileRepository profileRepository;
    private final ImgurService imgurService;

    /**
     * Создание нового профиля с загрузкой фото
     *
     * @param fullName            полное имя
     * @param personalHistory     личная история
     * @param professionalJourney профессиональный путь
     * @param personalReflections личные размышления
     * @param profilePhoto        файл фотографии
     * @return созданный профиль
     * @throws IOException если не удалось загрузить фото
     */
    public PersonalProfile createProfile(
            String fullName,
            String personalHistory,
            String professionalJourney,
            String personalReflections,
            MultipartFile profilePhoto) throws IOException {

        // Загрузка фото в Imgur
        ImgurService.UploadResult uploadResult = null;
        String photoUrl = null;
        String deleteHash = null;

        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            uploadResult = imgurService.uploadImage(profilePhoto);
            photoUrl = uploadResult.getUrl();
            deleteHash = uploadResult.getDeleteHash();
        }

        // Создание профиля с использованием Builder
        PersonalProfile profile = PersonalProfile.builder()
                .fullName(fullName)
                .profilePhotoUrl(photoUrl)
                .photoDeleteHash(deleteHash)
                .personalHistory(personalHistory)
                .professionalJourney(professionalJourney)
                .personalReflections(personalReflections)
                .build();

        // Сохранение профиля
        return profileRepository.save(profile);
    }

    /**
     * Обновление существующего профиля
     *
     * @param id                  ID профиля для обновления
     * @param fullName            новое полное имя
     * @param personalHistory     новая личная история
     * @param professionalJourney новый профессиональный путь
     * @param personalReflections новые личные размышления
     * @param profilePhoto        новое фото (опционально)
     * @return обновленный профиль
     * @throws IOException если не удалось загрузить фото
     */
    public PersonalProfile updateProfile(
            Long id,
            String fullName,
            String personalHistory,
            String professionalJourney,
            String personalReflections,
            MultipartFile profilePhoto) throws IOException {

        // Находим существующий профиль
        PersonalProfile existingProfile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Профиль не найден"));

        // Удаляем старое фото, если есть
        if (existingProfile.getProfilePhotoUrl() != null && existingProfile.getPhotoDeleteHash() != null) {
            imgurService.deleteImage(existingProfile.getPhotoDeleteHash());
        }

        // Загрузка нового фото
        ImgurService.UploadResult uploadResult = null;
        String photoUrl = null;
        String deleteHash = null;

        if (profilePhoto != null && !profilePhoto.isEmpty()) {
            uploadResult = imgurService.uploadImage(profilePhoto);
            photoUrl = uploadResult.getUrl();
            deleteHash = uploadResult.getDeleteHash();
        }

        // Обновляем данные профиля с использованием Builder
        PersonalProfile updatedProfile = PersonalProfile.builder()
                .id(existingProfile.getId())
                .fullName(fullName)
                .profilePhotoUrl(photoUrl)
                .photoDeleteHash(deleteHash)
                .personalHistory(personalHistory)
                .professionalJourney(professionalJourney)
                .personalReflections(personalReflections)
                .build();

        return profileRepository.save(updatedProfile);
    }

    /**
     * Удаление профиля по ID
     *
     * @param id ID профиля для удаления
     */
    public void deleteProfile(Long id) {
        PersonalProfile profile = profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Профиль не найден"));

        // Удаляем фото из Imgur, если оно есть
        if (profile.getProfilePhotoUrl() != null && profile.getPhotoDeleteHash() != null) {
            imgurService.deleteImage(profile.getPhotoDeleteHash());
        }

        profileRepository.deleteById(id);
    }

    /**
     * Получение профиля по ID
     *
     * @param id ID профиля
     * @return профиль
     */
    public PersonalProfile getProfileById(Long id) {
        return profileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Профиль не найден"));
    }

    /**
     * Получение всех профилей
     *
     * @return список всех профилей
     */
    public List<PersonalProfile> getAllProfiles() {
        return profileRepository.findAll();
    }
}