package com.PetTrackr.PetTrackr.controller;

import com.PetTrackr.PetTrackr.DTO.ErrorResponse;
import com.PetTrackr.PetTrackr.DTO.OwnerDTOs.OwnerResponse;
import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.repository.OwnerRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * AuthController handles login/logout and session management.
 * Uses Spring Security session-based authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final OwnerRepository ownerRepository;

    public AuthController(AuthenticationManager authenticationManager, OwnerRepository ownerRepository) {
        this.authenticationManager = authenticationManager;
        this.ownerRepository = ownerRepository;
    }

    /**
     * Login with email and password.
     * Returns owner info on success, 401 on failure.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpSession session) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        
        // Normalize email to lowercase for case-insensitive login
        email = email != null ? email.trim().toLowerCase() : null;

        if (email == null || password == null) {
            return ResponseEntity.badRequest().body(new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    "Email and password are required"
            ));
        }

        try {
            // Authenticate with Spring Security
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );

            // Set authentication in security context
            SecurityContextHolder.getContext().setAuthentication(auth);

            // Fetch owner to return info
            Owner owner = ownerRepository.findByEmail(email).orElseThrow();

            // Store owner ID in session for easy access
            session.setAttribute("ownerId", owner.getId());

            OwnerResponse response = new OwnerResponse(
                    owner.getId(),
                    owner.getEmail(),
                    owner.getName(),
                    owner.getPhoneNumber()
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unauthorized",
                    "Invalid email or password"
            ));
        }
    }

    /**
     * Logout - invalidate session.
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    /**
     * Get current authenticated user info.
     * Returns 401 if not authenticated.
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unauthorized",
                    "Not logged in"
            ));
        }

        String email = auth.getName();
        Owner owner = ownerRepository.findByEmail(email).orElse(null);

        if (owner == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse(
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unauthorized",
                    "User not found"
            ));
        }

        OwnerResponse response = new OwnerResponse(
                owner.getId(),
                owner.getEmail(),
                owner.getName(),
                owner.getPhoneNumber()
        );

        return ResponseEntity.ok(response);
    }
}
