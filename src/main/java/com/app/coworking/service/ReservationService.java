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
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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
        if (reservation != null) return reservation;

        reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found with id " + id));
        reservationCache.put(id, reservation);
        return reservation;
    }

    @Transactional
    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();  // работа с кешем
    }

    @Transactional
    public List<Reservation> getReservationsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id " + userId);
        }
        return reservationRepository.findByUserId(userId);
    }

    @Transactional
    public Reservation createReservation(Long workspaceId, Long userId, Reservation reservation) {
        throw new AlreadyExistsException("Reservation does not work");
    }

    @Transactional
    public Reservation updateReservation(Long id, Reservation updated) {
        throw new AlreadyExistsException("Reservation updated does not work");
    }

    @Transactional
    public List<Reservation> createBulkReservations(List<Reservation> reservations) {
        throw new AlreadyExistsException("Reservations bulk does not work");
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation existing = getReservationById(id);
        reservationRepository.delete(existing);
        reservationCache.remove(id);
    }
}
