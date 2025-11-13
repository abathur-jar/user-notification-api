package org.example.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
public class RefreshTokenRequest {
    @NotBlank(message = "refreshToken не может быть пустым!")
    private final String refreshToken;
}
