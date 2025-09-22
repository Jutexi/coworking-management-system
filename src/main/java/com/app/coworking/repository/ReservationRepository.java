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

    @Query("SELECT r FROM Reservation r WHERE "
            + "(:coworkingId IS NULL OR r.workspace.coworking.id = :coworkingId) AND "
            + "((r.startDate BETWEEN :startDate AND :endDate) OR "
            + "(r.endDate BETWEEN :startDate AND :endDate) OR "
            + "(r.startDate <= :startDate AND r.endDate >= :endDate))")
    List<Reservation> findReservationsByPeriodAndCoworking(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("coworkingId") Long coworkingId);
}
