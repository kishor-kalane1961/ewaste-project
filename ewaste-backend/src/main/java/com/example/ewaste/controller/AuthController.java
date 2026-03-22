package com.example.ewaste.controller;
import com.example.ewaste.model.PickupPerson;
import com.example.ewaste.model.Role;
import com.example.ewaste.model.User;
import com.example.ewaste.service.PickupPersonService;
import com.example.ewaste.service.UserService;
import com.example.ewaste.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173") // React frontend URL
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private UserService userService;

    @Autowired
    private PickupPersonService pickupPersonService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String email = loginRequest.get("email");
        String password = loginRequest.get("password");

        Map<String, Object> response = new HashMap<>();

        try {
            // 1️⃣ Try User / Admin login
            try {
                User user = userService.loginUser(email, password);
                String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

                response.put("token", token);
                response.put("email", user.getEmail());
                response.put("role", user.getRole().name());

                // ✅ Log once at successful login
                logger.info("User [{}] logged in successfully with role [{}]", user.getEmail(), user.getRole().name());

                return ResponseEntity.ok(response);
            } catch (Exception ignored) {
                // User/Admin not found → try PickupPerson
            }

            // 2️⃣ Try PickupPerson login
            if (pickupPersonService.validateLogin(email, password)) {
                PickupPerson person = pickupPersonService.getByEmail(email)
                        .orElseThrow(() -> new RuntimeException("PickupPerson not found"));

                String token = jwtUtil.generateToken(person.getEmail(), "PICKUP_PERSON");

                response.put("token", token);
                response.put("email", person.getEmail());
                response.put("role", "PICKUP_PERSON");

                logger.info("PickupPerson [{}] logged in successfully", person.getEmail());

                return ResponseEntity.ok(response);
            }

            // 3️⃣ Invalid credentials
            logger.warn("Failed login attempt for email [{}]", email);
            return ResponseEntity.status(401).body(Map.of("message", "Invalid email or password"));

        } catch (Exception e) {
            logger.error("Login failed for email [{}]: {}", email, e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("message", "Login failed: " + e.getMessage()));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            Role role = (user.getRole() != null) ? user.getRole() : Role.USER;

            User savedUser = userService.registerUser(
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    role
            );

            String token = jwtUtil.generateToken(savedUser.getEmail(), savedUser.getRole().name());

            Map<String, Object> response = new HashMap<>();
            response.put("token", token);
            response.put("email", savedUser.getEmail());
            response.put("role", savedUser.getRole().name());

            // ✅ Log once at registration
            logger.info("New user [{}] registered with role [{}]", savedUser.getEmail(), savedUser.getRole().name());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error during registration for email [{}]: {}", user.getEmail(), e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "Error during registration: " + e.getMessage()));
        }
    }
}
