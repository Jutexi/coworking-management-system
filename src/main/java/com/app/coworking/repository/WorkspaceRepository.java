package com.app.coworking.repository;

import com.app.coworking.model.Workspace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkspaceRepository extends JpaRepository<Workspace, Long> {
    @Query("SELECT CASE WHEN COUNT(w) > 0 THEN true ELSE false END "
            + "FROM Workspace w "
            + "WHERE w.name = :name AND w.coworking.id = :coworkingId")
    boolean existsByNameAndCoworkingId(@Param("name") String name,
                                       @Param("coworkingId") Long coworkingId);
}
