package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.entity.RevokedToken;
import org.example.repository.RevokedTokenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class TokenBlackListService {

    private final RevokedTokenRepository revokedTokenRepository;

    @Transactional
    public boolean revokedIfNotRevoked(String token, String email, String tokenType) {

        if (!isRevoked(token)) {
            final RevokedToken revokedToken = new RevokedToken(token, email, LocalDateTime.now(), tokenType);
            revokedTokenRepository.save(revokedToken);
            return true;
        }
        return false;
    }

    public boolean isRevoked(String token) {
        return revokedTokenRepository.existsById(token);
    }


}
