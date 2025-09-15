package com.app.coworking.service;

import com.app.coworking.cache.UserCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.enums.Role;
import com.app.coworking.model.User;
import com.app.coworking.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserCache userCache;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        user = new User(
                1L,
                "securePass",
                "john@example.com",
                "John",
                "Doe",
                Role.USER,
                new HashSet<>()
        );
    }

    // -------------------- getUserById --------------------
    @Test
    void getUserById_shouldReturnFromCache() {
        when(userCache.get(1L)).thenReturn(user);

        User result = userService.getUserById(1L);

        assertEquals("John", result.getFirstName());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void getUserById_shouldReturnFromRepositoryAndCacheIt() {
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.getUserById(1L);

        assertEquals("john@example.com", result.getEmail());
        verify(userCache).put(1L, user);
    }

    @Test
    void getUserById_shouldThrowIfNotFound() {
        when(userCache.get(1L)).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(1L));
    }

    // -------------------- getAllUsers --------------------
    @Test
    void getAllUsers_shouldReturnList() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<User> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("John", result.get(0).getFirstName());
    }

    // -------------------- createUser --------------------
    @Test
    void createUser_shouldSaveAndCache() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.createUser(user);

        assertEquals("john@example.com", result.getEmail());
        verify(userCache).put(1L, user);
    }

    @Test
    void createUser_shouldThrowIfEmailExists() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> userService.createUser(user));
    }

    // -------------------- updateUser --------------------
    @Test
    void updateUser_shouldUpdateAndCache() {
        User updated = new User(
                1L,
                "newPass",
                "john@example.com",
                "Johnathan",
                "Doe",
                Role.ADMIN,
                new HashSet<>()
        );

        when(userCache.get(1L)).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any())).thenReturn(updated);

        User result = userService.updateUser(1L, updated);

        assertEquals("Johnathan", result.getFirstName());
        assertEquals(Role.ADMIN, result.getRole());
        verify(userCache).put(1L, updated);
    }

    @Test
    void updateUser_shouldThrowIfEmailTakenByAnother() {
        User updated = new User(
                1L,
                "newPass",
                "taken@example.com",
                "John",
                "Doe",
                Role.USER,
                new HashSet<>()
        );

        when(userCache.get(1L)).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> userService.updateUser(1L, updated));
    }

    // -------------------- deleteUser --------------------
    @Test
    void deleteUser_shouldRemoveFromRepoAndCache() {
        when(userCache.get(1L)).thenReturn(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
        verify(userCache).remove(1L);
    }
}
