package com.example.artemenko_psychologist.dto.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceCardDTO {
    private Long id;
    private String title;
    private String shortDescription;
    private String iconClass;
}