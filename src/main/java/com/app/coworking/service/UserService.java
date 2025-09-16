package com.app.coworking.service;

import com.app.coworking.cache.UserCache;
import com.app.coworking.exception.AlreadyExistsException;
import com.app.coworking.exception.ResourceNotFoundException;
import com.app.coworking.model.User;
import com.app.coworking.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserCache userCache;

    public UserService(UserRepository userRepository, UserCache userCache) {
        this.userRepository = userRepository;
        this.userCache = userCache;
    }

    @Transactional
    public User getUserById(Long id) {
        User user = userCache.get(id);
        if (user != null) {
            return user;
        }

        user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + id));
        userCache.put(id, user);
        return user;
    }

    @Transactional
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new AlreadyExistsException("Email is already in use");
        }

        User saved = userRepository.save(user);
        userCache.put(saved.getId(), saved);
        return saved;
    }

    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User existing = getUserById(id);

        if (!existing.getEmail().equals(updatedUser.getEmail())
                && userRepository.existsByEmail(updatedUser.getEmail())) {
            throw new AlreadyExistsException("Email is already in use");
        }

        existing.setFirstName(updatedUser.getFirstName());
        existing.setLastName(updatedUser.getLastName());
        existing.setEmail(updatedUser.getEmail());
        existing.setPassword(updatedUser.getPassword());
        existing.setRole(updatedUser.getRole());

        User saved = userRepository.save(existing);
        userCache.put(saved.getId(), saved);
        return saved;
    }

    @Transactional
    public void deleteUser(Long id) {
        User existing = getUserById(id);
        userRepository.delete(existing);
        userCache.remove(id);
    }
}
