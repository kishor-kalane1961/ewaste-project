package com.example.ewaste.repository;

import com.example.ewaste.model.Role;
import com.example.ewaste.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    // ✅ New method: Find by email AND role (for stricter login checks)
    Optional<User> findByEmailAndRole(String email, Role role);
}
