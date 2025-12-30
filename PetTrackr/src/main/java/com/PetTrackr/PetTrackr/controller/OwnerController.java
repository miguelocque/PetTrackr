package com.PetTrackr.PetTrackr.controller;

import com.PetTrackr.PetTrackr.DTO.ErrorResponse;
import com.PetTrackr.PetTrackr.DTO.OwnerDTOs.OwnerRegistrationRequest;
import com.PetTrackr.PetTrackr.DTO.OwnerDTOs.OwnerResponse;
import com.PetTrackr.PetTrackr.DTO.OwnerDTOs.OwnerUpdateRequest;
import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.service.OwnerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * OwnerController handles all owner-related HTTP endpoints.
 * 
 * Design Principles:
 * 1. Separate request/response DTOs for security (password never in responses)
 * 2. Return detailed error messages in response bodies
 * 3. Use PATCH for partial updates (null fields ignored)
 * 4. Validation annotations on DTOs with @Valid
 * 5. Distinguish between 400 (validation) and 409 (conflict) errors
 * 
 * RESTful Conventions:
 * - POST for creation (201 Created)
 * - GET for retrieval (200 OK)
 * - PATCH for partial updates (200 OK)
 * - DELETE for removal (204 No Content)
 */
@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }
    
    // ========================================
    // Exception Handlers
    // ========================================
    
    /**
     * Handle validation errors from @Valid annotation.
     * Returns 400 Bad Request with details about which fields failed validation.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Validation Error",
                "Invalid input data",
                errors
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }

    
    // ========================================
    // UC-1: Register Owner Account
    // ========================================

    /**
     * Register a new owner account.
     * 
     * HTTP Status Codes:
     *   201 Created - Owner successfully created
     *   400 Bad Request - Validation error (handled by @Valid)
     *   409 Conflict - Email already registered
     * 
     * @param request registration data with validation
     * @return ResponseEntity with created owner (201), validation error (400), or conflict (409)
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerOwner(@Valid @RequestBody OwnerRegistrationRequest request) {
        try {
            Owner createdOwner = ownerService.registerOwner(
                    request.getEmail(),
                    request.getName(),
                    request.getPhoneNumber(),
                    request.getPassword()
            );
            
            OwnerResponse response = convertToResponse(createdOwner);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            // Check if it's an email conflict or other validation error
            if (e.getMessage().contains("already registered") || e.getMessage().contains("already exists")) {
                ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Conflict",
                        e.getMessage()
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }
            
            // Other validation errors (weak password, invalid format, etc.)
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    e.getMessage()
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ========================================
    // Get Owner Details
    // ========================================

    /**
     * Retrieve owner details by ID.
     * 
     * HTTP Status Codes:
     *   200 OK - Owner found and returned
     *   404 Not Found - Owner doesn't exist
     * 
     * @param ownerId the ID of the owner to retrieve
     * @return ResponseEntity with owner data (200) or error (404)
     */
    @GetMapping("/{ownerId}")
    public ResponseEntity<?> getOwner(@PathVariable Long ownerId) {
        try {
            Owner owner = ownerService.getOwnerById(ownerId);
            OwnerResponse response = convertToResponse(owner);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // ========================================
    // Update Owner Profile
    // ========================================

    /**
     * Update owner profile (partial update).
     * 
     * All fields are optional - only provided fields are updated.
     * Use PATCH for partial updates (PUT would require all fields).
     * 
     * HTTP Status Codes:
     *   200 OK - Owner updated and returned
     *   400 Bad Request - Validation error
     *   404 Not Found - Owner doesn't exist
     *   409 Conflict - Email already taken by another user
     * 
     * @param ownerId the ID of the owner to update
     * @param request update data (all fields optional)
     * @return ResponseEntity with updated owner (200) or error
     */
    @PatchMapping("/{ownerId}")
    public ResponseEntity<?> updateOwner(
            @PathVariable Long ownerId,
            @Valid @RequestBody OwnerUpdateRequest request) {
        
        try {
            Owner updatedOwner = ownerService.updateOwnerProfile(
                    ownerId,
                    request.getName(),
                    request.getPhoneNumber(),
                    request.getEmail()
            );
            
            OwnerResponse response = convertToResponse(updatedOwner);
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            // Distinguish between different error types
            String message = e.getMessage();
            
            if (message.contains("not found")) {
                ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        message
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }
            
            if (message.contains("already registered") || message.contains("already exists")) {
                ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.CONFLICT.value(),
                        "Conflict",
                        message
                );
                return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
            }
            
            // Other validation errors (invalid email format, etc.)
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    message
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }


    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Convert Owner entity to OwnerResponse DTO.
     * 
     * Note: In a larger application, use a dedicated mapper class (e.g., MapStruct)
     * for more complex conversions and better maintainability.
     * 
     * @param owner the owner entity to convert
     * @return OwnerResponse with safe fields (never includes password)
     */
    private OwnerResponse convertToResponse(Owner owner) {
        return new OwnerResponse(
                owner.getId(),
                owner.getName(),
                owner.getEmail(),
                owner.getPhoneNumber()
        );
    }

}