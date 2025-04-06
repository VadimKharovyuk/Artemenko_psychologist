package com.example.artemenko_psychologist.dto.blog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlogPostUpdateDto {
    private String title;
    private String previewImageUrl;
    private String deleteHash; // Оставляем этот параметр
    private String shortDescription;
    private String content;
    private Long categoryId;
    private MultipartFile imageFile;
}