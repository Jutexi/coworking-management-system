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
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final WorkspaceRepository workspaceRepository;
    private final UserRepository userRepository;
    private final ReservationCache reservationCache;

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
        if (reservation != null) {
            return reservation;
        }

        reservation = reservationRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Reservation not found with id " + id));
        reservationCache.put(id, reservation);
        return reservation;
    }

    @Transactional
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    @Transactional
    public List<Reservation> getReservationsByUserEmail(String email) {
        if (!userRepository.existsByEmail(email)) {
            throw new ResourceNotFoundException("User with email " + email + " not found");
        }
        return reservationRepository.findByUserEmail(email);
    }

    // ReservationService.java
    @Transactional
    public List<Reservation> getReservationsByPeriod(LocalDate startDate, LocalDate endDate, Long coworkingId) {
        // Валидация дат
        if (endDate.isBefore(startDate)) {
            throw new InvalidArgumentException("End date must be same or after start date");
        }

        return reservationRepository.findReservationsByPeriodAndCoworking(startDate, endDate, coworkingId);
    }

    private void checkAvailability(Workspace workspace,
                                   LocalDate start, LocalDate end, Long excludeReservationId) {

        // 1) конец не может быть раньше начала (end < start) — равно допустимо (one-day)
        if (end.isBefore(start)) {
            throw new InvalidArgumentException("End date must be same or after start date");
        }

        // 2) длительность включительно (например, start==end => daysInclusive = 1)
        long daysInclusive = ChronoUnit.DAYS.between(start, end) + 1;

        // 3) правила по типу workspace
        if (workspace.getType() == WorkspaceType.OFFICE && daysInclusive < 7) {
            throw new InvalidArgumentException(
                    "Office reservations must be at least " + 7 + " days long");
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
                throw new AlreadyExistsException(
                        "Open space capacity exceeded for the selected dates");
            }
        } else {
            // для всех остальных capacity = 1
            if (!overlapping.isEmpty()) {
                throw new AlreadyExistsException(
                        "This workspace is already reserved for the selected period");
            }
        }

    }

    @Transactional
    public Reservation createReservation(Long workspaceId, Long userId, Reservation reservation) {
        Workspace workspace = workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Workspace not found with id " + workspaceId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with id " + userId));
        checkAvailability(workspace, reservation.getStartDate(),
                reservation.getEndDate(), null);
        reservation.setWorkspace(workspace);
        reservation.setUser(user);

        Reservation saved = reservationRepository.save(reservation);
        reservationCache.put(saved.getId(), saved);
        return saved;
    }

    @Transactional
    public Reservation updateReservation(Long id, Reservation updated) {
        Reservation existing = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reservation not found with id " + id));
        Workspace workspace = existing.getWorkspace();
        checkAvailability(workspace, updated.getStartDate(), updated.getEndDate(), id);
        existing.setStartDate(updated.getStartDate());
        existing.setEndDate(updated.getEndDate());
        existing.setComment(updated.getComment());

        Reservation saved = reservationRepository.save(existing);
        reservationCache.put(saved.getId(), saved);
        return saved;
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation existing = getReservationById(id);
        reservationRepository.delete(existing);
        reservationCache.remove(id);
    }
}
