package org.example.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.details.CustomUserDetailsService;
import org.example.util.JwtUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtNotificationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService;


    public JwtNotificationFilter(JwtUtils jwtUtils, CustomUserDetailsService userDetailsService) {
        this.jwtUtils = jwtUtils;
        this.userDetailsService = userDetailsService;

    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // ==================== ШАГ 1: ПОЛУЧАЕМ ТОКЕН ИЗ ЗАГОЛОВКА ====================
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // Пример заголовка: "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
        if (authHeader!= null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7); // ← отрезаем "Bearer "
            email = jwtUtils.getEmailFromToken(token); // Достаём email из токена
        }

        // ==================== ШАГ 2: ПРОВЕРЯЕМ НУЖНО ЛИ АУТЕНТИФИЦИРОВАТЬ ====================
        if (authHeader != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                // === ШАГ 3: ПРОВЕРЯЕМ ТОКЕН ===
            jwtUtils.validateToken(token);
                // === ШАГ 4: ЗАГРУЖАЕМ ПОЛЬЗОВАТЕЛЯ ИЗ БАЗЫ ===
            final UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                // === ШАГ 5: СОЗДАЁМ ОБЪЕКТ АУТЕНТИФИКАЦИИ ===
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                // === ШАГ 6: ДОБАВЛЯЕМ ИНФОРМАЦИЮ О ЗАПРОСЕ ===
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // === ШАГ 7: СОХРАНЯЕМ В SECURITYCONTEXT ===
            SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (Exception e) {
                System.out.println("JWT ошибка authentication: "+ e.getMessage());
            }
        }
        // ШАГ 3: Передаём запрос дальше
        filterChain.doFilter(request, response);
    }
}
