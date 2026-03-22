package com.example.ewaste.repository;

import com.example.ewaste.model.PickupRequest;
import com.example.ewaste.model.PickupPerson;
import com.example.ewaste.model.RequestStatus;
import com.example.ewaste.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickupRepository extends JpaRepository<PickupRequest, Long> {

    // ---------------- USER SIDE ----------------
    List<PickupRequest> findByUser(User user);
    Optional<PickupRequest> findByIdAndUser(Long id, User user);

    // ---------------- ADMIN SIDE ----------------
    List<PickupRequest> findByStatus(RequestStatus status);
    List<PickupRequest> findAll(); // For admin dashboard showing all requests

    // ---------------- PICKUP PERSON SIDE ----------------
    List<PickupRequest> findByAssignedPickupPerson(PickupPerson pickupPerson);
    Optional<PickupRequest> findByIdAndAssignedPickupPerson(Long id, PickupPerson pickupPerson);

    // ---------------- CUSTOM REPORT QUERY ----------------
    @Query("SELECT r FROM PickupRequest r WHERE r.pickupDate BETWEEN :startDate AND :endDate")
    List<PickupRequest> findRequestsBetweenDates(String startDate, String endDate);
}
