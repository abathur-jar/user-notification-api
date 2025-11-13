package org.example.repository;

import org.example.entity.SecurityUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SecurityRepository extends JpaRepository<SecurityUser, Long> {
    Optional<SecurityUser> findByEmail(String email);
}
