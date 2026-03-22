package com.example.ewaste.service;

import com.example.ewaste.model.*;
import com.example.ewaste.repository.PickupRepository;
import com.example.ewaste.repository.PickupPersonRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

@Service
public class PickupService {

    private final PickupRepository pickupRepository;
    private final PickupPersonRepository pickupPersonRepository;
    private final EmailService emailService;

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    // ✅ Admin email injected from application.properties
    @Value("${admin.email}")
    private String adminEmail;

    // 👇 base URL from properties (optional for links in email)
    @Value("${app.base-url:http://localhost:8084}")
    private String baseUrl;

    public PickupService(PickupRepository pickupRepository,
                         PickupPersonRepository pickupPersonRepository,
                         EmailService emailService) {
        this.pickupRepository = pickupRepository;
        this.pickupPersonRepository = pickupPersonRepository;
        this.emailService = emailService;
    }

    // -------------------- CREATE REQUEST --------------------
    public PickupRequest create(User user,
                                String mobileNo,
                                String pickupAddress,
                                DeviceType deviceType,
                                String model,
                                String pickupDate,
                                String pickupTime,
                                MultipartFile photo,
                                Condition condition,
                                int quantity) throws IOException {

        PickupRequest request = new PickupRequest();
        request.setUser(user);
        request.setMobileNo(mobileNo);
        request.setPickupAddress(pickupAddress);
        request.setDeviceType(deviceType);
        request.setModel(model);
        request.setPickupDate(pickupDate);
        request.setPickupTime(pickupTime);
        request.setCondition(condition);
        request.setQuantity(quantity);
        request.setStatus(RequestStatus.PENDING);

        // Save uploaded photo
        if (photo != null && !photo.isEmpty()) {
            String originalName = photo.getOriginalFilename()
                    .replaceAll("\\s+", "_")
                    .replaceAll("[()]", "");
            String filename = System.currentTimeMillis() + "_" + originalName;

            Path uploadPath = Path.of(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            photo.transferTo(uploadPath.resolve(filename).toFile());

            request.setPhotoPath(filename);
        }

        PickupRequest saved = pickupRepository.save(request);

        // ✅ Send confirmation email to user
        emailService.sendRequestSubmittedEmail(saved);

        // ✅ Send notification email to admin
        emailService.sendAdminNotificationEmail(saved);
        return saved;
    }

    // -------------------- UPDATE STATUS --------------------
    public PickupRequest updateStatus(Long id, RequestStatus status, String rejectionReason) {
        PickupRequest request = pickupRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pickup request not found"));

        request.setStatus(status);
        request.setRejectionReason(status == RequestStatus.REJECTED ? rejectionReason : null);

        PickupRequest updated = pickupRepository.save(request);

        // ✅ Trigger emails depending on status
        switch (status) {
            case APPROVED, REJECTED, PICKED_UP, SCHEDULED ->
                    emailService.sendStatusUpdateEmail(updated);
            case COMPLETED ->
                    emailService.sendCompletionEmail(updated);
            default -> {
            }
        }

        return updated;
    }

    // -------------------- FIND METHODS --------------------
    public List<PickupRequest> findMy(User user) {
        return pickupRepository.findByUser(user);
    }

    public List<PickupRequest> findAll() {
        return pickupRepository.findAll();
    }

    public Optional<PickupRequest> findByIdAndUser(Long id, User user) {
        return pickupRepository.findByIdAndUser(id, user);
    }

    // -------------------- CANCEL --------------------
    public void cancelRequest(PickupRequest request) {
        pickupRepository.delete(request);
        // (Optional) You can add emailService.sendStatusUpdateEmail(request);
    }

    // -------------------- ASSIGN PICKUP PERSON --------------------
    public PickupRequest schedulePickup(Long requestId, Long pickupPersonId) {
        PickupRequest request = pickupRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        PickupPerson pickupPerson = pickupPersonRepository.findById(pickupPersonId)
                .orElseThrow(() -> new RuntimeException("Pickup person not found"));

        request.setAssignedPickupPerson(pickupPerson);
        request.setStatus(RequestStatus.SCHEDULED);

        pickupPerson.setAvailable(false);
        pickupPersonRepository.save(pickupPerson);

        pickupRepository.save(request);

        // ✅ Notify pickup person + user
        emailService.sendPickupAssignmentEmail(pickupPerson.getEmail(), request);
        emailService.sendPickupScheduledEmail(request);

        return request;
    }
}
