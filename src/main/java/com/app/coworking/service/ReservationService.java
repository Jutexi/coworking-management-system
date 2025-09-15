package com.app.coworking.service;

import com.app.coworking.cache.ReservationCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.exception.InvalidArgumentException;
import com.app.coworking.model.Reservation;
import com.app.coworking.model.User;
import com.app.coworking.model.Workspace;
import com.app.coworking.model.enums.WorkspaceType;
import com.app.coworking.repository.ReservationRepository;
import com.app.coworking.repository.UserRepository;
import com.app.coworking.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final ReservationCache reservationCache;

    private static final int MIN_OFFICE_DAYS = 7;

    public ReservationService(ReservationRepository reservationRepository,
                              WorkspaceRepository workspaceRepository,
                              UserRepository userRepository,
                              ReservationCache reservationCache) {
        this.reservationRepository = reservationRepository;
        this.workspaceRepository = workspaceRepository;
        this.userRepository = userRepository;
        this.reservationCache = reservationCache;
    }

    @Transactional
    public Reservation getReservationById(Long id) {
        Reservation reservation = reservationCache.get(id);
        if (reservation != null) return reservation;

        reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id " + id));
        reservationCache.put(id, reservation);
        return reservation;
    }

    @Transactional
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public List<Reservation> getReservationsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return reservationRepository.findByUserId(userId);
    }

    private void checkAvailability(Workspace workspace, User user,
                                   LocalDate start, LocalDate end, Long excludeReservationId) {

        // 1) конец не может быть раньше начала (end < start) — равно допустимо (one-day)
        if (end.isBefore(start)) {
            throw new InvalidArgumentException("End date must be same or after start date");
        }

        // 2) длительность включительно (например, start==end => daysInclusive = 1)
        long daysInclusive = ChronoUnit.DAYS.between(start, end) + 1;

        // 3) правила по типу workspace
        if (workspace.getType() == WorkspaceType.OFFICE) {
            final int MIN_OFFICE_DAYS = 7; // <- можно изменить на 30
            if (daysInclusive < MIN_OFFICE_DAYS) {
                throw new InvalidArgumentException("Office reservations must be at least " + MIN_OFFICE_DAYS + " days long");
            }
        }

        // 4) находим пересекающиеся бронирования
        List<Reservation> overlapping = new ArrayList<>(
                reservationRepository.findOverlappingReservations(workspace.getId(), start, end)
        );


// при update исключаем саму бронь
        if (excludeReservationId != null) {
            overlapping.removeIf(r -> r.getId().equals(excludeReservationId));
        }

// 5) capacity проверки
        if (workspace.getType() == WorkspaceType.OPEN_SPACE) {
            // для open space реально используем capacity
            if (overlapping.size() >= workspace.getCapacity()) {
                throw new AlreadyExistsException("Open space capacity exceeded for the selected dates");
            }
        } else {
            // для всех остальных capacity = 1
            if (!overlapping.isEmpty()) {
                throw new AlreadyExistsException("This workspace is already reserved for the selected period");
            }
        }

    }

    @Transactional
    public Reservation createReservation(Long workspaceId, Long userId, Reservation reservation) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id " + workspaceId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
        checkAvailability(workspace, user, reservation.getStartDate(), reservation.getEndDate(), null);
        reservation.setWorkspace(workspace);
        reservation.setUser(user);

        Reservation saved = reservationRepository.save(reservation);
        reservationCache.put(saved.getId(), saved);
        return saved;
    }

    @Transactional
    public Reservation updateReservation(Long id, Reservation updated) {
        Reservation existing = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id " + id));
        Workspace workspace = existing.getWorkspace();
        User user = existing.getUser();
        checkAvailability(workspace, user, updated.getStartDate(), updated.getEndDate(), id);
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setComment(updated.getComment());

        Reservation saved = reservationRepository.save(existing);
        reservationCache.put(saved.getId(), saved);
        return saved;
    }

    @Transactional
    public List<Reservation> createBulkReservations(List<Reservation> reservations) {
        return reservations.stream()
                .map(r -> createReservation(r.getWorkspaceId(), r.getUserId(), r))
                .toList();
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation existing = getReservationById(id);
        reservationRepository.delete(existing);
        reservationCache.remove(id);
    }
}
