package com.app.coworking.repository;

import com.app.coworking.model.Coworking;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CoworkingRepository extends JpaRepository<Coworking, Long> {
    boolean existsByName(@NotBlank(message = "Coworking name is required") @Size(min = 2, max = 100, message = "Coworking name must be between 2 and 100 characters") String name);

    boolean existsByAddress(@NotBlank(message = "Address is required") @Size(min = 10, max = 200, message = "Address must be between 10 and 200 characters") String address);
}