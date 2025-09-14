package com.app.coworking.Controller;


import com.app.coworking.model.Workspace;
import com.app.coworking.service.WorkspaceService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    @Operation(summary = "Получить все рабочие места", description = "Возвращает список всех рабочих мест")
    @GetMapping
    public ResponseEntity<List<Workspace>> getAll() {
        List<Workspace> workspaces = workspaceService.getAllWorkspaces();
        return ResponseEntity.ok(workspaces);
    }

    @Operation(summary = "Получить рабочее место по ID", description = "Возвращает рабочее место по его уникальному идентификатору")
    @GetMapping("/{id}")
    public ResponseEntity<Workspace> getById(@PathVariable Long id) {
        Workspace workspace = workspaceService.getWorkspaceById(id);
        return ResponseEntity.ok(workspace);
    }

    @Operation(summary = "Создать рабочее место в коворкинге", description = "Создает новое рабочее место в указанном коворкинге")
    @PostMapping("/coworking/{coworkingId}")
    public ResponseEntity<Workspace> create(@PathVariable Long coworkingId,
                                            @Valid @RequestBody Workspace workspace) {
        Workspace created = workspaceService.createWorkspace(coworkingId, workspace);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Обновить рабочее место", description = "Обновляет данные рабочего места по его ID")
    @PutMapping("/{id}")
    public ResponseEntity<Workspace> update(@PathVariable Long id,
                                            @Valid @RequestBody Workspace workspace) {
        Workspace updated = workspaceService.updateWorkspace(id, workspace);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "Удалить рабочее место", description = "Удаляет рабочее место по его ID")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workspaceService.deleteWorkspace(id);
        return ResponseEntity.noContent().build(); // возвращает статус 204 No Content
    }
}
