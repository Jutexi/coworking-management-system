package com.app.coworking.service;

import com.app.coworking.cache.WorkspaceCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.Coworking;
import com.app.coworking.model.Workspace;
import com.app.coworking.model.enums.WorkspaceType;
import com.app.coworking.repository.CoworkingRepository;
import com.app.coworking.repository.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private CoworkingRepository coworkingRepository;

    @Mock
    private WorkspaceCache workspaceCache;

    @InjectMocks
    private WorkspaceService workspaceService;

    private Workspace workspace;
    private Coworking coworking;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        coworking = new Coworking(
                1L, "WeWork", "123 Main Street", "info@wework.com",
                "+1234567890", "Popular coworking", new HashSet<>()
        );

        workspace = new Workspace(
                1L,
                "Meeting Room A",
                WorkspaceType.MEETING_ROOM,
                10,
                "Spacious meeting room",
                coworking,
                new HashSet<>()
        );
    }

    // -------------------- getWorkspaceById --------------------
    @Test
    void getWorkspaceById_shouldReturnFromCache() {
        when(workspaceCache.get(1L)).thenReturn(workspace);

        Workspace result = workspaceService.getWorkspaceById(1L);

        assertEquals("Meeting Room A", result.getName());
        verify(workspaceRepository, never()).findById(anyLong());
    }

    @Test
    void getWorkspaceById_shouldReturnFromRepositoryAndCacheIt() {
        when(workspaceCache.get(1L)).thenReturn(null);
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));

        Workspace result = workspaceService.getWorkspaceById(1L);

        assertEquals("Meeting Room A", result.getName());
        verify(workspaceCache).put(1L, workspace);
    }

    @Test
    void getWorkspaceById_shouldThrowIfNotFound() {
        when(workspaceCache.get(1L)).thenReturn(null);
        when(workspaceRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> workspaceService.getWorkspaceById(1L));
    }

    // -------------------- getAllWorkspaces --------------------
    @Test
    void getAllWorkspaces_shouldReturnList() {
        when(workspaceRepository.findAll()).thenReturn(List.of(workspace));

        List<Workspace> result = workspaceService.getAllWorkspaces();

        assertEquals(1, result.size());
        assertEquals("Meeting Room A", result.get(0).getName());
    }

    // -------------------- createWorkspace --------------------
    @Test
    void createWorkspace_shouldSaveAndCache() {
        when(coworkingRepository.findById(1L)).thenReturn(Optional.of(coworking));
        when(workspaceRepository.existsByNameAndCoworkingId("Meeting Room A", 1L)).thenReturn(false);
        when(workspaceRepository.save(workspace)).thenReturn(workspace);

        Workspace result = workspaceService.createWorkspace(1L, workspace);

        assertEquals("Meeting Room A", result.getName());
        verify(workspaceCache).put(1L, workspace);
    }

    @Test
    void createWorkspace_shouldThrowIfCoworkingNotFound() {
        when(coworkingRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> workspaceService.createWorkspace(1L, workspace));
    }

    @Test
    void createWorkspace_shouldThrowIfNameExistsInCoworking() {
        when(coworkingRepository.findById(1L)).thenReturn(Optional.of(coworking));
        when(workspaceRepository.existsByNameAndCoworkingId("Meeting Room A", 1L)).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> workspaceService.createWorkspace(1L, workspace));
    }

    // -------------------- updateWorkspace --------------------
    @Test
    void updateWorkspace_shouldUpdateAndCache() {
        Workspace updated = new Workspace(
                1L,
                "Conference Room B",
                WorkspaceType.OPEN_SPACE,
                20,
                "Updated description",
                coworking,
                new HashSet<>()
        );

        when(workspaceCache.get(1L)).thenReturn(workspace);
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.existsByNameAndCoworkingId("Conference Room B", 1L)).thenReturn(false);
        when(workspaceRepository.save(any())).thenReturn(updated);

        Workspace result = workspaceService.updateWorkspace(1L, updated);

        assertEquals("Conference Room B", result.getName());
        verify(workspaceCache).put(1L, updated);
    }

    @Test
    void updateWorkspace_shouldThrowIfNewNameExists() {
        Workspace updated = new Workspace(
                1L,
                "Another Room",
                WorkspaceType.OFFICE,
                5,
                "Desc",
                coworking,
                new HashSet<>()
        );

        when(workspaceCache.get(1L)).thenReturn(workspace);
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));
        when(workspaceRepository.existsByNameAndCoworkingId("Another Room", 1L)).thenReturn(true);

        assertThrows(AlreadyExistsException.class,
                () -> workspaceService.updateWorkspace(1L, updated));
    }

    // -------------------- deleteWorkspace --------------------
    @Test
    void deleteWorkspace_shouldRemoveFromRepoAndCache() {
        when(workspaceCache.get(1L)).thenReturn(workspace);
        when(workspaceRepository.findById(1L)).thenReturn(Optional.of(workspace));

        workspaceService.deleteWorkspace(1L);

        verify(workspaceRepository).delete(workspace);
        verify(workspaceCache).remove(1L);
    }
}
