package com.example.ewaste.service;

import com.example.ewaste.model.Role;
import com.example.ewaste.model.User;
import com.example.ewaste.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final EmailService emailService; // inject email service

    // Constructor injection
    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    // -------------------- REGISTER USER --------------------
    public User registerUser(String username, String email, String password, Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("Email already registered");
        }

        User u = new User();
        u.setUsername(username);
        u.setEmail(email);
        u.setPassword(password);   // ⚠️ For production, hash passwords!
        u.setRole(role != null ? role : Role.USER); // default USER if null

        User savedUser = userRepository.save(u);

        // ✅ Send welcome email after registration
        emailService.sendWelcomeEmail(savedUser);

        return savedUser;
    }

    // -------------------- LOGIN USER --------------------
    public User loginUser(String email, String password) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        if (!u.getPassword().equals(password)) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        return u;
    }

    // -------------------- GET USER --------------------
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }
}
