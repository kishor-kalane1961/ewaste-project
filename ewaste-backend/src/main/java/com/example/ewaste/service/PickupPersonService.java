package com.example.ewaste.service;

import com.example.ewaste.model.PickupPerson;
import com.example.ewaste.repository.PickupPersonRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PickupPersonService {

    private final PickupPersonRepository pickupPersonRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public PickupPersonService(PickupPersonRepository pickupPersonRepository) {
        this.pickupPersonRepository = pickupPersonRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(); // Initialize password encoder
    }

    // -------------------- CRUD --------------------
    public List<PickupPerson> getAllPersons() {
        return pickupPersonRepository.findAll();
    }

    public Optional<PickupPerson> getPersonById(Long id) {
        return pickupPersonRepository.findById(id);
    }

    public PickupPerson createPerson(PickupPerson person) {
        if (pickupPersonRepository.existsByEmail(person.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        // Hash password before saving
        person.setPassword(passwordEncoder.encode(person.getPassword()));
        return pickupPersonRepository.save(person);
    }

    public PickupPerson updatePerson(Long id, PickupPerson updatedPerson) {
        return pickupPersonRepository.findById(id)
                .map(person -> {
                    person.setName(updatedPerson.getName());
                    person.setEmail(updatedPerson.getEmail());
                    person.setMobile(updatedPerson.getMobile());
                    // Update password if provided
                    if (updatedPerson.getPassword() != null && !updatedPerson.getPassword().isEmpty()) {
                        person.setPassword(passwordEncoder.encode(updatedPerson.getPassword()));
                    }
                    return pickupPersonRepository.save(person);
                })
                .orElseThrow(() -> new RuntimeException("Pickup person not found"));
    }

    public void deletePerson(Long id) {
        pickupPersonRepository.deleteById(id);
    }

    // -------------------- Find PickupPerson by email --------------------
    public Optional<PickupPerson> getByEmail(String email) {
        return pickupPersonRepository.findByEmail(email);
    }

    // -------------------- Validate login --------------------
    public boolean validateLogin(String email, String rawPassword) {
        Optional<PickupPerson> personOpt = pickupPersonRepository.findByEmail(email);
        if (personOpt.isEmpty()) return false;
        PickupPerson person = personOpt.get();
        return passwordEncoder.matches(rawPassword, person.getPassword());
    }
}
