package com.example.ewaste.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Secret key (must be strong enough for HS256)
    private static final String SECRET = "mySuperSecretKeyForJwtValidation12345";
    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    // Token validity (10 hours)
    private static final long EXPIRATION_TIME = 1000 * 60 * 60 * 10;

    /**
     * ✅ Generate JWT token with email + role (role can be USER, ADMIN, PICKUP_PERSON).
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)
                .claim("role", role) // Store role inside token
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract all claims from token.
     */
    public Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Extract email (subject).
     */
    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    /**
     * Alias for extractEmail.
     */
    public String extractUsername(String token) {
        return extractEmail(token);
    }

    /**
     * Extract role (USER / ADMIN / PICKUP_PERSON).
     */
    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    /**
     * Validate token with email.
     */
    public boolean validateToken(String token, String email) {
        try {
            String extractedEmail = extractEmail(token);
            return (extractedEmail.equals(email) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Check token expiry.
     */
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}
