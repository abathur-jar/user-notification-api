package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class UserProfileResponse {
    private final Long id;
    private final String firstName;
    private final String lastName;
    private final String email;
}
