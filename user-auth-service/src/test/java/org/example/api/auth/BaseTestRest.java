package org.example.api.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.example.dto.AuthResponse;
import org.example.dto.LoginRequest;
import org.example.dto.RegisterRequest;
import org.example.service.UserEventProducer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
properties = "spring.cloud.config.enabled=false")
@ActiveProfiles("test")
public class BaseTestRest {

    @MockitoBean
    protected UserEventProducer userEventProducer;

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/auth";
    }

    @Test
     void register_Sends_EventsInKafka() {

        String email = "user" + UUID.randomUUID() + "@example.com";

        RegisterRequest request = new RegisterRequest(
                "Ivan",
                "Ivanov",
                email,
                "123456789",
                "+79998887766");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/register")
                .then()
                .statusCode(200);

        verify(userEventProducer, times(1)).sendUserEvent(any());
     }

    @Test
    @DisplayName("Success register user!")
    public void registerUser_Success() {

        String email = "user" + UUID.randomUUID() + "@example.com";

        RegisterRequest request = new RegisterRequest(
                "Ivan",
                "Ivanov",
                email,
                "123456789",
                "+79998887766");

        given()
                .contentType(ContentType.JSON)
                .body(request)
        .when()
                .post("/register")
        .then()
                .statusCode(200)
                .body(equalTo("Пользователь Ivan Ivanov, с email: " + email + " был успешно добавлен!"));
    }

    @Test
    @DisplayName("500 Internal Error User Already Exists! Duplicate")
    public void registerUser_AlreadyExists() {

        String email = "duplicate." + UUID.randomUUID() + "@example.com";

        final RegisterRequest request = new RegisterRequest(
                "Petr",
                "Petrov",
                email,
                "123456",
                "+79991112233");

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/register")
                .then().statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .body(request)
                .when()
                .post("/register")
                .then().statusCode(500);
    }

    @Test
    @DisplayName("Success Authorization Login")
    public void loginUser_Success() {

        String email = "login." + UUID.randomUUID() + "@example.com";
        String password = "Password123!";

        RegisterRequest registerRequest = new RegisterRequest(
                "Test",
                "User",
                email,
                password,
                "+79991112233"
        );

        given()
                .contentType(ContentType.JSON)
                .body(registerRequest)
                .when()
                        .post("/register")
                                .then()
                                        .statusCode(200);

        LoginRequest loginRequest = new LoginRequest(email, password);

        final AuthResponse response = given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .extract()
                .as(AuthResponse.class);

        Assertions.assertNotNull(response.accessToken());
        Assertions.assertNotNull(response.refreshToken());
        Assertions.assertNotNull(response.tokenType());
        Assertions.assertEquals("Bearer", response.tokenType());
    }

    @Test
    @DisplayName("Login User Not Authorization")
    public void loginUser_isNotAuthorization() {

        String email = "login" + UUID.randomUUID() + "@gmail.com";
        String password = "123456789";

        LoginRequest loginRequest = new LoginRequest(email, password);

        given()
                .contentType(ContentType.JSON)
                .body(loginRequest)
                .when()
                .post("/login")
                .then()
                .statusCode(401);
    }

}
