package com.app.coworking.repository;

import com.app.coworking.model.Workspace;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    boolean existsByNameAndCoworkingId(@NotBlank(message = "Workspace name is required")
                                        @Size(min = 2, max = 100,
                                                message =
                                                "Workspace name must be "
                                                        + "between 2 and 100 characters")
                                        String name, Long coworkingId);
}
