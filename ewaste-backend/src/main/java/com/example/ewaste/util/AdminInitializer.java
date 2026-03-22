package com.example.ewaste.util;

import com.example.ewaste.model.Role;
import com.example.ewaste.model.User;
import com.example.ewaste.repository.UserRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer {

    private final UserRepository userRepository;

    public AdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void addAdminIfMissing() {
        String adminEmail = "admin@ewaste.com";
        if (userRepository.existsByEmail(adminEmail)) return;

        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail(adminEmail);
        admin.setPassword("admin123"); // plain text (as requested)
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        System.out.println("Admin user created: admin@ewaste.com / admin123");
    }
}
