package org.example.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Запрос на изменение телефона")
public class PhoneUpdateUser {

    @Schema(description = "Новый номер телефона", example = "89851119988")
    @NotBlank(message = "Телефон обязателен")
    private String phone;
}
