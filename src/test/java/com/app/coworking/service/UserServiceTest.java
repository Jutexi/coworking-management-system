package com.app.coworking.service;

import com.app.coworking.cache.UserCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.InvalidArgumentException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.Reservation;
import com.app.coworking.model.User;
import com.app.coworking.model.enums.Role;
import com.app.coworking.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCache userCache;

    @InjectMocks
    private UserService userService;

    @Test
    void getUserById_WhenExistsInCache_ShouldReturnCachedUser() {
        // Arrange
        Long id = 1L;
        User cachedUser = new User();
        cachedUser.setId(id);
        cachedUser.setEmail("cached@test.com");
        when(userCache.get(id)).thenReturn(cachedUser);

        // Act
        User result = userService.getUserById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("cached@test.com", result.getEmail());
        verify(userCache, times(1)).get(id);
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserById_WhenNotInCacheButExistsInDb_ShouldReturnAndCacheUser() {
        // Arrange
        Long id = 1L;
        User dbUser = new User();
        dbUser.setId(id);
        dbUser.setEmail("db@test.com");
        when(userCache.get(id)).thenReturn(null);
        when(userRepository.findById(id)).thenReturn(Optional.of(dbUser));

        // Act
        User result = userService.getUserById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        assertEquals("db@test.com", result.getEmail());
        verify(userCache, times(1)).get(id);
        verify(userRepository, times(1)).findById(id);
        verify(userCache, times(1)).put(id, dbUser);
    }

    @Test
    void getUserById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long id = 999L;
        when(userCache.get(id)).thenReturn(null);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(id));
        verify(userCache, times(1)).get(id);
        verify(userRepository, times(1)).findById(id);
    }

    @Test
    void getAllUsers_ShouldReturnList() {
        // Arrange
        User user1 = new User();
        user1.setId(1L);
        User user2 = new User();
        user2.setId(2L);
        List<User> expectedList = Arrays.asList(user1, user2);
        when(userRepository.findAll()).thenReturn(expectedList);

        // Act
        List<User> result = userService.getAllUsers();

        // Assert
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void createUser_WhenValidData_ShouldSaveAndCache() {
        // Arrange
        User user = new User();
        user.setEmail("new@test.com");
        user.setPassword("password123");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.USER);

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.createUser(user);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).existsByEmail(user.getEmail());
        verify(userRepository, times(1)).save(user);
        verify(userCache, times(1)).put(any(), eq(user));
    }

    @Test
    void createUser_WhenReservationsProvided_ShouldThrowInvalidArgumentException() {
        // Arrange
        User user = new User();
        user.setReservations(new HashSet<>(Arrays.asList(new Reservation())));

        // Act & Assert
        assertThrows(InvalidArgumentException.class, () -> userService.createUser(user));
        verify(userRepository, never()).existsByEmail(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WhenEmailExists_ShouldThrowAlreadyExistsException() {
        // Arrange
        User user = new User();
        user.setEmail("existing@test.com");
        when(userRepository.existsByEmail(user.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> userService.createUser(user));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_WhenMissingRequiredFields_ShouldPropagateValidation() {
        // Arrange
        User user = new User(); // Не установлены обязательные поля

        // Act & Assert - валидация будет на уровне контроллера/Entity, но сервис должен упасть при сохранении
        when(userRepository.existsByEmail(null)).thenReturn(false);
        when(userRepository.save(any())).thenThrow(RuntimeException.class);

        // Проверяем, что исключение пробрасывается дальше
        assertThrows(RuntimeException.class, () -> userService.createUser(user));
    }

    @Test
    void updateUser_WhenValidData_ShouldUpdateAndCache() {
        // Arrange
        Long id = 1L;
        User existing = new User();
        existing.setId(id);
        existing.setEmail("old@test.com");
        existing.setFirstName("Old");
        existing.setLastName("Name");
        existing.setPassword("oldpass");
        existing.setRole(Role.USER);

        User updated = new User();
        updated.setEmail("new@test.com");
        updated.setFirstName("New");
        updated.setLastName("Name");
        updated.setPassword("newpass");
        updated.setRole(Role.ADMIN);

        when(userCache.get(id)).thenReturn(existing);
        when(userRepository.existsByEmail(updated.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(existing);

        // Act
        User result = userService.updateUser(id, updated);

        // Assert
        assertNotNull(result);
        assertEquals(updated.getEmail(), existing.getEmail());
        assertEquals(updated.getFirstName(), existing.getFirstName());
        assertEquals(updated.getLastName(), existing.getLastName());
        assertEquals(updated.getPassword(), existing.getPassword());
        assertEquals(updated.getRole(), existing.getRole());
        verify(userRepository, times(1)).save(existing);
        verify(userCache, times(1)).put(id, existing);
    }

    @Test
    void updateUser_WhenReservationsProvided_ShouldThrowInvalidArgumentException() {
        // Arrange
        Long id = 1L;
        User updated = new User();
        updated.setReservations(new HashSet<>(Arrays.asList(new Reservation())));

        // Act & Assert
        assertThrows(InvalidArgumentException.class, () -> userService.updateUser(id, updated));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenEmailExistsForOtherUser_ShouldThrowAlreadyExistsException() {
        // Arrange
        Long id = 1L;
        User existing = new User();
        existing.setId(id);
        existing.setEmail("old@test.com");

        User updated = new User();
        updated.setEmail("existing@test.com");

        when(userCache.get(id)).thenReturn(existing);
        when(userRepository.existsByEmail(updated.getEmail())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(id, updated));
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_WhenSameEmail_ShouldUpdateSuccessfully() {
        // Arrange
        Long id = 1L;
        User existing = new User();
        existing.setId(id);
        existing.setEmail("same@test.com");
        existing.setFirstName("Old");
        existing.setLastName("Name");

        User updated = new User();
        updated.setEmail("same@test.com"); // То же самое email
        updated.setFirstName("New");
        updated.setLastName("Name");

        when(userCache.get(id)).thenReturn(existing);
        when(userRepository.save(any(User.class))).thenReturn(existing);

        // Act
        User result = userService.updateUser(id, updated);

        // Assert
        assertNotNull(result);
        assertEquals(updated.getFirstName(), existing.getFirstName());
        verify(userRepository, times(1)).save(existing);
        verify(userCache, times(1)).put(id, existing);
    }

    @Test
    void updateUser_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long id = 999L;
        User updated = new User();
        updated.setEmail("test@test.com");

        when(userCache.get(id)).thenReturn(null);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.updateUser(id, updated));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_WhenNoReservations_ShouldDeleteAndRemoveFromCache() {
        // Arrange
        Long id = 1L;
        User existing = new User();
        existing.setId(id);
        existing.setReservations(new HashSet<>());

        when(userCache.get(id)).thenReturn(existing);

        // Act
        userService.deleteUser(id);

        // Assert
        verify(userRepository, times(1)).delete(existing);
        verify(userCache, times(1)).remove(id);
    }

    @Test
    void deleteUser_WhenHasReservations_ShouldThrowInvalidArgumentException() {
        // Arrange
        Long id = 1L;
        User existing = new User();
        existing.setId(id);
        existing.setReservations(new HashSet<>(Arrays.asList(new Reservation())));

        when(userCache.get(id)).thenReturn(existing);

        // Act & Assert
        assertThrows(InvalidArgumentException.class, () -> userService.deleteUser(id));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long id = 999L;
        when(userCache.get(id)).thenReturn(null);
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(id));
        verify(userRepository, never()).delete(any());
    }

    @Test
    void createUser_WithDifferentRoles_ShouldSaveCorrectly() {
        // Arrange
        User user = new User();
        user.setEmail("admin@test.com");
        user.setPassword("password123");
        user.setFirstName("Admin");
        user.setLastName("User");
        user.setRole(Role.ADMIN);

        when(userRepository.existsByEmail(user.getEmail())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User result = userService.createUser(user);

        // Assert
        assertNotNull(result);
        assertEquals(Role.ADMIN, result.getRole());
        verify(userRepository, times(1)).save(user);
    }
}