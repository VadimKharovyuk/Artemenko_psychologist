package com.example.artemenko_psychologist.model;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class PersonalProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Column(name = "personal_history", length = 1000)
    private String personalHistory;

    @Column(name = "professional_journey", length = 1000)
    private String professionalJourney;

    @Column(name = "personal_reflections", length = 1000)
    private String personalReflections;

    @Column(name = "photo_delete_hash")
    private String photoDeleteHash;



}