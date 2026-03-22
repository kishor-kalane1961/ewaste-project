package com.example.ewaste.controller;

import com.example.ewaste.model.PickupPerson;
import com.example.ewaste.model.PickupRequest;
import com.example.ewaste.model.RequestStatus;
import com.example.ewaste.service.PickupPersonService;
import com.example.ewaste.service.PickupRequestService;
import com.example.ewaste.service.EmailService;
import com.example.ewaste.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;


import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pickup-persons")
public class PickupPersonController {

    private final PickupPersonService pickupPersonService;
    private final PickupRequestService pickupRequestService;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Autowired
    public PickupPersonController(PickupPersonService pickupPersonService,
                                  PickupRequestService pickupRequestService,
                                  JwtUtil jwtUtil,
                                  EmailService emailService) {
        this.pickupPersonService = pickupPersonService;
        this.pickupRequestService = pickupRequestService;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    // -------------------- ADMIN CRUD --------------------
    @GetMapping
    public List<PickupPerson> getAllPersons() {
        return pickupPersonService.getAllPersons();
    }

    @GetMapping("/{id}")
    public ResponseEntity<PickupPerson> getPersonById(@PathVariable Long id) {
        return pickupPersonService.getPersonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createPerson(@RequestBody PickupPerson person) {
        try {
            PickupPerson savedPerson = pickupPersonService.createPerson(person);
            return ResponseEntity.ok(savedPerson);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePerson(@PathVariable Long id, @RequestBody PickupPerson person) {
        try {
            PickupPerson updatedPerson = pickupPersonService.updatePerson(id, person);
            return ResponseEntity.ok(updatedPerson);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePerson(@PathVariable Long id) {
        pickupPersonService.deletePerson(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-multiple")
    public ResponseEntity<?> deleteMultiplePersons(@RequestBody List<Long> ids) {
        try {
            for (Long id : ids) {
                pickupPersonService.deletePerson(id);
            }
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    // -------------------- PICKUP PERSON LOGIN --------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");

        if (pickupPersonService.validateLogin(email, password)) {
            String token = jwtUtil.generateToken(email, "PICKUP_PERSON");
            return ResponseEntity.ok(Map.of("token", token));
        } else {
            return ResponseEntity.status(401).body("Invalid email or password");
        }
    }

    // -------------------- PICKUP PERSON DASHBOARD --------------------
    @GetMapping("/dashboard/assigned-requests")
    public ResponseEntity<List<PickupRequest>> getAssignedRequests(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).build();
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            PickupPerson person = pickupPersonService.getByEmail(email)
                    .orElseThrow(() -> new RuntimeException("PickupPerson not found"));

            List<PickupRequest> assignedRequests = pickupRequestService.getRequestsForPickupPerson(person.getId());
            return ResponseEntity.ok(assignedRequests);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }

    // -------------------- UPDATE PICKUP REQUEST STATUS --------------------
    @PutMapping("/request/{id}/status")
    public ResponseEntity<?> updateRequestStatus(@PathVariable Long id,
                                                 @RequestBody Map<String, String> body,
                                                 HttpServletRequest request) {
        try {
            String statusStr = body.get("status");
            if (statusStr == null)
                return ResponseEntity.badRequest().body("Status is required");

            // Extract email from token
            String authHeader = request.getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).build();
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractEmail(token);

            PickupPerson person = pickupPersonService.getByEmail(email)
                    .orElseThrow(() -> new RuntimeException("PickupPerson not found"));

            RequestStatus requestStatus;
            try {
                requestStatus = RequestStatus.valueOf(statusStr);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().body("Invalid status value: " + statusStr);
            }

            PickupRequest updatedRequest = pickupRequestService.updatePickupPersonRequestStatus(id, person.getId(), requestStatus);

            return ResponseEntity.ok(Map.of(
                    "message", "Status updated successfully",
                    "request", updatedRequest
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }

    // -------------------- OTP GENERATION FOR PICKUP --------------------
    @PostMapping("/pickup/{id}/generate-otp")
    public ResponseEntity<String> generatePickupOtp(@PathVariable Long id) {
        Optional<PickupRequest> optionalRequest = pickupRequestService.getRequestById(id);

        if (optionalRequest.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PickupRequest request = optionalRequest.get();

        // Generate 6-digit OTP
        String otp = String.valueOf(100000 + new java.util.Random().nextInt(900000));
        request.setOtp(otp);
        request.setOtpGeneratedAt(java.time.LocalDateTime.now());

        // Save the request
        pickupRequestService.saveRequest(request);

        // Log OTP for testing
        System.out.println("Generated OTP for request ID " + id + ": " + otp);

        // Send OTP email
        emailService.sendOtpEmail(request);

        return ResponseEntity.ok("OTP generated and sent to user email!");
    }

    // -------------------- OTP VERIFICATION --------------------
    @PostMapping("/pickup/{id}/verify-otp")
    public ResponseEntity<String> verifyPickupOtp(@PathVariable Long id,
                                                  @RequestBody Map<String, String> body) {
        String enteredOtp = body.get("otp");
        if (enteredOtp == null || enteredOtp.isEmpty()) {
            return ResponseEntity.badRequest().body("OTP is required");
        }

        Optional<PickupRequest> optionalRequest = pickupRequestService.getRequestById(id);
        if (optionalRequest.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        PickupRequest request = optionalRequest.get();
        String actualOtp = request.getOtp();

        if (actualOtp == null) {
            return ResponseEntity.status(400).body("No OTP generated for this request");
        }

        // Trim whitespace and compare
        if (!enteredOtp.trim().equals(actualOtp.trim())) {
            return ResponseEntity.status(400).body("Invalid OTP");
        }

        // Check OTP expiry (10 minutes)
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (request.getOtpGeneratedAt() == null ||
                java.time.Duration.between(request.getOtpGeneratedAt(), now).toMinutes() > 10) {
            return ResponseEntity.status(400).body("OTP expired");
        }

        // OTP verified -> update status
        request.setStatus(RequestStatus.PICKED_UP);
        pickupRequestService.saveRequest(request);

        return ResponseEntity.ok("OTP verified successfully! Pickup confirmed.");
    }

}
