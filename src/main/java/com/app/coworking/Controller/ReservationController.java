package com.app.coworking.Controller;

import com.app.coworking.model.Reservation;
import com.app.coworking.service.ReservationService;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "Получить все бронирования", description = "Возвращает список всех бронирований")
    @GetMapping
    public ResponseEntity<List<Reservation>> getAll() {
        List<Reservation> reservations = reservationService.getAllReservations();
        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "Получить бронирование по ID", description = "Возвращает бронирование по его уникальному идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getById(@PathVariable Long id) {
        Reservation reservation = reservationService.getReservationById(id);
        return ResponseEntity.ok(reservation);
    }

    @Operation(summary = "Получить бронирования пользователя", description = "Возвращает список бронирований конкретного пользователя по userId")
    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<Reservation>> getByUser(@PathVariable Long userId) {
        List<Reservation> reservations = reservationService.getReservationsByUser(userId);
        return ResponseEntity.ok(reservations);
    }

    @Operation(summary = "Создать бронирование", description = "Создает новое бронирование для указанного workspace и пользователя")
    @PostMapping("/workspace/{workspaceId}/user/{userId}")
    public ResponseEntity<Reservation> create(@PathVariable Long workspaceId,
                                              @PathVariable Long userId,
                                              @Valid @RequestBody Reservation reservation) {
        Reservation created = reservationService.createReservation(workspaceId, userId, reservation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Обновить бронирование", description = "Обновляет данные бронирования по его ID")
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
        return ResponseEntity.noContent().build(); // статус 204 No Content
    }
}
