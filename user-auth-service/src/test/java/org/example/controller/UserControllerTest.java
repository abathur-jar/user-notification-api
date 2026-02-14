package org.example.controller;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest
class UserControllerTest {

//    @Container
//    static final PostgreSQLContainer<?> postgres =
//            new PostgreSQLContainer<>(DockerImageName.parse("postgres:15"));
//
//
//    @DynamicPropertySource
//    static void configureProperties(DynamicPropertyRegistry registry) {
//        registry.add("spring.datasource.url", postgres::getJdbcUrl);
//        registry.add("spring.datasource.username", postgres::getUsername);
//        registry.add("spring.datasource.password", postgres::getPassword);
//        registry.add("spring.jpa.generate-ddl", () -> true);
//    }
//
//    @Test
//    void containerShouldStart() {
//        Assertions.assertTrue(postgres.isRunning());
//        System.out.println("Контейнер запущен!");
//        System.out.println("JDBC URL " + postgres.getJdbcUrl());
//        System.out.println("Username: " + postgres.getUsername());
//    }
}