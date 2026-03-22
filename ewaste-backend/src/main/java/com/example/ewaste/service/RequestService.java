package com.example.ewaste.service;

import com.example.ewaste.model.PickupRequest;
import com.example.ewaste.model.RequestStatus;
import com.example.ewaste.repository.PickupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RequestService {

    private final PickupRepository repo;
    private final EmailService emailService;

    public RequestService(PickupRepository repo, EmailService emailService) {
        this.repo = repo;
        this.emailService = emailService;
    }

    // Admin: Get all requests
    public List<PickupRequest> getAllRequests() {
        return repo.findAll();
    }

    // Admin: Filter by status
    public List<PickupRequest> getRequestsByStatus(RequestStatus status) {
        return repo.findByStatus(status);
    }

    // Approve request
    @Transactional
    public PickupRequest approveRequest(Long id) {
        PickupRequest request = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(RequestStatus.APPROVED);
        repo.save(request);

        emailService.sendStatusUpdateEmail(request);
        return request;
    }

    // Reject request
    @Transactional
    public PickupRequest rejectRequest(Long id, String reason) {
        PickupRequest request = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(RequestStatus.REJECTED);
        request.setRejectionReason(reason);
        repo.save(request);

        emailService.sendStatusUpdateEmail(request);
        return request;
    }

    // Schedule request (admin assigns date & time already stored in entity)
    @Transactional
    public PickupRequest scheduleRequest(Long id, String pickupDate, String pickupTime) {
        PickupRequest request = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Request not found"));
        request.setStatus(RequestStatus.SCHEDULED);
        request.setPickupDate(pickupDate);
        request.setPickupTime(pickupTime);
        repo.save(request);

        emailService.sendPickupScheduledEmail(request);
        return request;
    }
}
