package com.PetTrackr.PetTrackr.controller;

import com.PetTrackr.PetTrackr.DTO.ErrorResponse;
import com.PetTrackr.PetTrackr.DTO.VetVisitDTOs.VetVisitCreateRequest;
import com.PetTrackr.PetTrackr.DTO.VetVisitDTOs.VetVisitResponse;
import com.PetTrackr.PetTrackr.DTO.VetVisitDTOs.VetVisitUpdateRequest;
import com.PetTrackr.PetTrackr.entity.VetVisit;
import com.PetTrackr.PetTrackr.service.VetVisitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * VetVisitController handles all vet visit-related HTTP endpoints.
 * 
 * Use Cases Implemented:
 *   UC-9: Add Vet Visit
 *   UC-10: View Vet Visit History
 *   Additional: Get single visit, Update visit, Delete visit
 * 
 * Design Principles:
 * 1. Nested under pet resource: /api/owners/{ownerId}/pets/{petId}/vet-visits
 * 2. Separate request/response DTOs
 * 3. Authorization checks via service layer (pet must belong to owner)
 * 4. Use PATCH for partial updates
 * 
 * RESTful Conventions:
 *   POST   /api/owners/{ownerId}/pets/{petId}/vet-visits              - Add vet visit (201)
 *   GET    /api/owners/{ownerId}/pets/{petId}/vet-visits              - List all visits (200)
 *   GET    /api/owners/{ownerId}/pets/{petId}/vet-visits/{visitId}    - Get single visit (200)
 *   PATCH  /api/owners/{ownerId}/pets/{petId}/vet-visits/{visitId}    - Update visit (200)
 *   DELETE /api/owners/{ownerId}/pets/{petId}/vet-visits/{visitId}    - Delete visit (204)
 */
@RestController
@RequestMapping("/api/owners/{ownerId}/pets/{petId}/vet-visits")
public class VetVisitController {

    private final VetVisitService vetVisitService;

    public VetVisitController(VetVisitService vetVisitService) {
        this.vetVisitService = vetVisitService;
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
     * Add a new vet visit to a pet. -- use case 9
     * 
     * HTTP Status Codes:
     *   201 Created - Vet visit successfully added
     *   400 Bad Request - Validation error
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Pet doesn't exist
     */
    @PostMapping
    public ResponseEntity<?> addVetVisit(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @Valid @RequestBody VetVisitCreateRequest request) {

        try {
            VetVisit createdVisit = vetVisitService.addVetVisitToPet(
                    petId,
                    ownerId,
                    request.getVisitDate(),
                    request.getNextVisitDate(),
                    request.getReasonForVisit(),
                    request.getVetName(),
                    request.getNotes()
            );

            VetVisitResponse response = convertToResponse(createdVisit);
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
     * Get all vet visits for a pet in chronological order. -- use case 10
     * 
     * HTTP Status Codes:
     *   200 OK - List of vet visits returned (may be empty)
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Pet doesn't exist
     */
    @GetMapping
    public ResponseEntity<?> getVetVisitHistory(
            @PathVariable Long ownerId,
            @PathVariable Long petId) {

        try {
            List<VetVisit> visits = vetVisitService.getVetVisitsForPet(petId, ownerId);

            List<VetVisitResponse> response = visits.stream()
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
     * Get details for a specific vet visit. - not in use cases but useful
     * 
     * HTTP Status Codes:
     *   200 OK - Vet visit details returned
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Vet visit or pet doesn't exist
     * 
     */
    @GetMapping("/{visitId}")
    public ResponseEntity<?> getVetVisit(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @PathVariable Long visitId) {

        try {
            VetVisit visit = vetVisitService.getVetVisitById(visitId, ownerId);
            VetVisitResponse response = convertToResponse(visit);
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
     * Update an existing vet visit (partial update).
     * All fields are optional - only provided fields are updated. 
     *  - not in use cases but useful
     * 
     * HTTP Status Codes:
     *   200 OK - Vet visit updated
     *   400 Bad Request - Validation error
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Vet visit or pet doesn't exist
     * 
     */
    @PatchMapping("/{visitId}")
    public ResponseEntity<?> updateVetVisit(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @PathVariable Long visitId,
            @Valid @RequestBody VetVisitUpdateRequest request) {

        try {
            VetVisit updatedVisit = vetVisitService.updateVetVisit(
                    visitId,
                    ownerId,
                    request.getVisitDate(),
                    request.getNextVisitDate(),
                    request.getReasonForVisit(),
                    request.getVetName(),
                    request.getNotes()
            );

            VetVisitResponse response = convertToResponse(updatedVisit);
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

    // ========================================
    // Delete Vet Visit
    // ========================================

    /**
     * Delete a vet visit.
     * 
     * HTTP Status Codes:
     *   204 No Content - Vet visit successfully deleted
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Vet visit or pet doesn't exist
     */
    @DeleteMapping("/{visitId}")
    public ResponseEntity<?> deleteVetVisit(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @PathVariable Long visitId) {

        try {
            vetVisitService.deleteVetVisit(visitId, ownerId);
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
     * Convert VetVisit entity to VetVisitResponse DTO.
     */
    private VetVisitResponse convertToResponse(VetVisit vetVisit) {
        return new VetVisitResponse(
                vetVisit.getId(),
                vetVisit.getVisitDate(),
                vetVisit.getNextVisitDate(),
                vetVisit.getVetName(),
                vetVisit.getReasonForVisit(),
                vetVisit.getNotes()
        );
    }
}