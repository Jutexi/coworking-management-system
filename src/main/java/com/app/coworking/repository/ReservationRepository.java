package com.app.coworking.repository;

import com.app.coworking.model.Reservation;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Query("SELECT r FROM Reservation r WHERE r.user.email = :email")
    List<Reservation> findByUserEmail(@Param("email") String email);

    @Query("SELECT r FROM Reservation r "
            + "WHERE r.workspace.id = :workspaceId "
            + "AND r.startDate <= :end "
            + "AND r.endDate >= :start")
    List<Reservation> findOverlappingReservations(Long workspaceId, LocalDate start, LocalDate end);
}
