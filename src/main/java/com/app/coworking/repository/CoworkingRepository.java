package com.app.coworking.repository;

import com.app.coworking.model.Coworking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoworkingRepository extends JpaRepository<Coworking, Long> {
    boolean existsByName(String name);

    boolean existsByAddress(String address);
}