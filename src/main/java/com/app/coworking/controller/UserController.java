package com.app.coworking.controller;

import com.app.coworking.model.User;
import com.app.coworking.service.UserService;
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
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "Получить всех пользователей",
            description = "Возвращает список всех пользователей")
    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Получить пользователя по ID",
            description = "Возвращает пользователя по его уникальному ID")
    @GetMapping("/{id}")
    public ResponseEntity<User> getById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Создать пользователя",
            description = "Создает нового пользователя с указанными данными. "
                    + "Не принимает связанные бронирования в теле запроса")
    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User user) {
        User created = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Обновить пользователя",
            description = "Обновляет данные пользователя по его ID. "
                    + "Не принимает связанные рабочие места в теле запроса")
    @PutMapping("/{id}")
    public ResponseEntity<User> update(@PathVariable Long id, @Valid @RequestBody User user) {
        User updated = userService.updateUser(id, user);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить пользователя", description = "Удаляет пользователя по его ID. "
            + "Нельзя удалить пользователя с существующими бронированиями.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
