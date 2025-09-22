
package com.app.coworking.service;

import com.app.coworking.cache.WorkspaceCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.InvalidArgumentException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.Coworking;
import com.app.coworking.model.Reservation;
import com.app.coworking.model.Workspace;
import com.app.coworking.model.enums.WorkspaceType;
import com.app.coworking.repository.CoworkingRepository;
import com.app.coworking.repository.WorkspaceRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private CoworkingRepository coworkingRepository;

    @Mock
    private WorkspaceCache workspaceCache;

    @InjectMocks
    private WorkspaceService workspaceService;

    @Test
    void getWorkspaceById_WhenExistsInCache_ShouldReturnCachedWorkspace() {
        // Arrange
        Long id = 1L;
        Workspace cachedWorkspace = new Workspace();
        cachedWorkspace.setId(id);
        when(workspaceCache.get(id)).thenReturn(cachedWorkspace);

        // Act
        Workspace result = workspaceService.getWorkspaceById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(workspaceCache, times(1)).get(id);
        verify(workspaceRepository, never()).findById(any());
    }

    @Test
    void getWorkspaceById_WhenNotInCacheButExistsInDb_ShouldReturnAndCacheWorkspace() {
        // Arrange
        Long id = 1L;
        Workspace dbWorkspace = new Workspace();
        dbWorkspace.setId(id);
        when(workspaceCache.get(id)).thenReturn(null);
        when(workspaceRepository.findById(id)).thenReturn(Optional.of(dbWorkspace));

        // Act
        Workspace result = workspaceService.getWorkspaceById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(workspaceCache, times(1)).get(id);
        verify(workspaceRepository, times(1)).findById(id);
        verify(workspaceCache, times(1)).put(id, dbWorkspace);
    }

    @Test
    void getWorkspaceById_WhenNotExists_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long id = 999L;
        when(workspaceCache.get(id)).thenReturn(null);
        when(workspaceRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> workspaceService.getWorkspaceById(id));
        verify(workspaceCache, times(1)).get(id);
        verify(workspaceRepository, times(1)).findById(id);
    }

    @Test
    void getAllWorkspaces_ShouldReturnList() {
        // Arrange
        Workspace workspace1 = new Workspace();
        Workspace workspace2 = new Workspace();
        List<Workspace> expectedList = Arrays.asList(workspace1, workspace2);
        when(workspaceRepository.findAll()).thenReturn(expectedList);

        // Act
        List<Workspace> result = workspaceService.getAllWorkspaces();

        // Assert
        assertEquals(2, result.size());
        verify(workspaceRepository, times(1)).findAll();
    }

    @Test
    void createWorkspace_WhenValidData_ShouldSaveAndCache() {
        // Arrange
        Long coworkingId = 1L;
        Coworking coworking = new Coworking();
        coworking.setId(coworkingId);

        Workspace workspace = new Workspace();
        workspace.setName("Test Workspace");
        workspace.setType(WorkspaceType.OPEN_SPACE);
        workspace.setCapacity(10);

        when(coworkingRepository.findById(coworkingId)).thenReturn(Optional.of(coworking));
        when(workspaceRepository.existsByNameAndCoworkingId(workspace.getName(), coworkingId)).thenReturn(false);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(workspace);

        // Act
        Workspace result = workspaceService.createWorkspace(coworkingId, workspace);

        // Assert
        assertNotNull(result);
        assertEquals(coworking, result.getCoworking());
        verify(coworkingRepository, times(1)).findById(coworkingId);
        verify(workspaceRepository, times(1)).existsByNameAndCoworkingId(workspace.getName(), coworkingId);
        verify(workspaceRepository, times(1)).save(workspace);
        verify(workspaceCache, times(1)).put(any(), eq(workspace));
    }

    @Test
    void createWorkspace_WhenCoworkingNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long coworkingId = 999L;
        Workspace workspace = new Workspace();
        when(coworkingRepository.findById(coworkingId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> workspaceService.createWorkspace(coworkingId, workspace));
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void createWorkspace_WhenReservationsProvided_ShouldThrowInvalidArgumentException() {
        // Arrange
        Long coworkingId = 1L;
        Workspace workspace = new Workspace();
        workspace.setReservations(new HashSet<>(Arrays.asList(new Reservation())));

        // Act & Assert
        assertThrows(InvalidArgumentException.class,
                () -> workspaceService.createWorkspace(coworkingId, workspace));
        verify(coworkingRepository, never()).findById(any());
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void createWorkspace_WhenCoworkingInRequestBody_ShouldThrowInvalidArgumentException() {
        // Arrange
        Long coworkingId = 1L;
        Workspace workspace = new Workspace();
        workspace.setCoworking(new Coworking());

        // Act & Assert
        assertThrows(InvalidArgumentException.class,
                () -> workspaceService.createWorkspace(coworkingId, workspace));
        verify(coworkingRepository, never()).findById(any());
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void createWorkspace_WhenNameExistsInSameCoworking_ShouldThrowAlreadyExistsException() {
        // Arrange
        Long coworkingId = 1L;
        Coworking coworking = new Coworking();
        coworking.setId(coworkingId);

        Workspace workspace = new Workspace();
        workspace.setName("Existing Workspace");

        when(coworkingRepository.findById(coworkingId)).thenReturn(Optional.of(coworking));
        when(workspaceRepository.existsByNameAndCoworkingId(workspace.getName(), coworkingId)).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> workspaceService.createWorkspace(coworkingId, workspace));
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void createWorkspacesBulk_ShouldCreateMultipleWorkspaces() {
        // Arrange
        Long coworkingId = 1L;
        Workspace workspace1 = new Workspace();
        workspace1.setName("Workspace 1");
        Workspace workspace2 = new Workspace();
        workspace2.setName("Workspace 2");

        List<Workspace> workspaces = Arrays.asList(workspace1, workspace2);

        when(coworkingRepository.findById(coworkingId)).thenReturn(Optional.of(new Coworking()));
        when(workspaceRepository.existsByNameAndCoworkingId(anyString(), eq(coworkingId))).thenReturn(false);
        when(workspaceRepository.save(any(Workspace.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        List<Workspace> result = workspaceService.createWorkspacesBulk(coworkingId, workspaces);

        // Assert
        assertEquals(2, result.size());
        verify(workspaceRepository, times(2)).save(any(Workspace.class));
        verify(workspaceCache, times(2)).put(any(), any());
    }

    @Test
    void updateWorkspace_WhenValidData_ShouldUpdateAndCache() {
        // Arrange
        Long id = 1L;
        Coworking coworking = new Coworking();
        coworking.setId(1L);

        Workspace existing = new Workspace();
        existing.setId(id);
        existing.setName("Old Name");
        existing.setCoworking(coworking);

        Workspace updated = new Workspace();
        updated.setName("New Name");
        updated.setType(WorkspaceType.MEETING_ROOM);
        updated.setCapacity(5);
        updated.setDescription("Updated Description");

        when(workspaceCache.get(id)).thenReturn(existing);
        when(workspaceRepository.existsByNameAndCoworkingId(updated.getName(), coworking.getId())).thenReturn(false);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(existing);

        // Act
        Workspace result = workspaceService.updateWorkspace(id, updated);

        // Assert
        assertNotNull(result);
        assertEquals(updated.getName(), existing.getName());
        assertEquals(updated.getType(), existing.getType());
        assertEquals(updated.getCapacity(), existing.getCapacity());
        assertEquals(updated.getDescription(), existing.getDescription());
        verify(workspaceRepository, times(1)).save(existing);
        verify(workspaceCache, times(1)).put(id, existing);
    }

    @Test
    void updateWorkspace_WhenReservationsProvided_ShouldThrowInvalidArgumentException() {
        // Arrange
        Long id = 1L;
        Workspace updated = new Workspace();
        updated.setReservations(new HashSet<>(Arrays.asList(new Reservation())));

        // Act & Assert
        assertThrows(InvalidArgumentException.class,
                () -> workspaceService.updateWorkspace(id, updated));
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void updateWorkspace_WhenCoworkingProvided_ShouldThrowInvalidArgumentException() {
        // Arrange
        Long id = 1L;
        Workspace updated = new Workspace();
        updated.setCoworking(new Coworking());

        // Act & Assert
        assertThrows(InvalidArgumentException.class,
                () -> workspaceService.updateWorkspace(id, updated));
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void updateWorkspace_WhenNameExistsForOtherWorkspaceInSameCoworking_ShouldThrowAlreadyExistsException() {
        // Arrange
        Long id = 1L;
        Coworking coworking = new Coworking();
        coworking.setId(1L);

        Workspace existing = new Workspace();
        existing.setId(id);
        existing.setName("Old Name");
        existing.setCoworking(coworking);

        Workspace updated = new Workspace();
        updated.setName("Existing Name");

        when(workspaceCache.get(id)).thenReturn(existing);
        when(workspaceRepository.existsByNameAndCoworkingId(updated.getName(), coworking.getId())).thenReturn(true);

        // Act & Assert
        assertThrows(AlreadyExistsException.class,
                () -> workspaceService.updateWorkspace(id, updated));
        verify(workspaceRepository, never()).save(any());
    }

    @Test
    void updateWorkspace_WhenSameName_ShouldUpdateSuccessfully() {
        // Arrange
        Long id = 1L;
        Coworking coworking = new Coworking();
        coworking.setId(1L);

        Workspace existing = new Workspace();
        existing.setId(id);
        existing.setName("Same Name");
        existing.setCoworking(coworking);

        Workspace updated = new Workspace();
        updated.setName("Same Name"); // То же самое имя
        updated.setType(WorkspaceType.FIXED_DESK);
        updated.setCapacity(1);

        when(workspaceCache.get(id)).thenReturn(existing);
        when(workspaceRepository.save(any(Workspace.class))).thenReturn(existing);

        // Act
        Workspace result = workspaceService.updateWorkspace(id, updated);

        // Assert
        assertNotNull(result);
        verify(workspaceRepository, times(1)).save(existing);
        verify(workspaceCache, times(1)).put(id, existing);
    }

    @Test
    void deleteWorkspace_WhenNoReservations_ShouldDeleteAndRemoveFromCache() {
        // Arrange
        Long id = 1L;
        Workspace existing = new Workspace();
        existing.setId(id);
        existing.setReservations(new HashSet<>());

        when(workspaceCache.get(id)).thenReturn(existing);

        // Act
        workspaceService.deleteWorkspace(id);

        // Assert
        verify(workspaceRepository, times(1)).delete(existing);
        verify(workspaceCache, times(1)).remove(id);
    }

    @Test
    void deleteWorkspace_WhenHasReservations_ShouldThrowInvalidArgumentException() {
        // Arrange
        Long id = 1L;
        Workspace existing = new Workspace();
        existing.setId(id);
        existing.setReservations(new HashSet<>(Arrays.asList(new Reservation())));

        when(workspaceCache.get(id)).thenReturn(existing);

        // Act & Assert
        assertThrows(InvalidArgumentException.class, () -> workspaceService.deleteWorkspace(id));
        verify(workspaceRepository, never()).delete(any());
    }

    @Test
    void deleteWorkspace_WhenWorkspaceNotFound_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long id = 999L;
        when(workspaceCache.get(id)).thenReturn(null);
        when(workspaceRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> workspaceService.deleteWorkspace(id));
        verify(workspaceRepository, never()).delete(any());
    }
}