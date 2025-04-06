package com.example.artemenko_psychologist.dto.blog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlogPostCreateDto {
    @NotBlank
    @Size(min = 1, max = 30)
    private String title;
    private String previewImageUrl;
    private String deleteHash;
    @NotBlank
    @Size(min = 1, max = 30)
    private String shortDescription;
    @NotBlank
    @Size(min = 1, max = 4000)
    private String content;
    @NotBlank
    private Long categoryId;

    private MultipartFile imageFile;
}