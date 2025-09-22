package com.app.coworking.service;

import com.app.coworking.cache.ReservationCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.InvalidArgumentException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.Reservation;
import com.app.coworking.model.User;
import com.app.coworking.model.Workspace;
import com.app.coworking.model.enums.WorkspaceType;
import com.app.coworking.repository.ReservationRepository;
import com.app.coworking.repository.UserRepository;
import com.app.coworking.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
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

    @Test
    void getReservationById_WhenExistsInCache_ShouldReturnCachedReservation() {
        // Arrange
        Long id = 1L;
        Reservation cachedReservation = new Reservation();
        cachedReservation.setId(id);
        when(reservationCache.get(id)).thenReturn(cachedReservation);

        // Act
        Reservation result = reservationService.getReservationById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(reservationCache, times(1)).get(id);
        verify(reservationRepository, never()).findById(any());
    }

    @Test
    void getReservationById_WhenNotInCacheButExistsInDb_ShouldReturnAndCacheReservation() {
        // Arrange
        Long id = 1L;
        Reservation dbReservation = new Reservation();
        dbReservation.setId(id);
        when(reservationCache.get(id)).thenReturn(null);
        when(reservationRepository.findById(id)).thenReturn(Optional.of(dbReservation));

        // Act
        Reservation result = reservationService.getReservationById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(reservationCache, times(1)).get(id);
        verify(reservationRepository, times(1)).findById(id);
        verify(reservationCache, times(1)).put(id, dbReservation);
    }

    @Test
    void getReservationById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long id = 999L;
        when(reservationCache.get(id)).thenReturn(null);
        when(reservationRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> reservationService.getReservationById(id));
        verify(reservationCache, times(1)).get(id);
        verify(reservationRepository, times(1)).findById(id);
    }

    @Test
    void getAllReservations_ShouldReturnList() {
        // Arrange
        Reservation reservation1 = new Reservation();
        Reservation reservation2 = new Reservation();
        List<Reservation> expectedList = Arrays.asList(reservation1, reservation2);
        when(reservationRepository.findAll()).thenReturn(expectedList);

        // Act
        List<Reservation> result = reservationService.getAllReservations();

        // Assert
        assertEquals(2, result.size());
        verify(reservationRepository, times(1)).findAll();
    }

    @Test
    void getReservationsByUserEmail_WhenUserExists_ShouldReturnReservations() {
        // Arrange
        String email = "user@test.com";
        Reservation reservation1 = new Reservation();
        Reservation reservation2 = new Reservation();
        List<Reservation> expectedList = Arrays.asList(reservation1, reservation2);

        when(userRepository.existsByEmail(email)).thenReturn(true);
        when(reservationRepository.findByUserEmail(email)).thenReturn(expectedList);

        // Act
        List<Reservation> result = reservationService.getReservationsByUserEmail(email);

        // Assert
        assertEquals(2, result.size());
        verify(userRepository, times(1)).existsByEmail(email);
        verify(reservationRepository, times(1)).findByUserEmail(email);
    }

    @Test
    void getReservationsByUserEmail_WhenUserNotExists_ShouldThrowResourceNotFoundException() {
        // Arrange
        String email = "nonexistent@test.com";
        when(userRepository.existsByEmail(email)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.getReservationsByUserEmail(email));
        verify(reservationRepository, never()).findByUserEmail(any());
    }

    @Test
    void getReservationsByPeriod_WhenValidDates_ShouldReturnReservations() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(5);
        Long coworkingId = 1L;

        Reservation reservation1 = new Reservation();
        Reservation reservation2 = new Reservation();
        List<Reservation> expectedList = Arrays.asList(reservation1, reservation2);

        when(reservationRepository.findReservationsByPeriodAndCoworking(startDate, endDate, coworkingId))
                .thenReturn(expectedList);

        // Act
        List<Reservation> result = reservationService.getReservationsByPeriod(startDate, endDate, coworkingId);

        // Assert
        assertEquals(2, result.size());
        verify(reservationRepository, times(1))
                .findReservationsByPeriodAndCoworking(startDate, endDate, coworkingId);
    }

    @Test
    void getReservationsByPeriod_WhenEndDateBeforeStartDate_ShouldThrowInvalidArgumentException() {
        // Arrange
        LocalDate startDate = LocalDate.now().plusDays(5);
        LocalDate endDate = LocalDate.now().plusDays(1);
        Long coworkingId = 1L;

        // Act & Assert
        assertThrows(InvalidArgumentException.class,
                () -> reservationService.getReservationsByPeriod(startDate, endDate, coworkingId));
        verify(reservationRepository, never()).findReservationsByPeriodAndCoworking(any(), any(), any());
    }

    @Test
    void createReservation_WhenValidOpenSpace_ShouldCreateReservation() {
        // Arrange
        Long workspaceId = 1L;
        Long userId = 1L;

        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);
        workspace.setType(WorkspaceType.OPEN_SPACE);
        workspace.setCapacity(3);

        User user = new User();
        user.setId(userId);

        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(3));

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.findOverlappingReservations(eq(workspaceId), any(), any()))
                .thenReturn(Arrays.asList()); // Нет пересекающихся бронирований
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // Act
        Reservation result = reservationService.createReservation(workspaceId, userId, reservation);

        // Assert
        assertNotNull(result);
        assertEquals(workspace, result.getWorkspace());
        assertEquals(user, result.getUser());
        verify(reservationRepository, times(1)).save(reservation);
        verify(reservationCache, times(1)).put(any(), eq(reservation));
    }

    @Test
    void createReservation_WhenWorkspaceNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long workspaceId = 999L;
        Long userId = 1L;
        Reservation reservation = new Reservation();

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.createReservation(workspaceId, userId, reservation));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long workspaceId = 1L;
        Long userId = 999L;
        Reservation reservation = new Reservation();

        Workspace workspace = new Workspace();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.createReservation(workspaceId, userId, reservation));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_WhenOfficeReservationLessThan7Days_ShouldThrowInvalidArgumentException() {
        // Arrange
        Long workspaceId = 1L;
        Long userId = 1L;

        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);
        workspace.setType(WorkspaceType.OFFICE);

        User user = new User();
        user.setId(userId);

        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(3)); // Всего 3 дня

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(InvalidArgumentException.class,
                () -> reservationService.createReservation(workspaceId, userId, reservation));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_WhenMeetingRoomAlreadyReserved_ShouldThrowAlreadyExistsException() {
        // Arrange
        Long workspaceId = 1L;
        Long userId = 1L;

        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);
        workspace.setType(WorkspaceType.MEETING_ROOM);
        workspace.setCapacity(1);

        User user = new User();
        user.setId(userId);

        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(3));

        Reservation existingReservation = new Reservation();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.findOverlappingReservations(eq(workspaceId), any(), any()))
                .thenReturn(Arrays.asList(existingReservation)); // Уже есть бронирование

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> reservationService.createReservation(workspaceId, userId, reservation));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_WhenOpenSpaceCapacityExceeded_ShouldThrowAlreadyExistsException() {
        // Arrange
        Long workspaceId = 1L;
        Long userId = 1L;

        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);
        workspace.setType(WorkspaceType.OPEN_SPACE);
        workspace.setCapacity(2); // Максимум 2 бронирования

        User user = new User();
        user.setId(userId);

        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(3));

        Reservation existingReservation1 = new Reservation();
        Reservation existingReservation2 = new Reservation();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.findOverlappingReservations(eq(workspaceId), any(), any()))
                .thenReturn(Arrays.asList(existingReservation1, existingReservation2)); // Уже 2 бронирования

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> reservationService.createReservation(workspaceId, userId, reservation));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void createReservation_WhenEndDateBeforeStartDate_ShouldThrowInvalidArgumentException() {
        // Arrange
        Long workspaceId = 1L;
        Long userId = 1L;

        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);
        workspace.setType(WorkspaceType.OPEN_SPACE);

        User user = new User();
        user.setId(userId);

        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDate.now().plusDays(5));
        reservation.setEndDate(LocalDate.now().plusDays(1)); // Конец раньше начала

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Act & Assert
        assertThrows(InvalidArgumentException.class,
                () -> reservationService.createReservation(workspaceId, userId, reservation));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void updateReservation_WhenValidData_ShouldUpdateReservation() {
        // Arrange
        Long reservationId = 1L;

        Workspace workspace = new Workspace();
        workspace.setId(1L);
        workspace.setType(WorkspaceType.FIXED_DESK);
        workspace.setCapacity(1);

        Reservation existing = new Reservation();
        existing.setId(reservationId);
        existing.setWorkspace(workspace);
        existing.setStartDate(LocalDate.now().plusDays(1));
        existing.setEndDate(LocalDate.now().plusDays(3));

        Reservation updated = new Reservation();
        updated.setStartDate(LocalDate.now().plusDays(5));
        updated.setEndDate(LocalDate.now().plusDays(7));
        updated.setComment("Updated comment");

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existing));
        when(reservationRepository.findOverlappingReservations(eq(workspace.getId()), any(), any()))
                .thenReturn(Arrays.asList()); // Нет пересекающихся бронирований
        when(reservationRepository.save(any(Reservation.class))).thenReturn(existing);

        // Act
        Reservation result = reservationService.updateReservation(reservationId, updated);

        // Assert
        assertNotNull(result);
        assertEquals(updated.getStartDate(), existing.getStartDate());
        assertEquals(updated.getEndDate(), existing.getEndDate());
        assertEquals(updated.getComment(), existing.getComment());
        verify(reservationRepository, times(1)).save(existing);
        verify(reservationCache, times(1)).put(reservationId, existing);
    }

    @Test
    void updateReservation_WhenReservationNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long reservationId = 999L;
        Reservation updated = new Reservation();

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.updateReservation(reservationId, updated));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void updateReservation_WhenNewDatesConflict_ShouldThrowAlreadyExistsException() {
        // Arrange
        Long reservationId = 1L;

        Workspace workspace = new Workspace();
        workspace.setId(1L);
        workspace.setType(WorkspaceType.MEETING_ROOM);

        Reservation existing = new Reservation();
        existing.setId(reservationId);
        existing.setWorkspace(workspace);

        Reservation updated = new Reservation();
        updated.setStartDate(LocalDate.now().plusDays(5));
        updated.setEndDate(LocalDate.now().plusDays(7));

        Reservation conflictingReservation = new Reservation();
        conflictingReservation.setId(2L);

        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existing));
        when(reservationRepository.findOverlappingReservations(eq(workspace.getId()), any(), any()))
                .thenReturn(Arrays.asList(conflictingReservation)); // Есть конфликтующее бронирование

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> reservationService.updateReservation(reservationId, updated));
        verify(reservationRepository, never()).save(any());
    }

    @Test
    void updateReservation_WhenUpdatingOwnReservation_ShouldExcludeCurrentReservationFromOverlapCheck() {
        // Arrange
        Long reservationId = 1L;

        Workspace workspace = new Workspace();
        workspace.setId(1L);
        workspace.setType(WorkspaceType.MEETING_ROOM);

        Reservation existing = new Reservation();
        existing.setId(reservationId);
        existing.setWorkspace(workspace);

        Reservation updated = new Reservation();
        updated.setStartDate(LocalDate.now().plusDays(5));
        updated.setEndDate(LocalDate.now().plusDays(7));

        // Только текущее бронирование (должно быть исключено из проверки)
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.of(existing));
        when(reservationRepository.findOverlappingReservations(eq(workspace.getId()), any(), any()))
                .thenReturn(Arrays.asList(existing)); // Возвращает само себя

        when(reservationRepository.save(any(Reservation.class))).thenReturn(existing);

        // Act
        Reservation result = reservationService.updateReservation(reservationId, updated);

        // Assert
        assertNotNull(result);
        // Должно пройти успешно, т.к. текущее бронирование исключается из проверки
        verify(reservationRepository, times(1)).save(existing);
    }

    @Test
    void deleteReservation_WhenExists_ShouldDeleteAndRemoveFromCache() {
        // Arrange
        Long reservationId = 1L;
        Reservation existing = new Reservation();
        existing.setId(reservationId);

        when(reservationCache.get(reservationId)).thenReturn(existing);

        // Act
        reservationService.deleteReservation(reservationId);

        // Assert
        verify(reservationRepository, times(1)).delete(existing);
        verify(reservationCache, times(1)).remove(reservationId);
    }

    @Test
    void deleteReservation_WhenNotExists_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long reservationId = 999L;
        when(reservationCache.get(reservationId)).thenReturn(null);
        when(reservationRepository.findById(reservationId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> reservationService.deleteReservation(reservationId));
        verify(reservationRepository, never()).delete(any());
    }

    @Test
    void createReservation_WhenOneDayReservation_ShouldCreateSuccessfully() {
        // Arrange
        Long workspaceId = 1L;
        Long userId = 1L;

        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);
        workspace.setType(WorkspaceType.OPEN_SPACE);
        workspace.setCapacity(3);

        User user = new User();
        user.setId(userId);

        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(1)); // Однодневное бронирование

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.findOverlappingReservations(eq(workspaceId), any(), any()))
                .thenReturn(Arrays.asList());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // Act
        Reservation result = reservationService.createReservation(workspaceId, userId, reservation);

        // Assert
        assertNotNull(result);
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    void createReservation_WhenOfficeReservationExactly7Days_ShouldCreateSuccessfully() {
        // Arrange
        Long workspaceId = 1L;
        Long userId = 1L;

        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);
        workspace.setType(WorkspaceType.OFFICE);

        User user = new User();
        user.setId(userId);

        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(7)); // Ровно 7 дней

        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.findOverlappingReservations(eq(workspaceId), any(), any()))
                .thenReturn(Arrays.asList());
        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // Act
        Reservation result = reservationService.createReservation(workspaceId, userId, reservation);

        // Assert
        assertNotNull(result);
        verify(reservationRepository, times(1)).save(reservation);
    }

    @Test
    void createReservation_WhenOpenSpaceWithAvailableCapacity_ShouldCreateSuccessfully() {
        // Arrange
        Long workspaceId = 1L;
        Long userId = 1L;

        Workspace workspace = new Workspace();
        workspace.setId(workspaceId);
        workspace.setType(WorkspaceType.OPEN_SPACE);
        workspace.setCapacity(3);

        User user = new User();
        user.setId(userId);

        Reservation reservation = new Reservation();
        reservation.setStartDate(LocalDate.now().plusDays(1));
        reservation.setEndDate(LocalDate.now().plusDays(3));

        Reservation existingReservation = new Reservation();
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(reservationRepository.findOverlappingReservations(eq(workspaceId), any(), any()))
                .thenReturn(Arrays.asList(existingReservation)); // 1 существующее, capacity=3

        when(reservationRepository.save(any(Reservation.class))).thenReturn(reservation);

        // Act
        Reservation result = reservationService.createReservation(workspaceId, userId, reservation);

        // Assert
        assertNotNull(result);
        verify(reservationRepository, times(1)).save(reservation);
    }
}