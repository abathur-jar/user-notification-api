package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.RegisterRequest;
import org.example.dto.TokenResponse;
import org.example.entity.SecurityUser;
import org.example.exception.InvalidTokenException;
import org.example.exception.TokenAlreadyUsedException;
import org.example.exception.UnauthorizedException;
import org.example.exception.UserAlreadyExistsException;
import org.example.repository.SecurityRepository;
import org.example.util.JwtUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



@Service
@RequiredArgsConstructor
public class AuthService {

    private final SecurityRepository securityRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;
    private final TokenBlackListService tokenBlackListService;

    public String registerUser(RegisterRequest request) {
        if (securityRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Пользователь с таким email уже существует!");
        }

        SecurityUser user = new SecurityUser();
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        securityRepository.save(user);

        return "Пользователь " + request.getFirstName() + " " + request.getLastName() + " успешно зарегистрирован!";
    }

    public TokenResponse loginAndGetToken(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        final String accessToken = jwtUtils.generateJwtToken(email);
        final String refreshToken = jwtUtils.generateRefreshToken(email);

        return new TokenResponse(accessToken, refreshToken);

    }

    public TokenResponse refreshToken(String refreshToken) {
        try {
            jwtUtils.validateToken(refreshToken);
        } catch (Exception e) {
            throw new InvalidTokenException("Невалидный refresh token: " + e.getMessage());
        }

        final String email = jwtUtils.getEmailFromToken(refreshToken);



        if (!tokenBlackListService.revokedIfNotRevoked(refreshToken, email, "refresh")) {
            throw new TokenAlreadyUsedException("Refresh token уже использовался!");
        }

        final String newAcceptToken = jwtUtils.generateJwtToken(email);
        final String newRefreshToken = jwtUtils.generateRefreshToken(email);

        return new TokenResponse(newAcceptToken, newRefreshToken);
    }

    public boolean validateToken(String token) {
        try {
            jwtUtils.validateToken(token);
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка валидации токена: " + e.getMessage());
            return false;
        }
    }
}
