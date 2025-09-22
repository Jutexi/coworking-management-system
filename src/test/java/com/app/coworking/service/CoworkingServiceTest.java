package com.app.coworking.service;

import com.app.coworking.cache.CoworkingCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.InvalidArgumentException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.Coworking;
import com.app.coworking.model.Workspace;
import com.app.coworking.repository.CoworkingRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CoworkingServiceTest {

    @Mock
    private CoworkingRepository coworkingRepository;

    @Mock
    private CoworkingCache coworkingCache;

    @InjectMocks
    private CoworkingService coworkingService;

    @Test
    void getCoworkingById_WhenExistsInCache_ShouldReturnCachedCoworking() {
        // Arrange
        Long id = 1L;
        Coworking cachedCoworking = new Coworking();
        cachedCoworking.setId(id);
        when(coworkingCache.get(id)).thenReturn(cachedCoworking);

        // Act
        Coworking result = coworkingService.getCoworkingById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(coworkingCache, times(1)).get(id);
        verify(coworkingRepository, never()).findById(any());
    }

    @Test
    void getCoworkingById_WhenNotInCacheButExistsInDb_ShouldReturnAndCacheCoworking() {
        // Arrange
        Long id = 1L;
        Coworking dbCoworking = new Coworking();
        dbCoworking.setId(id);
        when(coworkingCache.get(id)).thenReturn(null);
        when(coworkingRepository.findById(id)).thenReturn(Optional.of(dbCoworking));

        // Act
        Coworking result = coworkingService.getCoworkingById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(coworkingCache, times(1)).get(id);
        verify(coworkingRepository, times(1)).findById(id);
        verify(coworkingCache, times(1)).put(id, dbCoworking);
    }

    @Test
    void getCoworkingById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long id = 999L;
        when(coworkingCache.get(id)).thenReturn(null);
        when(coworkingRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> coworkingService.getCoworkingById(id));
        verify(coworkingCache, times(1)).get(id);
        verify(coworkingRepository, times(1)).findById(id);
    }

    @Test
    void getAllCoworkings_ShouldReturnList() {
        // Arrange
        Coworking coworking1 = new Coworking();
        Coworking coworking2 = new Coworking();
        List<Coworking> expectedList = Arrays.asList(coworking1, coworking2);
        when(coworkingRepository.findAll()).thenReturn(expectedList);

        // Act
        List<Coworking> result = coworkingService.getAllCoworkings();

        // Assert
        assertEquals(2, result.size());
        verify(coworkingRepository, times(1)).findAll();
    }

    @Test
    void createCoworking_WhenValidData_ShouldSaveAndCache() {
        // Arrange
        Coworking coworking = new Coworking();
        coworking.setName("Test Coworking");
        coworking.setAddress("Test Address");
        coworking.setEmail("test@test.com");
        coworking.setPhoneNumber("+1234567890");

        when(coworkingRepository.existsByName(anyString())).thenReturn(false);
        when(coworkingRepository.existsByAddress(anyString())).thenReturn(false);
        when(coworkingRepository.save(any(Coworking.class))).thenReturn(coworking);

        // Act
        Coworking result = coworkingService.createCoworking(coworking);

        // Assert
        assertNotNull(result);
        verify(coworkingRepository, times(1)).existsByName(coworking.getName());
        verify(coworkingRepository, times(1)).existsByAddress(coworking.getAddress());
        verify(coworkingRepository, times(1)).save(coworking);
        verify(coworkingCache, times(1)).put(any(), eq(coworking));
    }

    @Test
    void createCoworking_WhenWorkspacesProvided_ShouldThrowInvalidArgumentException() {
        // Arrange
        Coworking coworking = new Coworking();
        coworking.setWorkspaces(new HashSet<>(Arrays.asList(new Workspace())));

        // Act & Assert
        assertThrows(InvalidArgumentException.class, () -> coworkingService.createCoworking(coworking));
        verify(coworkingRepository, never()).save(any());
    }

    @Test
    void createCoworking_WhenNameExists_ShouldThrowAlreadyExistsException() {
        // Arrange
        Coworking coworking = new Coworking();
        coworking.setName("Existing Name");
        when(coworkingRepository.existsByName(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> coworkingService.createCoworking(coworking));
        verify(coworkingRepository, never()).save(any());
    }

    @Test
    void createCoworking_WhenAddressExists_ShouldThrowAlreadyExistsException() {
        // Arrange
        Coworking coworking = new Coworking();
        coworking.setName("New Name");
        coworking.setAddress("Existing Address");
        when(coworkingRepository.existsByName(anyString())).thenReturn(false);
        when(coworkingRepository.existsByAddress(anyString())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> coworkingService.createCoworking(coworking));
        verify(coworkingRepository, never()).save(any());
    }

    @Test
    void updateCoworking_WhenValidData_ShouldUpdateAndCache() {
        // Arrange
        Long id = 1L;
        Coworking existing = new Coworking();
        existing.setId(id);
        existing.setName("Old Name");
        existing.setAddress("Old Address");

        Coworking updated = new Coworking();
        updated.setName("New Name");
        updated.setAddress("New Address");
        updated.setEmail("new@test.com");
        updated.setPhoneNumber("+9876543210");
        updated.setDescription("New Description");

        when(coworkingCache.get(id)).thenReturn(existing);
        when(coworkingRepository.existsByName(updated.getName())).thenReturn(false);
        when(coworkingRepository.existsByAddress(updated.getAddress())).thenReturn(false);
        when(coworkingRepository.save(any(Coworking.class))).thenReturn(existing);

        // Act
        Coworking result = coworkingService.updateCoworking(id, updated);

        // Assert
        assertNotNull(result);
        assertEquals(updated.getName(), existing.getName());
        assertEquals(updated.getAddress(), existing.getAddress());
        verify(coworkingRepository, times(1)).save(existing);
        verify(coworkingCache, times(1)).put(id, existing);
    }

    @Test
    void updateCoworking_WhenWorkspacesProvided_ShouldThrowInvalidArgumentException() {
        // Arrange
        Long id = 1L;
        Coworking updated = new Coworking();
        updated.setWorkspaces(new HashSet<>(Arrays.asList(new Workspace())));

        // Act & Assert
        assertThrows(InvalidArgumentException.class, () -> coworkingService.updateCoworking(id, updated));
        verify(coworkingRepository, never()).save(any());
    }

    @Test
    void updateCoworking_WhenNameExistsForOtherCoworking_ShouldThrowAlreadyExistsException() {
        // Arrange
        Long id = 1L;
        Coworking existing = new Coworking();
        existing.setId(id);
        existing.setName("Old Name");

        Coworking updated = new Coworking();
        updated.setName("Existing Name");

        when(coworkingCache.get(id)).thenReturn(existing);
        when(coworkingRepository.existsByName(updated.getName())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> coworkingService.updateCoworking(id, updated));
        verify(coworkingRepository, never()).save(any());
    }

    @Test
    void updateCoworking_WhenAddressExistsForOtherCoworking_ShouldThrowAlreadyExistsException() {
        // Arrange
        Long id = 1L;
        Coworking existing = new Coworking();
        existing.setId(id);
        existing.setName("Old Name");
        existing.setAddress("Old Address");

        Coworking updated = new Coworking();
        updated.setName("New Name");
        updated.setAddress("Existing Address");

        when(coworkingCache.get(id)).thenReturn(existing);
        when(coworkingRepository.existsByName(updated.getName())).thenReturn(false);
        when(coworkingRepository.existsByAddress(updated.getAddress())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class, () -> coworkingService.updateCoworking(id, updated));
        verify(coworkingRepository, never()).save(any());
    }

    @Test
    void deleteCoworking_WhenNoWorkspaces_ShouldDeleteAndRemoveFromCache() {
        // Arrange
        Long id = 1L;
        Coworking existing = new Coworking();
        existing.setId(id);
        existing.setWorkspaces(new HashSet<>());

        when(coworkingCache.get(id)).thenReturn(existing);

        // Act
        coworkingService.deleteCoworking(id);

        // Assert
        verify(coworkingRepository, times(1)).delete(existing);
        verify(coworkingCache, times(1)).remove(id);
    }

    @Test
    void deleteCoworking_WhenHasWorkspaces_ShouldThrowInvalidArgumentException() {
        // Arrange
        Long id = 1L;
        Coworking existing = new Coworking();
        existing.setId(id);
        Workspace workspace = new Workspace();
        existing.setWorkspaces(new HashSet<>(Arrays.asList(workspace)));

        when(coworkingCache.get(id)).thenReturn(existing);

        // Act & Assert
        assertThrows(InvalidArgumentException.class, () -> coworkingService.deleteCoworking(id));
        verify(coworkingRepository, never()).delete(any());
    }

    @Test
    void updateCoworking_WhenNameNotChanged_ShouldNotCheckNameUniqueness() {
        // Arrange
        Long id = 1L;
        Coworking existing = new Coworking();
        existing.setId(id);
        existing.setName("Same Name");
        existing.setAddress("Old Address");

        Coworking updated = new Coworking();
        updated.setName("Same Name"); // То же самое имя - не изменяется
        updated.setAddress("New Address");
        updated.setEmail("new@test.com");
        updated.setPhoneNumber("+9876543210");

        when(coworkingCache.get(id)).thenReturn(existing);
        when(coworkingRepository.existsByAddress(updated.getAddress())).thenReturn(false);
        when(coworkingRepository.save(any(Coworking.class))).thenReturn(existing);

        // Act
        Coworking result = coworkingService.updateCoworking(id, updated);

        // Assert
        assertNotNull(result);
        // Проверяем, что проверка уникальности имени НЕ вызывалась (т.к. имя не изменилось)
        verify(coworkingRepository, never()).existsByName(anyString());
        verify(coworkingRepository, times(1)).existsByAddress(updated.getAddress());
        verify(coworkingRepository, times(1)).save(existing);
    }
}