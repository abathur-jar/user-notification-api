package org.example.unit.service;

import org.example.dto.RegisterRequest;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.entity.AuthUser;
import org.example.repository.UserRepository;
import org.example.service.UserEventProducer;
import org.example.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    protected UserRepository repository;

    @Mock
    protected UserEventProducer userEventProducer;

    @InjectMocks
    protected UserService userService;

    @Nested
    @DisplayName("GET TESTS")
    class GetUserTests {

        @ParameterizedTest
        @DisplayName("Параметризованные тесты с разными id на GET")
        @ValueSource(longs = {1L, 2L, 5L, 10L, 100L})
        void getUser_DifferentId_Success(Long userId) {
            // given
            final AuthUser testUser = new AuthUser();
            testUser.setId(userId);
            testUser.setFirstName("User_id" + userId);
            testUser.setLastName("Testov");
            testUser.setEmail("user.id" + userId + "@example.com");
            // password и phone не нужны - не устанавливаем

            when(repository.findById(userId)).thenReturn(Optional.of(testUser));

            // when
            final UserResponse result = userService.getUser(userId);

            // then
            assertEquals("User_id" + userId, result.getFirstName());
            assertEquals("user.id" + userId + "@example.com", result.getEmail());

            verify(repository, times(1)).findById(userId);
            verify(userEventProducer, never()).sendUserEvent(any());
        }

        @ParameterizedTest
        @DisplayName("Комплексные тестовые сценарии на GET")
        @CsvSource({
                "1, 'Ivan', 'Ivanov', 'ivan@mail.com'",
                "2, 'Maria', 'Petrova', 'maria@company.com'",
                "5, 'Admin', 'System', 'admin@test.org'"
        })
        void getUser_MultipleParameters_Success(Long id, String firstName, String lastName, String email) {
            // Given
            final AuthUser testUser = new AuthUser();
            testUser.setId(id);
            testUser.setFirstName(firstName);
            testUser.setLastName(lastName);
            testUser.setEmail(email);
            // password и phone не нужны - не устанавливаем

            when(repository.findById(id)).thenReturn(Optional.of(testUser));

            // When
            UserResponse result = userService.getUser(id);

            // Then
            assertEquals(firstName, result.getFirstName());
            assertEquals(lastName, result.getLastName());
            assertEquals(email, result.getEmail());
        }

        @ParameterizedTest
        @DisplayName("Граничные значения id на GET")
        @ValueSource(longs = {Long.MIN_VALUE, -100L, -1L, 0L, 100L, Long.MAX_VALUE})
        void getUser_BoundaryId(Long boundaryId) {
            // given
            if (boundaryId <= 0) {
                when(repository.findById(boundaryId)).thenReturn(Optional.empty());

                final RuntimeException exception = assertThrows(RuntimeException.class,
                        () -> userService.getUser(boundaryId));

                assertEquals("User not found", exception.getMessage());
            } else {
                final AuthUser testUser = new AuthUser();
                testUser.setId(boundaryId);
                testUser.setFirstName("Ivan");
                testUser.setLastName("Ivanov");
                testUser.setEmail("ivan.ivanov.test" + boundaryId + "@example.com");

                when(repository.findById(boundaryId)).thenReturn(Optional.of(testUser));

                // when
                final UserResponse result = userService.getUser(boundaryId);
                assertEquals(boundaryId, result.getId());
                assertEquals("ivan.ivanov.test" + boundaryId + "@example.com", result.getEmail());
            }
        }

        @Test
        @DisplayName("Поиск пользователя по id на GET")
        void getFindUserOnUserService() {
            // Arrange
            Long userId = 1L;

            final AuthUser testUser = new AuthUser();
            testUser.setId(1L);
            testUser.setFirstName("Ivan");
            testUser.setLastName("Ivanov");
            testUser.setEmail("ivan.ivanov.test1@example.com");
            // password и phone не нужны - не устанавливаем

            when(repository.findById(userId)).thenReturn(Optional.of(testUser));

            // Act
            final UserResponse userResult = userService.getUser(userId);

            // Assert
            assertEquals("Ivan", userResult.getFirstName());
            assertEquals("Ivanov", userResult.getLastName());
            assertEquals("ivan.ivanov.test1@example.com", userResult.getEmail());
            assertNotNull(userResult, "результат не должен быть null");

            verify(repository, times(1)).findById(userId);
            verify(userEventProducer, never()).sendUserEvent(any());
        }

        @Test
        @DisplayName("Проверка NotFoundUser, если найден такой пользователь на GET")
        void getUser_UserNotFound() {
            // Arrange
            Long id = 999L;
            when(repository.findById(id)).thenReturn(Optional.empty());

            // Act & Assert
            final RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.getUser(id));

            assertEquals("User not found", exception.getMessage());

            verify(repository, times(1)).findById(id);
            verify(userEventProducer, never()).sendUserEvent(any());
        }
    }

    @Nested
    @DisplayName("DELETE TESTS")
    class DeleteUserTests {

        @Test
        @DisplayName("Delete User Tests")
        void deleteUser_UserExists_DeleteSuccessfully() {
            // given
            Long id = 1L;
            final AuthUser testUser = new AuthUser();
            testUser.setId(1L);
            testUser.setFirstName("Ivan");
            testUser.setLastName("Ivanov");
            testUser.setEmail("ivan.ivanov.test1@example.com");
            // password и phone не нужны - не устанавливаем

            when(repository.findById(id)).thenReturn(Optional.of(testUser));
            doNothing().when(repository).delete(testUser);

            // when
            userService.deleteUser(id);

            // then
            verify(repository, times(1)).findById(id);
            verify(repository, times(1)).delete(testUser);
            verify(userEventProducer, times(1)).sendUserEvent(argThat(event ->
                    "DELETE".equals(event.getOperation()) &&
                            testUser.getEmail().equals(event.getEmail())
            ));
        }
    }

    @Nested
    @DisplayName("UPDATE TESTS")
    class UpdateUserTests {

        @Test
        @DisplayName("Exception on update user UPDATE")
        void updateUser_NotFoundUser() {
            // given
            Long userId = 1L;

            final UserRequest newUser = new UserRequest();
            newUser.setFirstName("newName");
            newUser.setEmail("newMail@example.com");

            when(repository.findById(userId)).thenReturn(Optional.empty());

            // when
            final RuntimeException exception = assertThrows(RuntimeException.class, () -> {
                userService.updateUser(userId, newUser);
            });

            // then
            assertEquals("User not found", exception.getMessage());

            verify(repository, times(1)).findById(userId);
            verify(repository, never()).save(any());
            verify(userEventProducer, never()).sendUserEvent(any());
        }

        @Test
        @DisplayName("Проверка на обновление входных данных UPDATE")
        void updateUser_UserExists_UpdateSuccessfully() {
            // Arrange
            Long userId = 1L;
            AuthUser existingUser = new AuthUser();
            existingUser.setId(userId);
            existingUser.setFirstName("Old");
            existingUser.setLastName("User");
            existingUser.setEmail("old@email.com");
            // password и phone не нужны - не устанавливаем

            when(repository.findById(userId)).thenReturn(Optional.of(existingUser));
            when(repository.save(any(AuthUser.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            UserRequest updateRequest = new UserRequest();
            updateRequest.setFirstName("NewName");
            updateRequest.setLastName("NewLastName");
            updateRequest.setEmail("new.email@example.com");

            final UserResponse result = userService.updateUser(userId, updateRequest);

            // Assert
            assertEquals("NewName", result.getFirstName());
            assertEquals("NewLastName", result.getLastName());
            assertEquals("new.email@example.com", result.getEmail());

            verify(repository, times(1)).findById(userId);
            verify(repository, times(1)).save(any(AuthUser.class));
            verify(userEventProducer, times(1)).sendUserEvent(any());
        }
    }
}