package com.app.coworking.service;

import com.app.coworking.cache.WorkspaceCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.Coworking;
import com.app.coworking.model.Workspace;
import com.app.coworking.repository.CoworkingRepository;
import com.app.coworking.repository.WorkspaceRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WorkspaceService {

    private final WorkspaceRepository workspaceRepository;
    private final CoworkingRepository coworkingRepository;
    private final WorkspaceCache workspaceCache;

    public WorkspaceService(WorkspaceRepository workspaceRepository,
                            CoworkingRepository coworkingRepository,
                            WorkspaceCache workspaceCache) {
        this.workspaceRepository = workspaceRepository;
        this.coworkingRepository = coworkingRepository;
        this.workspaceCache = workspaceCache;
    }

    @Transactional
    public Workspace getWorkspaceById(Long id) {
        Workspace workspace = workspaceCache.get(id);
        if (workspace != null) return workspace;

        workspace = workspaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Workspace not found with id " + id));
        workspaceCache.put(id, workspace);
        return workspace;
    }

    @Transactional
    public List<Workspace> getAllWorkspaces() {
        return workspaceRepository.findAll();  // работа с кешем
    }

    @Transactional
    public Workspace createWorkspace(Long coworkingId, Workspace workspace) {
        Coworking coworking = coworkingRepository.findById(coworkingId)
                .orElseThrow(() -> new ResourceNotFoundException("Coworking not found with id " + coworkingId));

        // Привязываем к coworking
        workspace.setCoworking(coworking);

        // Проверка уникальности имени рабочего места в пределах этого коворкинга
        if (workspaceRepository.existsByNameAndCoworking_Id(workspace.getName(), coworkingId)) {
            throw new AlreadyExistsException("Workspace with this name already exists in the coworking");
        }

        Workspace saved = workspaceRepository.save(workspace);
        workspaceCache.put(saved.getId(), saved);
        return saved;
    }

    @Transactional
    public Workspace updateWorkspace(Long id, Workspace updatedWorkspace) {
        Workspace existing = getWorkspaceById(id);

        // Проверка уникальности имени
        if (!existing.getName().equals(updatedWorkspace.getName()) &&
                workspaceRepository.existsByNameAndCoworking_Id(updatedWorkspace.getName(),
                        existing.getCoworking().getId())) {
            throw new AlreadyExistsException("Workspace with this name already exists in the coworking");
        }

        existing.setName(updatedWorkspace.getName());
        existing.setDescription(updatedWorkspace.getDescription());
        existing.setType(updatedWorkspace.getType());
        existing.setCapacity(updatedWorkspace.getCapacity());

        Workspace saved = workspaceRepository.save(existing);
        workspaceCache.put(saved.getId(), saved);
        return saved;
    }

    @Transactional
    public void deleteWorkspace(Long id) {
        Workspace existing = getWorkspaceById(id);
        workspaceRepository.delete(existing);
        workspaceCache.remove(id);
    }
}
