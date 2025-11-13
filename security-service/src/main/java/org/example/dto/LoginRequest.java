package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
public class LoginRequest {

    @NotBlank(message = "Email не может быть пустым!")
    @Email(message = "Некорректный формат email!")
    private final String email;

    @NotBlank(message = "Пароль не может быть пустым!")
    private final String password;
}
