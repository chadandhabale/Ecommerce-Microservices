package com.example.ecoms.ServiceImpl;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.ecoms.Entity.User;
import com.example.ecoms.Repository.UserRepository;
import com.example.ecoms.Service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service layer for managing user registration, login, and CRUD operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Injected from a @Bean in config

    @Override
    public User registerUser(User user) {
        log.info("Registering new user: {}", user.getEmail());

        // ✅ Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already registered: " + user.getEmail());
        }

        // ✅ Encrypt password before saving
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());
        return savedUser;
    }

    @Override
    public User loginUser(String email, String password) {
        log.info("Attempting login for user: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No user found with email: " + email));

        // ✅ Verify password
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials for email: " + email);
        }

        log.info("Login successful for user: {}", email);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        log.info("Fetching all users...");
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        log.info("Fetching user by email: {}", email);
        return userRepository.findByEmail(email);
    }

    @Override
    public void deleteUser(Long userId) {
        log.info("Deleting user with ID: {}", userId);
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found with ID: " + userId);
        }
        userRepository.deleteById(userId);
        log.info("User deleted successfully: {}", userId);
    }
}
