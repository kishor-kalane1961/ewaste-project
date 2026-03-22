package com.example.ewaste.controller;

import com.example.ewaste.dto.ResponseMessage;
import com.example.ewaste.model.*;
import com.example.ewaste.service.PickupService;
import com.example.ewaste.service.UserService;
import com.example.ewaste.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/pickup")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"}, allowCredentials = "true")
public class PickupController {

    private final PickupService pickupService;
    private final JwtUtil jwtUtil;
    private final UserService userService;

    public PickupController(PickupService pickupService, JwtUtil jwtUtil, UserService userService) {
        this.pickupService = pickupService;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
    }

    @PostMapping("/create")
    public ResponseEntity<ResponseMessage> createPickup(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("mobileNo") String mobileNo,
            @RequestParam("pickupAddress") String pickupAddress,
            @RequestParam("deviceType") DeviceType deviceType,
            @RequestParam("model") String model,
            @RequestParam("pickupDate") String pickupDate,
            @RequestParam("pickupTime") String pickupTime,
            @RequestParam("condition") Condition condition,
            @RequestParam("quantity") int quantity,
            @RequestParam(value = "photo", required = false) MultipartFile photo) {

        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(ResponseMessage.error("Authorization header missing or invalid"));
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);

            if (!jwtUtil.validateToken(token, email)) {
                return ResponseEntity.status(401).body(ResponseMessage.error("Invalid or expired token"));
            }

            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(ResponseMessage.error("User not found"));
            }

            if (mobileNo.isBlank() || pickupAddress.isBlank() || model.isBlank() ||
                    pickupDate.isBlank() || pickupTime.isBlank()) {
                return ResponseEntity.badRequest().body(ResponseMessage.error("All fields except photo are required"));
            }

            PickupRequest saved = pickupService.create(
                    user,
                    mobileNo,
                    pickupAddress,
                    deviceType,
                    model,
                    pickupDate,
                    pickupTime,
                    photo,
                    condition,
                    quantity
            );

            return ResponseEntity.ok(ResponseMessage.success("Pickup request created successfully", saved));

        } catch (IOException e) {
            return ResponseEntity.status(500).body(ResponseMessage.error("File upload failed: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ResponseMessage.error("Unexpected error: " + e.getMessage()));
        }
    }

    @GetMapping("/my-requests")
    public ResponseEntity<ResponseMessage> myRequests(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(ResponseMessage.error("Authorization header missing or invalid"));
            }

            String token = authHeader.substring(7);
            String email = jwtUtil.extractUsername(token);

            if (!jwtUtil.validateToken(token, email)) {
                return ResponseEntity.status(401).body(ResponseMessage.error("Invalid or expired token"));
            }

            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(404).body(ResponseMessage.error("User not found"));
            }

            List<PickupRequest> list = pickupService.findMy(user);
            return ResponseEntity.ok(ResponseMessage.success("Pickup requests fetched successfully", list));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(ResponseMessage.error("Unexpected error: " + e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseMessage> updateStatus(
            @PathVariable Long id,
            @RequestParam RequestStatus status,
            @RequestParam(required = false) String rejectionReason) {

        try {
            PickupRequest updated = pickupService.updateStatus(id, status, rejectionReason);
            return ResponseEntity.ok(ResponseMessage.success("Status updated successfully", updated));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(ResponseMessage.error("Error updating status: " + e.getMessage()));
        }
    }
}
