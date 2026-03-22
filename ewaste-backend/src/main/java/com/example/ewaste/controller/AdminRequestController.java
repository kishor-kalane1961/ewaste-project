package com.example.ewaste.controller;

import com.example.ewaste.model.PickupPerson;
import com.example.ewaste.model.PickupRequest;
import com.example.ewaste.model.RequestStatus;
import com.example.ewaste.service.PickupRequestService;
import com.example.ewaste.service.PickupPersonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminRequestController {

    private final PickupRequestService pickupRequestService;
    @Autowired
    private PickupPersonService pickupPersonService;
    public AdminRequestController(PickupRequestService pickupRequestService) {
        this.pickupRequestService = pickupRequestService;
    }

    @PostMapping("/pickup-persons")
    public ResponseEntity<?> createPickupPerson(@RequestBody PickupPerson person) {
        try {
            PickupPerson saved = pickupPersonService.createPerson(person);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Get all pickup persons
    @GetMapping("/pickup-persons")
    public List<PickupPerson> getAllPickupPersons() {
        return pickupPersonService.getAllPersons();
    }

    // Get pickup person by ID
    @GetMapping("/pickup-persons/{id}")
    public ResponseEntity<?> getPickupPersonById(@PathVariable Long id) {
        return pickupPersonService.getPersonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    // -------------------- REQUEST MANAGEMENT --------------------

    @GetMapping("/requests")
    public List<PickupRequest> getAllRequests() {
        return pickupRequestService.getAllRequests();
    }

    @GetMapping("/requests/status/{status}")
    public List<PickupRequest> getRequestsByStatus(@PathVariable RequestStatus status) {
        return pickupRequestService.getAllRequests()
                .stream()
                .filter(req -> req.getStatus() == status)
                .toList();
    }

    @PutMapping("/requests/{id}/approve")
    public PickupRequest approveRequest(@PathVariable Long id) {
        return pickupRequestService.updateRequestStatus(id, RequestStatus.APPROVED, null);
    }

    @PutMapping("/requests/{id}/reject")
    public PickupRequest rejectRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return pickupRequestService.updateRequestStatus(id, RequestStatus.REJECTED, body.get("reason"));
    }

    @PutMapping("/requests/{id}/schedule")
    public PickupRequest scheduleRequest(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return pickupRequestService.scheduleRequest(id, body.get("pickupDate"), body.get("pickupTime"));
    }

    @PutMapping("/requests/{requestId}/assign/{personId}")
    public ResponseEntity<PickupRequest> assignPickupPerson(
            @PathVariable Long requestId,
            @PathVariable Long personId) {
        PickupRequest updated = pickupRequestService.assignPickupPerson(requestId, personId);
        return ResponseEntity.ok(updated);
    }
    // DELETE /api/admin/requests
    @DeleteMapping("/requests")
    public ResponseEntity<?> deleteAllRequests() {
        try {
            pickupRequestService.deleteAllRequests();
            return ResponseEntity.ok(Map.of("message", "All requests deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", e.getMessage()));
        }
    }


    // -------------------- REPORTS --------------------

    @GetMapping("/reports/daily")
    public ResponseEntity<byte[]> downloadDailyReport() {
        byte[] pdf = pickupRequestService.generateDailyReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=daily-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/reports/weekly")
    public ResponseEntity<byte[]> downloadWeeklyReport() {
        byte[] pdf = pickupRequestService.generateWeeklyReport();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=weekly-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/reports/custom")
    public ResponseEntity<byte[]> downloadCustomReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        byte[] pdf = pickupRequestService.generateCustomReport(start, end);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=custom-report.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

}
