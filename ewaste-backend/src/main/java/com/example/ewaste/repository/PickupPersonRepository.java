package com.example.ewaste.repository;

import com.example.ewaste.model.PickupPerson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PickupPersonRepository extends JpaRepository<PickupPerson, Long> {

    boolean existsByEmail(String email);

    // Find PickupPerson by email (used for login and dashboard)
    Optional<PickupPerson> findByEmail(String email);
}
