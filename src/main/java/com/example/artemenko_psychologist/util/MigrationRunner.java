package com.example.artemenko_psychologist.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class MigrationRunner implements CommandLineRunner {

    private final PhotoMigrationService photoMigrationService;

    @Value("${app.run-photo-migration:false}")
    private boolean runPhotoMigration;

    @Autowired
    public MigrationRunner(PhotoMigrationService photoMigrationService) {
        this.photoMigrationService = photoMigrationService;
    }

    @Override
    public void run(String... args) {
        if (runPhotoMigration) {
            photoMigrationService.runMigration();
        }
    }
}