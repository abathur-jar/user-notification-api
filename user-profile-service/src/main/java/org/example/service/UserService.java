package org.example.service;

import lombok.RequiredArgsConstructor;
import org.example.dto.UserEvent;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserEventProducer userEventProducer;

    public UserResponse saveUser(UserRequest request) {
        if (userRepository.findByEmail(request.getEmail())) {
            throw new RuntimeException("Пользователь с таким email " + request.getEmail() + " уже существует!");
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        final User savedUser = userRepository.save(user);
        userEventProducer.sendUserEvent(new UserEvent("CREATE", savedUser.getEmail()));

        return convertToResponse(savedUser);
    }

    public UserResponse updateUser(Long id, UserRequest request) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Такого пользователя не существует!"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        final User updatedUser = userRepository.save(user);
        return convertToResponse(updatedUser);

    }

    public void deleteUser(Long id) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Такого пользователя не существует!"));

        userEventProducer.sendUserEvent(new UserEvent("DELETE", user.getEmail()));
        userRepository.delete(user);
    }

    public UserResponse getUser(Long id) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Такого пользователя не существует!"));
        return convertToResponse(user);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public UserResponse convertToResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        return response;
    }
}
