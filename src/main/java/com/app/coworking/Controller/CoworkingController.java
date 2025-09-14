package com.app.coworking.Controller;

import com.app.coworking.model.Coworking;
import com.app.coworking.service.CoworkingService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coworkings")
@RequiredArgsConstructor
public class CoworkingController {

    private final CoworkingService coworkingService;

    @Operation(summary = "Получить все коворкинги", description = "Возвращает список всех коворкингов")
    @GetMapping
    public ResponseEntity<List<Coworking>> getAll() {
        List<Coworking> coworkings = coworkingService.getAllCoworkings();
        return ResponseEntity.ok(coworkings);
    }

    @Operation(summary = "Получить коворкинг по ID", description = "Возвращает коворкинг по его уникальному идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<Coworking> getById(@PathVariable Long id) {
        Coworking coworking = coworkingService.getCoworkingById(id);
        return ResponseEntity.ok(coworking);
    }

    @Operation(summary = "Создать новый коворкинг", description = "Принимает объект коворкинга и сохраняет его в базе")
    @PostMapping
    public ResponseEntity<Coworking> create(@Valid @RequestBody Coworking coworking) {
        Coworking created = coworkingService.createCoworking(coworking);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Обновить коворкинг", description = "Обновляет данные коворкинга по его ID")
    @PutMapping("/{id}")
    public ResponseEntity<Coworking> update(@PathVariable Long id, @Valid @RequestBody Coworking coworking) {
        Coworking updated = coworkingService.updateCoworking(id, coworking);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить коворкинг", description = "Удаляет коворкинг по его ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        coworkingService.deleteCoworking(id);
        return ResponseEntity.noContent().build(); // возвращает статус 204 No Content
    }
}


