package com.PetTrackr.PetTrackr.controller;

import com.PetTrackr.PetTrackr.DTO.OwnerDTO;
import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.service.OwnerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * OwnerController handles all owner-related HTTP endpoints.
 * 
 * Design Principles:
 * 1. Use DTOs for request/response (don't expose JPA entities directly)
 * 2. Let service layer handle all business logic and validation
 * 3. Controller is responsible for HTTP concerns only (routing, status codes, content negotiation)
 * 4. All error handling delegated to global exception handler (future implementation)
 * 5. Authorization checks happen in service layer via getPetById() pattern
 * 
 * RESTful Conventions:
 * - POST for creation (201 Created)
 * - GET for retrieval (200 OK)
 * - PUT for full updates (200 OK)
 * - DELETE for removal (204 No Content or 200 OK with response)
 */
@RestController
@RequestMapping("/api/owners")
public class OwnerController {

    private final OwnerService ownerService;

    public OwnerController(OwnerService ownerService) {
        this.ownerService = ownerService;
    }

    /**
     * Register a new owner account.
     * 
     * Request: OwnerDTO with name, email, phoneNumber, password
     * Response: Registered OwnerDTO with generated ID
     * Error Cases:
     *   - Email already exists → 409 Conflict
     *   - Invalid email format → 400 Bad Request
     *   - Weak password → 400 Bad Request
     *   - Missing required fields → 400 Bad Request
     * 
     * HTTP Status Codes:
     *   201 Created - Owner successfully created
     *   400 Bad Request - Validation error
     *   409 Conflict - Email already registered
     */
    @PostMapping("/register")
    public ResponseEntity<OwnerDTO> registerOwner(@Valid @RequestBody OwnerDTO ownerDTO) {
        try {
            Owner createdOwner = ownerService.registerOwner(ownerDTO.getEmail(), 
                                                            ownerDTO.getName(), 
                                                            ownerDTO.getPhoneNumber(), 
                                                            ownerDTO.getPassword());
            
            // Convert entity to DTO and return 201 Created
            OwnerDTO responseDTO = convertToDTO(createdOwner);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
            
        } catch (IllegalArgumentException e) {
            // Email already exists or validation failed
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    // ========================================
    // Get Owner Details
    // ========================================

    /**
     * Retrieve owner details by ID.
     * 
     * Note: In a real app with authentication, you'd verify the requesting user
     * matches the requested owner ID for security. For now, this is open access.
     * 
     * HTTP Status Codes:
     *   200 OK - Owner found and returned
     *   404 Not Found - Owner doesn't exist
     */
    @GetMapping("/{ownerId}")
    public ResponseEntity<OwnerDTO> getOwner(@PathVariable Long ownerId) {
        try {
            Owner owner = ownerService.getOwnerById(ownerId);
            OwnerDTO responseDTO = convertToDTO(owner);
            return ResponseEntity.ok(responseDTO);
            
        } catch (IllegalArgumentException e) {
            // Owner not found
            return ResponseEntity.notFound().build();
        }
    }

    // ========================================
    // Update Owner Profile
    // ========================================

    /**
     * Update owner profile (partial update).
     * 
     * Allows updating name and phoneNumber. Email and password changes would be
     * handled by separate endpoints for security and audit purposes.
     * 
     * Design: Null fields are ignored (partial update pattern) in service, so client only
     * sends fields they want to update.
     * 
     * HTTP Status Codes:
     *   200 OK - Owner updated and returned
     *   404 Not Found - Owner doesn't exist
     *   400 Bad Request - Invalid data
     */
    @PutMapping("/{ownerId}")
    public ResponseEntity<OwnerDTO> updateOwner(
            @PathVariable Long ownerId,
            @Valid @RequestBody OwnerDTO updateDTO) {
        
        try {
            Owner updatedOwner = ownerService.updateOwnerProfile(
                ownerId,
                updateDTO.getName(),
                updateDTO.getPhoneNumber(),
                updateDTO.getEmail()
            );
            
            OwnerDTO responseDTO = convertToDTO(updatedOwner);
            return ResponseEntity.ok(responseDTO);
            
        } catch (IllegalArgumentException e) {
            // Owner not found or validation failed
            if (e.getMessage().contains("not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().build();
        }
    }


    // ** HELPER METHOD ** 

    // Convert Owner entity to OwnerDTO for API response.
    private OwnerDTO convertToDTO(Owner owner) {
        OwnerDTO dto = new OwnerDTO();
        dto.setId(owner.getId());
        dto.setName(owner.getName());
        dto.setEmail(owner.getEmail());
        dto.setPhoneNumber(owner.getPhoneNumber());
        // NOTE: Never include password hash in response
        return dto;
    }

}