package com.example.artemenko_psychologist.dto.subscription;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionDto {
    @NotBlank(message = "Email cannot be empty")
    @Email(message = "Invalid email format")
    @Length(max = 255, message = "Email is too long")
    private String email;
}