package com.app.coworking.controller;

import com.app.coworking.model.Reservation;
import com.app.coworking.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @Operation(summary = "Получить все бронирования",
            description = "Возвращает список всех бронирований")
    @GetMapping
    public ResponseEntity<List<Reservation>> getAll() {
        List<Reservation> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "Получить бронирование по ID",
            description = "Возвращает бронирование по его уникальному идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    @Operation(summary = "Создать бронирование",
            description = "Создает новое бронирование для указанного workspace и пользователя")
    @PostMapping("/workspace/{workspaceId}/user/{userId}")
    public ResponseEntity<Reservation> create(@PathVariable Long workspaceId,
                                              @PathVariable Long userId,
                                              @Valid @RequestBody Reservation reservation) {
        Reservation created = reservationService.createReservation(
                workspaceId, userId, reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Обновить бронирование",
            description = "Обновляет данные бронирования по его ID")
    @PutMapping("/{id}")
    public ResponseEntity<Reservation> update(@PathVariable Long id,
                                              @Valid @RequestBody Reservation reservation) {
        Reservation updated = reservationService.updateReservation(id, reservation);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить бронирование", description = "Удаляет бронирование по его ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Получить бронирования пользователя по email",
            description = "Возвращает все бронирования пользователя с указанным email")
    @GetMapping("/user")
    public ResponseEntity<List<Reservation>> getByUserEmail(@RequestParam String email) {
        List<Reservation> reservations = reservationService.getReservationsByUserEmail(email);
        return ResponseEntity.ok(reservations);
    }
}
