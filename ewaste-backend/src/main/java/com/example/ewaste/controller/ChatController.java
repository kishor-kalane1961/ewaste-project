package com.example.ewaste.controller;

import com.example.ewaste.model.PickupRequest;
import com.example.ewaste.service.PickupRequestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final PickupRequestService pickupService;

    // Constructor injection
    public ChatController(PickupRequestService pickupService) {
        this.pickupService = pickupService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> chat(@RequestBody Map<String, String> body) {
        String message = body.getOrDefault("message", "").toLowerCase().trim();
        String reply;
        Object payload = null;

        if (message.isEmpty()) {
            reply = "Hello! I can help with: pickup, status, items, time, contact, benefits. What do you need?";
        } else if (message.contains("pickup") || message.contains("schedule")) {
            reply = "pickup_init";
        } else if (message.startsWith("status ")) {
            String[] parts = message.split("\\s+");
            if (parts.length >= 2) {
                try {
                    Long id = Long.parseLong(parts[1]);
                    Optional<PickupRequest> opt = pickupService.getRequestById(id);
                    if (opt.isPresent()) {
                        PickupRequest r = opt.get();
                        String dateStr = r.getPickupDate() != null ? r.getPickupDate().toString() : "not scheduled yet";
                        reply = String.format("Request %d status: %s (pickup date: %s)", r.getId(), r.getStatus(), dateStr);
                        payload = r;
                    } else {
                        reply = "No request found with ID " + parts[1];
                    }
                } catch (NumberFormatException nfe) {
                    reply = "Please provide a numeric Request ID. Example: 'status 5'";
                }
            } else {
                reply = "Please enter your Request ID. Example: 'status 5'";
            }
        } else if (message.contains("items") || message.contains("recycle") || message.contains("accepted")) {
            reply = "We accept: mobile phones, laptops, desktops, monitors, TVs, refrigerators, air conditioners, printers, batteries, chargers, and other small electronics. For large appliances, please mention exact model/weight.";
        } else if (message.contains("time") || message.contains("hours") || message.contains("working")) {
            reply = "Our pickup window is 9:00 AM – 6:00 PM, Monday to Saturday. Pickup day depends on availability.";
        } else if (message.contains("contact") || message.contains("phone") || message.contains("email")) {
            reply = "Contact us at support@ewaste.com or call +91-9876543210.";
        } else if (message.contains("benefit") || message.contains("why") || message.contains("importance")) {
            reply = "Recycling recovers precious metals, reduces pollution, and prevents hazardous materials from entering landfills.";
        } else if (message.startsWith("create_pickup")) {
            reply = "I can create a pickup — please use the pickup API endpoint or send address and items when prompted.";
        } else {
            reply = "Sorry, I didn't get that. Try: pickup, status <id>, items, time, contact, benefits.";
        }

        // Use HashMap to allow null payload
        Map<String, Object> response = new HashMap<>();
        response.put("response", reply);
        response.put("payload", payload);

        return ResponseEntity.ok(response);
    }
}
