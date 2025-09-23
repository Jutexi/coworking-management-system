package com.app.coworking.service;

import com.app.coworking.cache.CoworkingCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.InvalidArgumentException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.Coworking;
import com.app.coworking.repository.CoworkingRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;


@Service
public class CoworkingService {

    private final CoworkingRepository coworkingRepository;
    private final CoworkingCache coworkingCache;

    public CoworkingService(CoworkingRepository coworkingRepository,
                            CoworkingCache coworkingCache) {
        this.coworkingRepository = coworkingRepository;
        this.coworkingCache = coworkingCache;
    }

    @Transactional
    public Coworking getCoworkingById(Long id) {
        Coworking coworking = coworkingCache.get(id);
        if (coworking != null) {
            return coworking;
        }
        coworking = coworkingRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Coworking not found with id " + id));
        coworkingCache.put(id, coworking);
        return coworking;
    }

    @Transactional
    public List<Coworking> getAllCoworkings() {
        return coworkingRepository.findAll();
    }

    @Transactional
    public Coworking createCoworking(Coworking coworking) {

        if (coworking.getWorkspaces() != null && !coworking.getWorkspaces().isEmpty()) {
            throw new InvalidArgumentException(
                    "Cannot create workspaces through coworking creation."
                            + " Use workspace endpoints instead.");
        }

        if (coworkingRepository.existsByName(coworking.getName())) {
            throw new AlreadyExistsException(
                    "Coworking with name '" + coworking.getName() + "' already exists");
        }
        if (coworkingRepository.existsByAddress(coworking.getAddress())) {
            throw new AlreadyExistsException(
                    "Coworking with address '" + coworking.getAddress() + "' already exists");
        }

        Coworking saved = coworkingRepository.save(coworking);
        coworkingCache.put(saved.getId(), saved);
        return saved;
    }

    @Transactional
    public Coworking updateCoworking(Long id, Coworking updatedCoworking) {

        if (updatedCoworking.getWorkspaces() != null
                && !updatedCoworking.getWorkspaces().isEmpty()) {
            throw new InvalidArgumentException("Cannot update workspaces through coworking update."
                    + " Use workspace endpoints instead.");
        }

        Coworking existing = getCoworkingById(id);

        // Проверка уникальности (кроме текущей записи)
        if (!existing.getName().equals(updatedCoworking.getName())
                && coworkingRepository.existsByName(updatedCoworking.getName())) {
            throw new AlreadyExistsException(
                    "Coworking with name '" + updatedCoworking.getName() + "' already exists");
        }
        if (!existing.getAddress().equals(updatedCoworking.getAddress())
                && coworkingRepository.existsByAddress(updatedCoworking.getAddress())) {
            throw new AlreadyExistsException(
                    "Coworking with address '" + updatedCoworking.getAddress()
                            + "' already exists");
        }

        // Обновляем поля
        existing.setName(updatedCoworking.getName());
        existing.setAddress(updatedCoworking.getAddress());
        existing.setEmail(updatedCoworking.getEmail());
        existing.setPhoneNumber(updatedCoworking.getPhoneNumber());
        existing.setDescription(updatedCoworking.getDescription());

        Coworking saved = coworkingRepository.save(existing);
        coworkingCache.put(saved.getId(), saved);
        return saved;
    }

    @Transactional
    public void deleteCoworking(Long id) {
        Coworking existing = getCoworkingById(id);
        if (existing.getWorkspaces() != null && !existing.getWorkspaces().isEmpty()) {
            throw new InvalidArgumentException("Cannot delete coworking with existing workspaces."
                    + " Delete workspaces first.");
        }
        coworkingRepository.delete(existing);
        coworkingCache.remove(id);
    }

}
