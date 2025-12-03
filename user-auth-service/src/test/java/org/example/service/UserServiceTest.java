package org.example.service;

import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.entity.AuthUser;
import org.example.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private UserEventProducer userEventProducer;

    @InjectMocks
    private UserService service;

    @Test
    void testGetUser() {
        Long testId = 5L;

        final AuthUser testUser = new AuthUser();
        testUser.setId(5L);
        testUser.setFirstName("Иван");
        testUser.setLastName("Иванов");
        testUser.setEmail("ivan@mail.ru");

        // arrange
        when(repository.findById(testId)).thenReturn(Optional.of(testUser));

        // Act
        final UserResponse result = service.getUser(testId);

        System.out.println("User ID: " + testId);
        System.out.println("Expected name: " + testUser.getFirstName());
        System.out.println("Actual name: " + result.getFirstName());
        System.out.println("Expected email: " + testUser.getEmail());
        System.out.println("Actual email: " + result.getEmail());

        // Assert
        assertNotNull(result, "Не может быть null");
        assertEquals(testUser.getFirstName(), result.getFirstName());
        assertEquals(testUser.getLastName(), result.getLastName());
        assertEquals(testUser.getEmail(), result.getEmail());

        // Verify
        verify(repository, times(1)).findById(testId);
        verify(repository, never()).findByEmail(any());
        verify(userEventProducer, never()).sendUserEvent(any());
    }

    @Test
    void testUpdateUser() {

        Long testId = 10L;

        final UserRequest updateRequest = new UserRequest();
        updateRequest.setEmail("ivan@example.com");
        updateRequest.setFirstName("Ivan");
        updateRequest.setLastName("Ivanov");

        final AuthUser existingUser = new AuthUser();
        existingUser.setId(testId);
        existingUser.setFirstName("Иван");
        existingUser.setLastName("Иванов");
        existingUser.setEmail("ivan@mail.ru");

        when(repository.findById(testId)).thenReturn(Optional.of(existingUser));

        when(repository.save(existingUser)).thenReturn(existingUser);

        final UserResponse result = service.updateUser(testId, updateRequest);

        System.out.println("User ID: " + testId);
        System.out.println("Actual name: " + result.getFirstName());
        System.out.println("Actual family: " + result.getLastName());
        System.out.println("Actual email: " + result.getEmail());

        assertNotNull(existingUser, "Сannot be null");
        assertEquals("Ivan", existingUser.getFirstName());
        assertEquals("Ivanov", existingUser.getLastName());
        assertEquals("ivan@example.com", existingUser.getEmail());

        verify(repository, times(1)).save(existingUser);
        verify(repository, times(1)).findById(testId);
        verify(repository, never()).delete(any());
        verify(userEventProducer, times(1)).sendUserEvent(any());

    }

    @Test
    void testGetUser_UserNotFound() {
        when(repository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.getUser(999L));

        verify(repository, times(1)).findById(999L);
        verify(repository, never()).delete(any());
        verify(repository, never()).save(any());
        verify(userEventProducer, never()).sendUserEvent(any());
    }
}
