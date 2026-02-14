package org.example.service;


import lombok.RequiredArgsConstructor;
import org.example.dto.AuthResponse;
import org.example.dto.RegisterRequest;
import org.example.dto.UserEvent;
import org.example.entity.AuthUser;
import org.example.repository.UserRepository;
import org.example.util.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final UserEventProducer userEventProducer;


    public String registerUser(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RuntimeException("Пользователь с email: " + request.email() + " уже существует!");
        }
        userRepository.save(transformUser(request));
        System.out.println("✅ User saved to database");

        System.out.println("=== SENDING KAFKA EVENT ===");
        UserEvent event = new UserEvent("CREATE", request.email());
        System.out.println("Event: " + event.getOperation() + " for " + event.getEmail());

        userEventProducer.sendUserEvent(event);
        System.out.println("✅ Kafka event sent");
        return "Пользователь " + request.firstName() + " " + request.lastName()
                + ", с email: " + request.email() + " был успешно добавлен!";
    }

    public AuthResponse loginUser(String email, String password) {
        try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

                final String accessToken = jwtUtil.generateAccessToken(email);
                final String refreshToken = jwtUtil.generateRefreshToken(email);

                return new AuthResponse(accessToken, refreshToken, "Bearer");

        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Неверный email или пароль!");
        } catch (AuthenticationException e) {
            throw new BadCredentialsException("Ошибка аутентификации: " + e.getMessage());
        }
    }

    public AuthResponse refreshTokens(String refreshToken) {
            final String email = jwtUtil.getEmailFromToken(refreshToken);

            final String accessToken = jwtUtil.generateAccessToken(email);
            final String newRefreshToken = jwtUtil.generateRefreshToken(email);

            return new AuthResponse(accessToken, newRefreshToken);
    }

    public boolean validateToken(String token) {
        return jwtUtil.isTokenValid(token);
    }


    // метод для создания пользователя
    private AuthUser transformUser(RegisterRequest request) {
        AuthUser newUser = new AuthUser();
        newUser.setEmail(request.email());
        newUser.setFirstName(request.firstName());
        newUser.setLastName(request.lastName());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setPhone(request.phone());

        return newUser;
    }

}
