package org.example;

import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.entity.User;
import org.example.repository.UserRepository;
import org.example.service.UserEventProducer;
import org.example.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private UserService userService;

    private final User testUser = new User(1L, "John", "Doe", "john@gmail.com");

    // ✅ ОБЪЯВЛЯЕМ метод createTestUserRequest
    private UserRequest createTestUserRequest() {
        UserRequest request = new UserRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("john@gmail.com");
        return request;
    }

    @Test
    void shouldSaveUser() {
        // given
        UserRequest request = createTestUserRequest();  // ← Теперь метод существует!
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(userEventProducer).sendUserEvent(any());

        // when
        UserResponse result = userService.saveUser(request);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john@gmail.com");

        verify(userRepository).save(any(User.class));
        verify(userEventProducer).sendUserEvent(any());
    }

    @Test
    void shouldDeleteUser() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(testUser);
        doNothing().when(userEventProducer).sendUserEvent(any());

        // when
        userService.deleteUser(1L);

        // then
        verify(userRepository).findById(1L);
        verify(userRepository).delete(testUser);
        verify(userEventProducer).sendUserEvent(any());
    }

    @Test
    void shouldUpdateUser() {
        // given
        User existingUser = new User(1L, "OldName", "OldLastName", "old@email.com");

        UserRequest newData = new UserRequest();
        newData.setFirstName("NewName");
        newData.setLastName("NewLastName");
        newData.setEmail("new@email.com");

        User updatedUser = new User(1L, "NewName", "NewLastName", "new@email.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // when
        UserResponse result = userService.updateUser(1L, newData);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("NewName");
        assertThat(result.getLastName()).isEqualTo("NewLastName");
        assertThat(result.getEmail()).isEqualTo("new@email.com");

        verify(userRepository).findById(1L);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        // given
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> userService.getUser(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Такого пользователя не существует!");

        verify(userRepository).findById(999L);
    }

    @Test
    void shouldGetUser() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // when
        UserResponse result = userService.getUser(1L);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmail()).isEqualTo("john@gmail.com");

        verify(userRepository).findById(1L);
    }

    @Test
    void shouldGetAllUsers() {
        // given
        List<User> users = Arrays.asList(
                new User(1L, "John", "Doe", "john@mail.com"),
                new User(2L, "Jane", "Smith", "jane@mail.com")
        );
        when(userRepository.findAll()).thenReturn(users);

        // when
        List<UserResponse> result = userService.getAllUsers();

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getFirstName()).isEqualTo("John");
        assertThat(result.get(1).getFirstName()).isEqualTo("Jane");

        verify(userRepository).findAll();
    }

    @Test
    void shouldSaveUserEvenWhenKafkaIsDropped() {
        // given
        UserRequest request = createTestUserRequest();  // ← И здесь используем!
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doThrow(new RuntimeException("Kafka unavailable"))
                .when(userEventProducer).sendUserEvent(any());

        // when & then
        assertThatThrownBy(() -> userService.saveUser(request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Kafka");

        verify(userRepository).save(any(User.class));
        verify(userEventProducer).sendUserEvent(any());
    }
}