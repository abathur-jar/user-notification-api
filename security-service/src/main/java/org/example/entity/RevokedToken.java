package org.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import java.time.LocalDateTime;

@Entity
@Table(name = "revoked_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevokedToken {

    @Id
    @Column(nullable = false, length = 500)
    private String token;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false)
    private LocalDateTime revokedAt;

    @Column(nullable = false, length = 20)
    private String tokenType;


}
