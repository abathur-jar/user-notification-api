package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.entity.AuthUser;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.example.dto.UserEvent;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repository;
    private final UserEventProducer userEventProducer;

    public UserResponse getUser(Long id) {
        AuthUser user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToResponse(user);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        AuthUser user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        AuthUser updatedUser = repository.save(user);
        userEventProducer.sendUserEvent(new UserEvent("UPDATE", updatedUser.getEmail()));

        return convertToResponse(updatedUser);
    }

    public void deleteUser(Long id) {
        AuthUser user = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userEventProducer.sendUserEvent(new UserEvent("DELETE", user.getEmail()));
        repository.delete(user);
    }

    public UserResponse convertToResponse(AuthUser user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        return response;
    }
}