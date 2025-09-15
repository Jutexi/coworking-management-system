package com.app.coworking.service;

import com.app.coworking.cache.ReservationCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.Reservation;
import com.app.coworking.model.User;
import com.app.coworking.model.Workspace;
import com.app.coworking.model.enums.WorkspaceType;
import com.app.coworking.repository.ReservationRepository;
import com.app.coworking.repository.UserRepository;
import com.app.coworking.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationCache reservationCache;

    @InjectMocks
    private ReservationService reservationService;

    private Workspace workspaceOpen;
    private Workspace workspaceOffice;
    private User user;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        workspaceOpen = new Workspace();
        workspaceOpen.setId(1L);
        workspaceOpen.setType(WorkspaceType.OPEN_SPACE);
        workspaceOpen.setCapacity(2);

        workspaceOffice = new Workspace();
        workspaceOffice.setId(2L);
        workspaceOffice.setType(WorkspaceType.OFFICE);
        workspaceOffice.setCapacity(1);

        user = new User();
        user.setId(1L);

        reservation = new Reservation();
        reservation.setId(1L);
        reservation.setUser(user);
        reservation.setWorkspace(workspaceOpen);
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(1));
    }

    @Test
    void getReservationById_shouldReturnFromCache() {
        when(reservationCache.get(1L)).thenReturn(reservation);

        Reservation result = reservationService.getReservationById(1L);

        assertEquals(reservation, result);
        verify(reservationRepository, never()).findById(anyLong());
    }

    @Test
    void getReservationById_shouldReturnFromRepositoryAndCacheIt() {
        when(reservationCache.get(1L)).thenReturn(null);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        Reservation result = reservationService.getReservationById(1L);

        assertEquals(reservation, result);
        verify(reservationCache).put(1L, reservation);
    }

    @Test
    void getReservationById_shouldThrowIfNotFound() {
        when(reservationCache.get(1L)).thenReturn(null);
        when(reservationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.getReservationById(1L));
    }

    @Test
    void createReservation_shouldSaveAndCache() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspaceOpen));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any())).thenReturn(List.of());
        when(reservationRepository.save(any())).thenReturn(reservation);

        Reservation result = reservationService.createReservation(1L, 1L, reservation);

        assertEquals(reservation, result);
        verify(reservationCache).put(reservation.getId(), reservation);
    }

    @Test
    void createReservation_shouldThrowIfCapacityExceeded() {
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspaceOpen));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        // Уже 2 брони для open space (capacity=2)
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any()))
                .thenReturn(List.of(new Reservation(), new Reservation()));

        assertThrows(AlreadyExistsException.class,
                () -> reservationService.createReservation(1L, 1L, reservation));
    }

    @Test
    void updateReservation_shouldUpdateAndCache() {
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any())).thenReturn(List.of());
        when(reservationRepository.save(any())).thenReturn(reservation);

        reservation.setComment("Updated");
        Reservation updated = reservationService.updateReservation(1L, reservation);

        assertEquals("Updated", updated.getComment());
        verify(reservationCache).put(reservation.getId(), reservation);
    }

    @Test
    void deleteReservation_shouldRemoveFromRepoAndCache() {
        when(reservationCache.get(1L)).thenReturn(reservation);
        when(reservationRepository.findById(1L)).thenReturn(Optional.of(reservation));

        reservationService.deleteReservation(1L);

        verify(reservationRepository).delete(reservation);
        verify(reservationCache).remove(1L);
    }

    @Test
    void getAllReservations_shouldReturnList() {
        when(reservationRepository.findAll()).thenReturn(List.of(reservation));

        List<Reservation> result = reservationService.getAllReservations();

        assertEquals(1, result.size());
    }

    @Test
    void getReservationsByUser_shouldReturnList() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(reservationRepository.findByUserId(1L)).thenReturn(List.of(reservation));

        List<Reservation> result = reservationService.getReservationsByUser(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getReservationsByUser_shouldThrowIfUserNotFound() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.getReservationsByUser(1L));
    }

    @Test
    void createReservation_endBeforeStart_shouldThrow() {
        reservation.setStartDate(LocalDate.now().plusDays(2));
        reservation.setEndDate(LocalDate.now().plusDays(1));

        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspaceOpen));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(1L, 1L, reservation));
    }

    @Test
    void createReservation_officeTooShort_shouldThrow() {
        workspaceOffice.setType(WorkspaceType.OFFICE);
        reservation.setWorkspace(workspaceOffice);
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(3));

        when(workspaceRepository.findById(2L)).thenReturn(Optional.of(workspaceOffice));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any()))
                .thenReturn(List.of());

        assertThrows(RuntimeException.class,
                () -> reservationService.createReservation(2L, 1L, reservation));
    }

    @Test
    void createReservation_officeValid_shouldPass() {
        workspaceOffice.setType(WorkspaceType.OFFICE);
        reservation.setWorkspace(workspaceOffice);
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(7));

        when(workspaceRepository.findById(2L)).thenReturn(Optional.of(workspaceOffice));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any()))
                .thenReturn(List.of());
        when(reservationRepository.save(any())).thenReturn(reservation);

        Reservation result = reservationService.createReservation(2L, 1L, reservation);

        assertEquals(reservation, result);
    }

    @Test
    void createReservation_openSpaceCapacityExceeded_shouldThrow() {
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(1));
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspaceOpen));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any()))
                .thenReturn(List.of(new Reservation(), new Reservation())); // capacity=2

        assertThrows(AlreadyExistsException.class,
                () -> reservationService.createReservation(1L, 1L, reservation));
    }

    @Test
    void updateReservation_excludeSelf_overlap_shouldPass() {
        Reservation existing = new Reservation();
        existing.setId(1L);
        existing.setWorkspace(workspaceOpen);
        existing.setUser(user);
        existing.setStartDate(LocalDate.now().plusDays(1));
        existing.setEndDate(LocalDate.now().plusDays(1));

        Reservation updated = new Reservation();
        updated.setStartDate(LocalDate.now().plusDays(1));
        updated.setEndDate(LocalDate.now().plusDays(1));

        when(reservationRepository.findById(1L)).thenReturn(Optional.of(existing));
        // возвращаем immutable список с самим объектом, чтобы проверить excludeReservationId
        when(reservationRepository.findOverlappingReservations(anyLong(), any(), any()))
                .thenReturn(List.of(existing));
        when(reservationRepository.save(any())).thenReturn(existing);

        Reservation result = reservationService.updateReservation(1L, updated);
        assertEquals(existing, result);
    }

}
