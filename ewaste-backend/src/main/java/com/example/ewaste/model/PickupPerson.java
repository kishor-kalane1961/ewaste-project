package com.example.ewaste.model;

import jakarta.persistence.*;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "pickup_persons")
public class PickupPerson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    private String mobile;

    @Column(nullable = false)
    private String password; // <-- New password field

    // Availability flag (optional but useful)
    private boolean available = true;

    // One person can handle many requests
    @OneToMany(mappedBy = "assignedPickupPerson", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<PickupRequest> pickupRequests;

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getPassword() { return password; }  // Getter for password
    public void setPassword(String password) { this.password = password; } // Setter for password

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

}
