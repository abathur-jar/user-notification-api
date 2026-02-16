package org.example.api.auth;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.example.dto.RegisterRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class BaseTestRest {

    @LocalServerPort
    private int port;

    @BeforeEach
    public void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/auth";
    }

    // для проверки CI
    @Test
    void shouldFail() {
        assertEquals(1, 2);
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

        String loginJson = String.format("""
                {
                    "email": "%s",
                    "password": "%s"
                }
                """, email, password);

        given()
                .contentType(ContentType.JSON)
                .body(loginJson)
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("accessToken", notNullValue())
                .body("refreshToken", notNullValue())
                .body("tokenType", equalTo("Bearer"));
    }

    @Test
    @DisplayName("Login User Not Authorization")
    public void loginUser_isNotAuthorization() {
        String email = "login" + UUID.randomUUID() + "@gmail.com";
        String password = "123456789";

        String jsonRequest = String.format("""
                {
                "email": "%s",
                "password": "%s"
                }
                """, email, password);

        given()
                .contentType(ContentType.JSON)
                .body(jsonRequest)
                .when()
                .post("/login")
                .then()
                .statusCode(401);
    }

}
