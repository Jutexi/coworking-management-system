package com.app.coworking.service;

import com.app.coworking.cache.UserCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.User;
import com.app.coworking.model.enums.Role;
import com.app.coworking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCache userCache;

    @InjectMocks
    private UserService userService;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Пользователь 1
        user1 = new User();
        user1.setId(1L);
        user1.setFirstName("John");
        user1.setLastName("Doe");
        user1.setEmail("john@example.com");
        user1.setPassword("pass123");
        user1.setRole(Role.USER);
        user1.setReservations(new HashSet<>());

        // Пользователь 2
        user2 = new User();
        user2.setId(2L);
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmail("jane@example.com");
        user2.setPassword("pass456");
        user2.setRole(Role.ADMIN);
        user2.setReservations(new HashSet<>());
    }

    // -------------------- getUserById --------------------
    @Test
    void getUserById_shouldReturnUserFromCache() {
        when(userCache.get(1L)).thenReturn(user1);

        User result = userService.getUserById(1L);

        assertEquals(user1, result);
        verify(userCache, times(1)).get(1L);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void getUserById_shouldReturnUserFromRepositoryIfCacheEmpty() {
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));

        User result = userService.getUserById(1L);

        assertEquals(user1, result);
        verify(userCache).put(1L, user1);
    }

    @Test
    void getUserById_shouldThrowIfNotFound() {
        when(userCache.get(3L)).thenReturn(null);
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(3L));
    }

    // -------------------- getAllUsers --------------------
    @Test
    void getAllUsers_shouldReturnAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user1, user2));

        List<User> users = userService.getAllUsers();

        assertEquals(2, users.size());
        assertTrue(users.contains(user1));
        assertTrue(users.contains(user2));
    }

    // -------------------- createUser --------------------
    @Test
    void createUser_shouldSaveUserIfEmailNotExists() {
        when(userRepository.existsByEmail(user1.getEmail())).thenReturn(false);
        when(userRepository.save(user1)).thenReturn(user1);

        User saved = userService.createUser(user1);

        assertEquals(user1, saved);
        verify(userCache).put(user1.getId(), user1);
    }

    @Test
    void createUser_shouldThrowIfEmailExists() {
        when(userRepository.existsByEmail(user1.getEmail())).thenReturn(true);

        assertThrows(AlreadyExistsException.class, () -> userService.createUser(user1));
        verify(userRepository, never()).save(any());
        verify(userCache, never()).put(anyLong(), any());
    }

    // -------------------- updateUser --------------------
    @Test
    void updateUser_shouldUpdateExistingUser() {
        // Мокаем кеш: сначала get вернет user1
        when(userCache.get(1L)).thenReturn(user1);

        // Мокаем сохранение в репозитории
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Создаем обновленные данные
        User updated = new User();
        updated.setFirstName("Johnathan");
        updated.setLastName("Doe");
        updated.setEmail("john@example.com");
        updated.setPassword("newpass");
        updated.setRole(Role.USER);

        // Вызываем метод сервиса
        User result = userService.updateUser(1L, updated);

        // Проверяем, что данные обновились
        assertEquals("Johnathan", result.getFirstName());
        assertEquals("newpass", result.getPassword());

        // Проверяем, что кеш был обновлен
        verify(userCache).put(1L, result);

        // Проверяем, что репозиторий вызван для сохранения
        verify(userRepository).save(result);
    }


    @Test
    void updateUser_shouldThrowIfEmailTakenByOther() {
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user1));
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        doNothing().when(userCache).put(anyLong(), any()); // <-- добавлено

        User updated = new User();
        updated.setFirstName("John");
        updated.setLastName("Doe");
        updated.setEmail("jane@example.com");
        updated.setPassword("pass123");
        updated.setRole(Role.USER);

        assertThrows(AlreadyExistsException.class, () -> userService.updateUser(1L, updated));

        verify(userRepository, never()).save(any());
    }


    // -------------------- deleteUser --------------------
    @Test
    void deleteUser_shouldDeleteUserAndRemoveFromCache() {
        when(userCache.get(1L)).thenReturn(user1);

        userService.deleteUser(1L);

        verify(userRepository).delete(user1);
        verify(userCache).remove(1L);
    }

    @Test
    void deleteUser_shouldThrowIfNotFound() {
        when(userCache.get(3L)).thenReturn(null);
        when(userRepository.findById(3L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(3L));
        verify(userRepository, never()).delete(any());
    }
}
