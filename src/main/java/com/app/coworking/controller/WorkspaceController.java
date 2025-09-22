package com.app.coworking.controller;


import com.app.coworking.model.Workspace;
import com.app.coworking.service.WorkspaceService;
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
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @Operation(summary = "Получить все рабочие места",
            description = "Возвращает список всех рабочих мест")
    @GetMapping
    public ResponseEntity<List<Workspace>> getAll() {
        List<Workspace> workspaces = workspaceService.getAllWorkspaces();
        return ResponseEntity.ok(workspaces);
    }

    @Operation(summary = "Получить рабочее место по ID",
            description = "Возвращает рабочее место по его уникальному идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<Workspace> getById(@PathVariable Long id) {
        Workspace workspace = workspaceService.getWorkspaceById(id);
        return ResponseEntity.ok(workspace);
    }

    @Operation(summary = "Создать рабочее место в коворкинге",
            description = "Создает новое рабочее место в указанном коворкинге. "
                    + "Не принимает связанные сущности в теле запроса.")
    @PostMapping("/coworking/{coworkingId}")
    public ResponseEntity<Workspace> create(@PathVariable Long coworkingId,
                                            @Valid @RequestBody Workspace workspace) {
        Workspace created = workspaceService.createWorkspace(coworkingId, workspace);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Обновить рабочее место",
            description = "Обновляет данные рабочего места по его ID. "
                    + "Не принимает связанные сущности в теле запроса.")
    @PutMapping("/{id}")
    public ResponseEntity<Workspace> update(@PathVariable Long id,
                                            @Valid @RequestBody Workspace workspace) {
        Workspace updated = workspaceService.updateWorkspace(id, workspace);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить рабочее место", description = "Удаляет рабочее место по его ID. "
            + "Нельзя удалить рабочее место с существующими бронированиями.")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workspaceService.deleteWorkspace(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Массовое создание рабочих мест в коворкинге",
            description = "Создает несколько рабочих мест в указанном коворкинге.")
    @PostMapping("/coworking/{coworkingId}/bulk")
    public ResponseEntity<List<Workspace>> createBulk(
            @PathVariable Long coworkingId,
            @Valid @RequestBody List<Workspace> workspaces) {
        List<Workspace> createdWorkspaces = workspaceService.createWorkspacesBulk(coworkingId, workspaces);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWorkspaces);
    }
}
