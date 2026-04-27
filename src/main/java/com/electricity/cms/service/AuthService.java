package com.electricity.cms.service;

import com.electricity.cms.dto.UserContext;
import com.electricity.cms.model.User;
import com.electricity.cms.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import org.hibernate.LazyInitializationException;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService() {
        this(new UserRepository());
    }

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserContext login(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        if (!user.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid username or password.");
        }

        String displayName = user.getUsername();
        try {
            if (user.getPerson() != null && user.getPerson().getFullName() != null) {
                displayName = user.getPerson().getFullName();
            }
        } catch (EntityNotFoundException | LazyInitializationException ignored) {
            // Keep username fallback when person cannot be loaded or is missing
        }

        return new UserContext(
            user.getId(),
            user.getRole(),
            user.getRegion() != null ? user.getRegion().getId() : null,
            displayName
        );
    }
}
