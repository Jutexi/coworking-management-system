package com.app.coworking.service;

import com.app.coworking.cache.CoworkingCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.Coworking;
import com.app.coworking.repository.CoworkingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CoworkingServiceTest {

    @Mock
    private CoworkingRepository coworkingRepository;

    @Mock
    private CoworkingCache coworkingCache;

    @InjectMocks
    private CoworkingService coworkingService;

    private Coworking coworking;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        coworking = new Coworking(
                1L,
                "WeWork",
                "123 Main Street, City",
                "info@wework.com",
                "+1234567890",
                "Popular coworking space",
                new HashSet<>()
        );
    }

    // -------------------- getCoworkingById --------------------
    @Test
    void getCoworkingById_shouldReturnFromCache() {
        when(coworkingCache.get(1L)).thenReturn(coworking);

        Coworking result = coworkingService.getCoworkingById(1L);

        assertEquals("WeWork", result.getName());
        verify(coworkingRepository, never()).findById(anyLong());
    }

    @Test
    void getCoworkingById_shouldReturnFromRepositoryAndCacheIt() {
        when(coworkingCache.get(1L)).thenReturn(null);
        when(coworkingRepository.findById(1L)).thenReturn(Optional.of(coworking));

        Coworking result = coworkingService.getCoworkingById(1L);

        assertEquals("WeWork", result.getName());
        verify(coworkingCache).put(1L, coworking);
    }

    @Test
    void getCoworkingById_shouldThrowIfNotFound() {
        when(coworkingCache.get(1L)).thenReturn(null);
        when(coworkingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> coworkingService.getCoworkingById(1L));
    }

    // -------------------- getAllCoworkings --------------------
    @Test
    void getAllCoworkings_shouldReturnList() {
        when(coworkingRepository.findAll()).thenReturn(List.of(coworking));

        List<Coworking> result = coworkingService.getAllCoworkings();

        assertEquals(1, result.size());
        assertEquals("WeWork", result.get(0).getName());
    }

    // -------------------- createCoworking --------------------
    @Test
    void createCoworking_shouldSaveAndCache() {
        when(coworkingRepository.existsByName("WeWork")).thenReturn(false);
        when(coworkingRepository.existsByAddress("123 Main Street, City")).thenReturn(false);
        when(coworkingRepository.save(coworking)).thenReturn(coworking);

        Coworking result = coworkingService.createCoworking(coworking);

        assertEquals("WeWork", result.getName());
        verify(coworkingCache).put(1L, coworking);
    }

    @Test
    void createCoworking_shouldThrowIfNameExists() {
        when(coworkingRepository.existsByName("WeWork")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> coworkingService.createCoworking(coworking));
    }

    @Test
    void createCoworking_shouldThrowIfAddressExists() {
        when(coworkingRepository.existsByName("WeWork")).thenReturn(false);
        when(coworkingRepository.existsByAddress("123 Main Street, City")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> coworkingService.createCoworking(coworking));
    }

    // -------------------- updateCoworking --------------------
    @Test
    void updateCoworking_shouldUpdateAndCache() {
        Coworking updated = new Coworking(
                1L,
                "NewName",
                "New Address 99",
                "new@mail.com",
                "+987654321",
                "Updated description",
                new HashSet<>()
        );

        when(coworkingCache.get(1L)).thenReturn(coworking);
        when(coworkingRepository.findById(1L)).thenReturn(Optional.of(coworking));
        when(coworkingRepository.existsByName("NewName")).thenReturn(false);
        when(coworkingRepository.existsByAddress("New Address 99")).thenReturn(false);
        when(coworkingRepository.save(any())).thenReturn(updated);

        Coworking result = coworkingService.updateCoworking(1L, updated);

        assertEquals("NewName", result.getName());
        verify(coworkingCache).put(1L, updated);
    }

    @Test
    void updateCoworking_shouldThrowIfNewNameExists() {
        Coworking updated = new Coworking(
                1L,
                "Other",
                "123 Main Street, City",
                "mail@mail.com",
                "+000000000",
                "desc",
                new HashSet<>()
        );

        when(coworkingCache.get(1L)).thenReturn(coworking);
        when(coworkingRepository.findById(1L)).thenReturn(Optional.of(coworking));
        when(coworkingRepository.existsByName("Other")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> coworkingService.updateCoworking(1L, updated));
    }

    @Test
    void updateCoworking_shouldThrowIfNewAddressExists() {
        Coworking updated = new Coworking(
                1L,
                "WeWork",
                "Other Address",
                "mail@mail.com",
                "+000000000",
                "desc",
                new HashSet<>()
        );

        when(coworkingCache.get(1L)).thenReturn(coworking);
        when(coworkingRepository.findById(1L)).thenReturn(Optional.of(coworking));
        when(coworkingRepository.existsByAddress("Other Address")).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> coworkingService.updateCoworking(1L, updated));
    }

    // -------------------- deleteCoworking --------------------
    @Test
    void deleteCoworking_shouldRemoveFromRepoAndCache() {
        when(coworkingCache.get(1L)).thenReturn(coworking);
        when(coworkingRepository.findById(1L)).thenReturn(Optional.of(coworking));

        coworkingService.deleteCoworking(1L);

        verify(coworkingRepository).delete(coworking);
        verify(coworkingCache).remove(1L);
    }
}
