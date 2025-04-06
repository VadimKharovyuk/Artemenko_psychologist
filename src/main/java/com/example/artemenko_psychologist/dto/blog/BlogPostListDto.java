package com.example.artemenko_psychologist.dto.blog;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BlogPostListDto {
    private Long id;
    private String title;
    private String previewImageUrl;
    private String shortDescription;
    private String categoryName;
    private LocalDateTime publicationDate;
}
