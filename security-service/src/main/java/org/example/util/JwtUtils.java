package org.example.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${security.jjwt}")
    private String jwtSecret;

    public SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    } 

    public String generateJwtToken(String email) {
        Date date = Date.from(LocalDateTime.now().plusMinutes(30).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder().subject(email).expiration(date).signWith(getSecretKey()).compact();
    }


    public String generateRefreshToken(String email) {
        Date date = Date.from(LocalDateTime.now().plusDays(14).atZone(ZoneId.systemDefault()).toInstant());

        return Jwts.builder().subject(email).expiration(date).signWith(getSecretKey()).compact();
    }

    public Jws<Claims> validateToken(String token) {
        return Jwts.parser().verifyWith(getSecretKey()).build().parseSignedClaims(token);
    }

    public String getEmailFromToken(String token) {
        try {
            final Jws<Claims> claimsJws = validateToken(token);
            return claimsJws.getPayload().getSubject();
        } catch (Exception e) {
            throw new RuntimeException("Невалидный токен: " + e.getMessage());
        }
    }
}
