package org.example.integration.api;

import org.example.dto.AuthResponse;
import org.example.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean // ← ВМЕСТО @MockBean в Spring Boot 3.3+
    AuthService authService;

    @Nested
    @DisplayName("REGISTRATION TESTS")
    class RegistrationTests {

        @Test
        @DisplayName("Success registration")
        void registerUser_Test() throws Exception {
            // given

            when(authService.registerUser(any())).thenReturn("User created");

            String jsonRequest = """
                {
                "email": "test@mail.com",
                "password": "Password123",
                "firstName": "Test",
                "lastName": "User",
                "phone": "+79991234567"
                }
                """;

            // when
            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Registration with empty email")
        void registerUser_InvalidEmail_ReturnsBadRequest() throws Exception {
            // given
            String jsonRequest = """
                {
                "password": "Password123",
                "firstName": "Test",
                "lastName": "User",
                "phone": "+79991234567"
                }
                """;

            mockMvc.perform(post("/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("LOGIN TESTS")
    class LoginTests {

        @Test
        @DisplayName("Valid login return JWT tokens")
        void loginUser_Valid_ReturnsJwtTokens() throws Exception {
            //given
            AuthResponse mockResponse = new AuthResponse(
                    "access-token-123",
                    "refresh-token-456",
                    "Bearer"
            );

            when(authService.loginUser("test@mail.com", "Password123"))
                    .thenReturn(mockResponse);

            String jsonRequest = """
            {
            "email": "test@mail.com",
            "password": "Password123"
            }
            """;

            // when
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").value("access-token-123"))
                    .andExpect(jsonPath("$.refreshToken").value("refresh-token-456"))
                    .andExpect(jsonPath("$.tokenType").value("Bearer"));
        }

        @Test
        @DisplayName("Invalid password returns 401")
        void loginUser_InvalidPassword_ReturnsUnauthorized() throws Exception {

            when(authService.loginUser("test@mail.com", "wrong"))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            String jsonRequest = """
            {
            "email": "test@mail.com",
            "password": "wrong"
            }
            """;

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpect(status().is4xxClientError());
        }
    }

}
