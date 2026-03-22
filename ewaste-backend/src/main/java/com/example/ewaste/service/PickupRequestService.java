package com.example.ewaste.service;

import com.example.ewaste.model.PickupRequest;
import com.example.ewaste.model.PickupPerson;
import com.example.ewaste.model.RequestStatus;
import com.example.ewaste.repository.PickupRepository;
import com.example.ewaste.repository.PickupPersonRepository;
import com.example.ewaste.util.ReportGenerator; // import PDF utility
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PickupRequestService {

    @Autowired
    private PickupRepository pickupRequestRepository;

    @Autowired
    private PickupPersonRepository pickupPersonRepository;

    @Autowired
    private EmailService emailService; // ✅ Inject EmailService

    // ---------------- EXISTING METHODS ----------------
    public List<PickupRequest> getAllRequests() { return pickupRequestRepository.findAll(); }
    public Optional<PickupRequest> getRequestById(Long id) { return pickupRequestRepository.findById(id); }
    public PickupRequest saveRequest(PickupRequest request) { return pickupRequestRepository.save(request); }

    public PickupRequest updateRequest(Long id, PickupRequest updatedRequest) {
        return pickupRequestRepository.findById(id)
                .map(request -> {
                    request.setUser(updatedRequest.getUser());
                    request.setMobileNo(updatedRequest.getMobileNo());
                    request.setPickupAddress(updatedRequest.getPickupAddress());
                    request.setDeviceType(updatedRequest.getDeviceType());
                    request.setModel(updatedRequest.getModel());
                    request.setPickupDate(updatedRequest.getPickupDate());
                    request.setPickupTime(updatedRequest.getPickupTime());
                    request.setPhotoPath(updatedRequest.getPhotoPath());
                    request.setCondition(updatedRequest.getCondition());
                    request.setQuantity(updatedRequest.getQuantity());
                    request.setStatus(updatedRequest.getStatus());
                    request.setRejectionReason(updatedRequest.getRejectionReason());
                    request.setAssignedPickupPerson(updatedRequest.getAssignedPickupPerson());
                    PickupRequest saved = pickupRequestRepository.save(request);

                    emailService.sendStatusUpdateEmail(saved); // send email
                    return saved;
                })
                .orElseThrow(() -> new RuntimeException("PickupRequest not found with id " + id));
    }

    public void deleteRequest(Long id) { pickupRequestRepository.deleteById(id); }
    public PickupRequest scheduleRequest(Long id, String pickupDate, String pickupTime) {
        return pickupRequestRepository.findById(id)
                .map(request -> {
                    request.setPickupDate(pickupDate);
                    request.setPickupTime(pickupTime);
                    request.setStatus(RequestStatus.SCHEDULED);
                    PickupRequest saved = pickupRequestRepository.save(request);

                    emailService.sendPickupScheduledEmail(saved);
                    return saved;
                }).orElseThrow(() -> new RuntimeException("PickupRequest not found with id " + id));
    }

    public PickupRequest updateRequestStatus(Long id, RequestStatus status, String rejectionReason) {
        return pickupRequestRepository.findById(id)
                .map(request -> {
                    request.setStatus(status);
                    request.setRejectionReason(status == RequestStatus.REJECTED ? rejectionReason : null);
                    PickupRequest saved = pickupRequestRepository.save(request);
                    emailService.sendStatusUpdateEmail(saved);
                    return saved;
                }).orElseThrow(() -> new RuntimeException("PickupRequest not found with id " + id));
    }

    public PickupRequest updateRequestStatus(Long id, RequestStatus status) { return updateRequestStatus(id, status, null); }

    public PickupRequest assignPickupPerson(Long requestId, Long personId) {
        PickupRequest request = pickupRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("PickupRequest not found with id " + requestId));
        PickupPerson person = pickupPersonRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("PickupPerson not found with id " + personId));

        request.setAssignedPickupPerson(person);
        request.setStatus(RequestStatus.SCHEDULED);
        PickupRequest saved = pickupRequestRepository.save(request);
        emailService.sendPickupAssignmentEmail(person.getEmail(), saved);
        return saved;
    }

    public List<PickupRequest> getRequestsForPickupPerson(Long personId) {
        PickupPerson person = pickupPersonRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("PickupPerson not found with id " + personId));
        return pickupRequestRepository.findByAssignedPickupPerson(person);
    }

    public PickupRequest updatePickupPersonRequestStatus(Long requestId, Long personId, RequestStatus status) {
        PickupPerson person = pickupPersonRepository.findById(personId)
                .orElseThrow(() -> new RuntimeException("PickupPerson not found with id " + personId));
        PickupRequest request = pickupRequestRepository.findByIdAndAssignedPickupPerson(requestId, person)
                .orElseThrow(() -> new RuntimeException("Request not assigned to PickupPerson with id " + personId));

        request.setStatus(status);
        PickupRequest saved = pickupRequestRepository.save(request);
        emailService.sendStatusUpdateEmail(saved);
        return saved;
    }

    // ---------------- COMPLETED REQUEST FILTERS ----------------
    public List<PickupRequest> getCompletedRequestsByDate(LocalDate start, LocalDate end) {
        return pickupRequestRepository.findAll()
                .stream()
                .filter(req -> req.getStatus() == RequestStatus.COMPLETED)
                .filter(req -> req.getPickupDate() != null)
                .filter(req -> {
                    LocalDate date = LocalDate.parse(req.getPickupDate());
                    return !date.isBefore(start) && !date.isAfter(end);
                })
                .collect(Collectors.toList());
    }

    public List<PickupRequest> getRequestsByDay(LocalDate day) { return getCompletedRequestsByDate(day, day); }
    public void deleteAllRequests() { pickupRequestRepository.deleteAll(); }
    public List<PickupRequest> getRequestsLastNDays(int days) {
        LocalDate end = LocalDate.now();
        LocalDate start = end.minusDays(days - 1);
        return getCompletedRequestsByDate(start, end);
    }

    // ---------------- PDF REPORT GENERATION ----------------
    public byte[] generateDailyReport() {
        LocalDate today = LocalDate.now();
        List<PickupRequest> completed = getRequestsByDay(today);
        return ReportGenerator.generateCompletedRequestsReport(completed, "Daily Report - " + today);
    }

    public byte[] generateWeeklyReport() {
        List<PickupRequest> completed = getRequestsLastNDays(7);
        return ReportGenerator.generateCompletedRequestsReport(completed, "Weekly Report");
    }

    public byte[] generateCustomReport(LocalDate start, LocalDate end) {
        List<PickupRequest> completed = getCompletedRequestsByDate(start, end);
        return ReportGenerator.generateCompletedRequestsReport(completed, "Custom Report (" + start + " to " + end + ")");
    }
}
