package com.PetTrackr.PetTrackr.controller;

import com.PetTrackr.PetTrackr.DTO.ErrorResponse;
import com.PetTrackr.PetTrackr.DTO.MedicationDTOs.MedicationCreateRequest;
import com.PetTrackr.PetTrackr.DTO.MedicationDTOs.MedicationResponse;
import com.PetTrackr.PetTrackr.DTO.MedicationDTOs.MedicationUpdateRequest;
import com.PetTrackr.PetTrackr.entity.Medication;
import com.PetTrackr.PetTrackr.service.MedicationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * MedicationController handles all medication-related HTTP endpoints.
 * 
 * Use Cases Implemented:
 *   UC-7: Add Medication to Pet
 *   UC-8: View Pet Medications
 *   Additional: Get single medication, Update medication, Delete medication
 * 
 * Design Principles:
 * 1. Nested under pet resource: /api/owners/{ownerId}/pets/{petId}/medications
 * 2. Separate request/response DTOs
 * 3. Authorization checks via service layer (pet must belong to owner)
 * 4. Use PATCH for partial updates
 * 5. Delete available for removing incorrect entries; use PATCH to set endDate for discontinuing
 * 
 * RESTful Conventions:
 *   POST   /api/owners/{ownerId}/pets/{petId}/medications               - Add medication (201)
 *   GET    /api/owners/{ownerId}/pets/{petId}/medications               - List all medications (200)
 *   GET    /api/owners/{ownerId}/pets/{petId}/medications/{medicationId} - Get single medication (200)
 *   PATCH  /api/owners/{ownerId}/pets/{petId}/medications/{medicationId} - Update medication (200)
 *   DELETE /api/owners/{ownerId}/pets/{petId}/medications/{medicationId} - Delete medication (204)
 */
@RestController
@RequestMapping("/api/owners/{ownerId}/pets/{petId}/medications")
public class MedicationController {

    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    // ========================================
    // Exception Handlers
    // ========================================

    /**
     * Handle validation errors from @Valid annotation.
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

    /**
     * Add a new medication to a pet.
     * 
     * HTTP Status Codes:
     *   201 Created - Medication successfully added
     *   400 Bad Request - Validation error
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Pet doesn't exist
     */
    @PostMapping
    public ResponseEntity<?> addMedication(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @Valid @RequestBody MedicationCreateRequest request) {

        try {
            Medication createdMedication = medicationService.addMedicationToPet(
                    petId,
                    ownerId,
                    request.getName(),
                    request.getDosageAmount(),
                    request.getDosageUnit(),
                    request.getFrequency(),
                    request.getTimeToAdminister(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            MedicationResponse response = convertToResponse(createdMedication);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (SecurityException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "Forbidden",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

        } catch (IllegalArgumentException e) {
            String message = e.getMessage();

            if (message.contains("not found")) {
                ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        message
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    message
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Get all medications for a pet, ordered by time to administer.
     * 
     * HTTP Status Codes:
     *   200 OK - List of medications returned (may be empty)
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Pet doesn't exist
     */
    @GetMapping
    public ResponseEntity<?> getMedications(
            @PathVariable Long ownerId,
            @PathVariable Long petId) {

        try {
            List<Medication> medications = medicationService.getMedicationsForPet(petId, ownerId);

            List<MedicationResponse> response = medications.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "Forbidden",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Get details for a specific medication.
     * 
     * HTTP Status Codes:
     *   200 OK - Medication details returned
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Medication or pet doesn't exist
     * 
     */
    @GetMapping("/{medicationId}")
    public ResponseEntity<?> getMedication(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @PathVariable Long medicationId) {

        try {
            // Use update method with all nulls to just fetch and verify authorization
            // Or we could add a getMedicationById method to service - for now use getMedicationsForPet
            List<Medication> medications = medicationService.getMedicationsForPet(petId, ownerId);
            
            Medication medication = medications.stream()
                    .filter(m -> m.getId().equals(medicationId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Medication not found with id: " + medicationId));

            MedicationResponse response = convertToResponse(medication);
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "Forbidden",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

        } catch (IllegalArgumentException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Not Found",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Update an existing medication (partial update).
     * All fields are optional - only provided fields are updated.
     * 
     * Use this to discontinue a medication by setting endDate.
     * 
     * HTTP Status Codes:
     *   200 OK - Medication updated
     *   400 Bad Request - Validation error (e.g., endDate before startDate)
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Medication or pet doesn't exist
     */
    @PatchMapping("/{medicationId}")
    public ResponseEntity<?> updateMedication(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @PathVariable Long medicationId,
            @Valid @RequestBody MedicationUpdateRequest request) {

        try {
            Medication updatedMedication = medicationService.updateMedication(
                    medicationId,
                    ownerId,
                    request.getName(),
                    request.getDosageAmount(),
                    request.getDosageUnit(),
                    request.getFrequency(),
                    request.getTimeToAdminister(),
                    request.getStartDate(),
                    request.getEndDate()
            );

            MedicationResponse response = convertToResponse(updatedMedication);
            return ResponseEntity.ok(response);

        } catch (SecurityException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "Forbidden",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

        } catch (IllegalArgumentException e) {
            String message = e.getMessage();

            if (message.contains("not found")) {
                ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        message
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    message
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Delete a medication.
     * 
     * Use this to remove incorrectly entered medications or clean up old data.
     * For discontinuing a medication while preserving history, use PATCH to set endDate instead.
     * 
     * HTTP Status Codes:
     *   204 No Content - Medication deleted successfully
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Medication or pet doesn't exist
     */
    @DeleteMapping("/{medicationId}")
    public ResponseEntity<?> deleteMedication(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @PathVariable Long medicationId) {

        try {
            medicationService.deleteMedication(medicationId, ownerId);
            return ResponseEntity.noContent().build();

        } catch (SecurityException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "Forbidden",
                    e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);

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
    // Helper Methods
    // ========================================

    /**
     * Convert Medication entity to MedicationResponse DTO.
     */
    private MedicationResponse convertToResponse(Medication medication) {
        return new MedicationResponse(
                medication.getId(),
                medication.getName(),
                medication.getDosageAmount(),
                medication.getDosageUnit(),
                medication.getFrequency(),
                medication.getTimeToAdminister(),
                medication.getStartDate(),
                medication.getEndDate()
        );
    }
}