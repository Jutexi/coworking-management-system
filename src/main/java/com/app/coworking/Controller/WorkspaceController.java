package com.app.coworking.Controller;


import com.app.coworking.model.Workspace;
import com.app.coworking.service.WorkspaceService;
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

    // Получение всех рабочих мест
    @GetMapping
    public List<Workspace> getAll() {
        return workspaceService.getAllWorkspaces();
    }

    // Получение рабочего места по id
    @GetMapping("/{id}")
    public Workspace getById(@PathVariable Long id) {
        return workspaceService.getWorkspaceById(id);
    }

    // Создание рабочего места в коворкинге (coworkingId указываем в URL)
    @PostMapping("/coworking/{coworkingId}")
    public ResponseEntity<Workspace> create(@PathVariable Long coworkingId,
                                            @Valid @RequestBody Workspace workspace) {
        Workspace created = workspaceService.createWorkspace(coworkingId, workspace);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Обновление рабочего места
    @PutMapping("/{id}")
    public Workspace update(@PathVariable Long id,
                            @Valid @RequestBody Workspace workspace) {
        return workspaceService.updateWorkspace(id, workspace);
    }

    // Удаление рабочего места
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        workspaceService.deleteWorkspace(id);
    }
}