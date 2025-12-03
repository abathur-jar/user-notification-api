package org.example.details;

import lombok.RequiredArgsConstructor;
import org.example.entity.AuthUser;
import org.example.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthUserDetails implements UserDetailsService {

    private final UserRepository repository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final String email = username;
        final AuthUser verifyUser = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователя: " + email + " в базе данных нет!"));
        return verifyUser;
    }
}
