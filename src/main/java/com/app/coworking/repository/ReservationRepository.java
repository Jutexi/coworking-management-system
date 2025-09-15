package com.app.coworking.repository;

import com.app.coworking.model.Reservation;
import com.app.coworking.model.Workspace;
import com.app.coworking.model.enums.WorkspaceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.user.id = :userId")
    List<Reservation> findByUserId(@Param("userId") Long userId);

    @Query("SELECT r FROM Reservation r " +
            "WHERE r.workspace.id = :workspaceId " +
            "AND r.startDate <= :end " +
            "AND r.endDate >= :start")
    List<Reservation> findOverlappingReservations(Long workspaceId, LocalDate start, LocalDate end);

}
