package org.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    @Value("${security.jjwt}")
    private String jwtSecret;

    private SecretKey getSecretKey() {
        final byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String email) {
        final Instant expInstant = Instant.now().plus(30, ChronoUnit.MINUTES);
        final Date expirationDate = Date.from(expInstant);

        return Jwts.builder()
                .subject(email)
                .expiration(expirationDate)
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(String email) {
        final Instant expInstant = Instant.now().plus(14, ChronoUnit.DAYS);
        final Date expirationDate = Date.from(expInstant);

        return Jwts.builder()
                .subject(email)
                .expiration(expirationDate)
                .signWith(getSecretKey())
                .compact();
    }

    public Jws<Claims> validateToken(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token);
    }

    public boolean isTokenValid(String token) {
        try {
            final Jws<Claims> verifyToken = validateToken(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            final Jws<Claims> verifyToken = validateToken(token);
            return verifyToken.getPayload().getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Токен не валидный: " + e.getMessage());
        }
    }


}
