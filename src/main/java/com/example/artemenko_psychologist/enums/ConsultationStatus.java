package com.example.artemenko_psychologist.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ConsultationStatus {
    NEW("Нова"),
    CONTACTED("Зв'язались"),
    SCHEDULED("Заплановано"),
    COMPLETED("Завершено"),
    CANCELLED("Скасовано");

    private final String displayName;


}
