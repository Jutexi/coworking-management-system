package com.app.coworking.Controller;

import com.app.coworking.model.Reservation;
import com.app.coworking.service.ReservationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    // Получение всех бронирований
    @GetMapping
    public List<Reservation> getAll() {
        return reservationService.getAllReservations();
    }

    // Получение бронирования по id
    @GetMapping("/{id}")
    public Reservation getById(@PathVariable Long id) {
        return reservationService.getReservationById(id);
    }

    // Получение бронирований конкретного пользователя
    @GetMapping("/by-user/{userId}")
    public List<Reservation> getByUser(@PathVariable Long userId) {
        return reservationService.getReservationsByUser(userId);
    }


    // Создание бронирования: нужно указать workspaceId и userId
    @PostMapping("/workspace/{workspaceId}/user/{userId}")
    public ResponseEntity<Reservation> create(@PathVariable Long workspaceId,
                                              @PathVariable Long userId,
                                              @Valid @RequestBody Reservation reservation) {
        Reservation created = reservationService.createReservation(workspaceId, userId, reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Обновление бронирования
    @PutMapping("/{id}")
    public Reservation update(@PathVariable Long id,
                              @Valid @RequestBody Reservation reservation) {
        return reservationService.updateReservation(id, reservation);
    }

    // Удаление бронирования
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reservationService.deleteReservation(id);
    }
}