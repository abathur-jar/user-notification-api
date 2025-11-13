package org.example.controller;

import jakarta.validation.Valid;
import org.example.dto.LoginRequest;
import org.example.dto.RefreshTokenRequest;
import org.example.dto.RegisterRequest;
import org.example.dto.TokenResponse;
import org.example.service.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private AuthService service;

    public AuthController(AuthService service) {
        this.service = service;
    }

    @PostMapping("/register")
    public String addNewUser(@Valid @RequestBody RegisterRequest request) {
        return service.registerUser(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return service.loginAndGetToken(request.getEmail(), request.getPassword());
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return service.refreshToken(request.getRefreshToken());
    }


    @PostMapping
    public boolean validateToken(@Valid @RequestParam String token) {
        try {
            service.validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
