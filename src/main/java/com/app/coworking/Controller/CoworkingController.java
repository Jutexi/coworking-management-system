package com.app.coworking.Controller;

import com.app.coworking.model.Coworking;
import com.app.coworking.service.CoworkingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/coworkings")
@RequiredArgsConstructor
public class CoworkingController {

    private final CoworkingService coworkingService;

    @GetMapping
    public List<Coworking> getAll() {
        return coworkingService.getAllCoworkings();
    }

    @GetMapping("/{id}")
    public Coworking getById(@PathVariable Long id) {
        return coworkingService.getCoworkingById(id);
    }

    @PostMapping
    public ResponseEntity<Coworking> create(@Valid @RequestBody Coworking coworking) {
        Coworking created = coworkingService.createCoworking(coworking);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public Coworking update(@PathVariable Long id, @Valid @RequestBody Coworking coworking) {
        return coworkingService.updateCoworking(id, coworking);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        coworkingService.deleteCoworking(id);
    }
}

