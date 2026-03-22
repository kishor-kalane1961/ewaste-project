package com.example.ewaste.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pickup_requests")
public class PickupRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ---------------- User Who Created the Request ----------------
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private User user;

    @Column(nullable = false)
    private String mobileNo;

    @Column(nullable = false)
    private String pickupAddress;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeviceType deviceType;

    @Column(nullable = false)
    private String model;

    @Column(nullable = false)
    private String pickupDate;

    @Column(nullable = false)
    private String pickupTime;

    private String photoPath;

    @Enumerated(EnumType.STRING)
    @Column(name = "device_condition", nullable = false)
    private Condition condition;

    @Column(nullable = false)
    private int quantity;
    @Column(name = "completed_at")
    private LocalDateTime completedAt;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RequestStatus status = RequestStatus.PENDING;

    private String rejectionReason;

    // ---------------- Assigned Pickup Person ----------------
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_person_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PickupPerson assignedPickupPerson;

    // ---------------- OTP Verification Fields ----------------
    @Column(length = 6)
    private String otp;

    private LocalDateTime otpGeneratedAt;

    // ---------------- Constructors ----------------
    public PickupRequest() {
    }

    public PickupRequest(User user, String mobileNo, String pickupAddress,
                         DeviceType deviceType, String model,
                         String pickupDate, String pickupTime,
                         String photoPath, Condition condition,
                         int quantity, RequestStatus status, String rejectionReason,
                         PickupPerson assignedPickupPerson) {
        this.user = user;
        this.mobileNo = mobileNo;
        this.pickupAddress = pickupAddress;
        this.deviceType = deviceType;
        this.model = model;
        this.pickupDate = pickupDate;
        this.pickupTime = pickupTime;
        this.photoPath = photoPath;
        this.condition = condition;
        this.quantity = quantity;
        this.status = status != null ? status : RequestStatus.PENDING;
        this.rejectionReason = rejectionReason;
        this.assignedPickupPerson = assignedPickupPerson;
    }

    // ---------------- Getters & Setters ----------------
    public Long getId() { return id; }
    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }



    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getMobileNo() { return mobileNo; }
    public void setMobileNo(String mobileNo) { this.mobileNo = mobileNo; }

    public String getPickupAddress() { return pickupAddress; }
    public void setPickupAddress(String pickupAddress) { this.pickupAddress = pickupAddress; }

    public DeviceType getDeviceType() { return deviceType; }
    public void setDeviceType(DeviceType deviceType) { this.deviceType = deviceType; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPickupDate() { return pickupDate; }
    public void setPickupDate(String pickupDate) { this.pickupDate = pickupDate; }

    public String getPickupTime() { return pickupTime; }
    public void setPickupTime(String pickupTime) { this.pickupTime = pickupTime; }

    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }

    public Condition getCondition() { return condition; }
    public void setCondition(Condition condition) { this.condition = condition; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public PickupPerson getAssignedPickupPerson() { return assignedPickupPerson; }
    public void setAssignedPickupPerson(PickupPerson assignedPickupPerson) {
        this.assignedPickupPerson = assignedPickupPerson;
    }

    // ---------------- OTP Getters & Setters ----------------
    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }

    public LocalDateTime getOtpGeneratedAt() { return otpGeneratedAt; }
    public void setOtpGeneratedAt(LocalDateTime otpGeneratedAt) { this.otpGeneratedAt = otpGeneratedAt; }

    // ---------------- Utility ----------------
    public boolean hasAssignedPickupPerson() {
        return assignedPickupPerson != null;
    }

    // ---------------- Debugging Support ----------------
    @Override
    public String toString() {
        return "PickupRequest{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", mobileNo='" + mobileNo + '\'' +
                ", pickupAddress='" + pickupAddress + '\'' +
                ", deviceType=" + deviceType +
                ", model='" + model + '\'' +
                ", pickupDate='" + pickupDate + '\'' +
                ", pickupTime='" + pickupTime + '\'' +
                ", photoPath='" + photoPath + '\'' +
                ", condition=" + condition +
                ", quantity=" + quantity +
                ", status=" + status +
                ", rejectionReason='" + rejectionReason + '\'' +
                ", assignedPickupPerson=" + (assignedPickupPerson != null ? assignedPickupPerson.getId() : null) +
                ", otp='" + otp + '\'' +
                ", otpGeneratedAt=" + otpGeneratedAt +
                '}';
    }
}
