package org.example.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
public class RegisterRequest {

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 50, message = "Имя должно быть от 2 до 50 символов")
    private final String firstName;

    @NotBlank(message = "Фамилия не может быть пустой")
    @Size(min = 2, max = 50, message = "Фамилия должна быть от 2 до 50 символов")
    private final String lastName;

    @NotBlank(message = "Email не может быть пустым!")
    @Email(message = "Некорректный формат email!")
    private final String email;

    @NotBlank(message = "Пароль не может быть пустым!")
    @Size(min = 6, message = "Пароль содержать не менее 6 символов")
    private final String password;
}
