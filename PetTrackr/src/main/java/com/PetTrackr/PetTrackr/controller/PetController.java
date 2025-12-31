package com.PetTrackr.PetTrackr.controller;

import com.PetTrackr.PetTrackr.DTO.ErrorResponse;
import com.PetTrackr.PetTrackr.DTO.FeedingScheduleDTOs.FeedingScheduleResponse;
import com.PetTrackr.PetTrackr.DTO.MedicationDTOs.MedicationResponse;
import com.PetTrackr.PetTrackr.DTO.PetDTOs.PetCreateRequest;
import com.PetTrackr.PetTrackr.DTO.PetDTOs.PetDetailedResponse;
import com.PetTrackr.PetTrackr.DTO.PetDTOs.PetSummaryResponse;
import com.PetTrackr.PetTrackr.DTO.PetDTOs.PetUpdateRequest;
import com.PetTrackr.PetTrackr.DTO.VetVisitDTOs.VetVisitResponse;
import com.PetTrackr.PetTrackr.entity.FeedingSchedule;
import com.PetTrackr.PetTrackr.entity.Medication;
import com.PetTrackr.PetTrackr.entity.Pet;
import com.PetTrackr.PetTrackr.entity.VetVisit;
import com.PetTrackr.PetTrackr.service.PetService;
import com.PetTrackr.PetTrackr.service.QRCodeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * PetController handles all pet-related HTTP endpoints.
 * 
 * Use Cases Implemented:
 *   UC-2: Create Pet Profile
 *   UC-3: View All Owner's Pets (Dashboard)
 *   UC-4: View Pet Details (Profile Page)
 *   UC-5: Upload Pet Image
 *   UC-6: Generate QR Code for Pet
 *   Additional: Update Pet, Delete Pet
 * 
 * Design Principles:
 * 1. Separate request/response DTOs for different use cases
 * 2. Return detailed error messages in response bodies
 * 3. Use PATCH for partial updates
 * 4. Validation annotations on DTOs with @Valid
 * 5. Authorization checks (pet must belong to owner)
 * 6. Distinguish between 400/403/404 errors appropriately
 * 
 * RESTful Conventions:
 *   POST   /api/owners/{ownerId}/pets          - Create pet (201 Created)
 *   GET    /api/owners/{ownerId}/pets          - List owner's pets (200 OK)
 *   GET    /api/owners/{ownerId}/pets/{petId}  - Get pet details (200 OK)
 *   PATCH  /api/owners/{ownerId}/pets/{petId}  - Update pet (200 OK)
 *   DELETE /api/owners/{ownerId}/pets/{petId}  - Delete pet (204 No Content)
 *   POST   /api/owners/{ownerId}/pets/{petId}/photo - Upload photo (200 OK)
 *   GET    /api/owners/{ownerId}/pets/{petId}/qr-code - Generate QR (200 OK)
 */
@RestController
@RequestMapping("/api/owners/{ownerId}/pets")
public class PetController {

    private final PetService petService;
    private final QRCodeService qrCodeService;

    public PetController(PetService petService, QRCodeService qrCodeService) {
        this.petService = petService;
        this.qrCodeService = qrCodeService;
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
    // UC-2: Create Pet Profile
    // ========================================

    /**
     * Create a new pet for the specified owner.
     * 
     * HTTP Status Codes:
     *   201 Created - Pet successfully created
     *   400 Bad Request - Validation error
     *   404 Not Found - Owner doesn't exist
     * 
     * @param ownerId the owner creating the pet
     * @param request pet creation data with validation
     * @return ResponseEntity with created pet summary (201) or error
     */
    @PostMapping
    public ResponseEntity<?> createPet(
            @PathVariable Long ownerId,
            @Valid @RequestBody PetCreateRequest request) {
        
        try {
            Pet createdPet = petService.createPet(
                    ownerId,
                    request.getName(),
                    request.getType(),
                    request.getBreed(),
                    request.getWeight(),
                    request.getWeightType(),
                    request.getDateOfBirth(),
                    request.getActivityLevel()
            );

            PetSummaryResponse response = convertToSummaryResponse(createdPet);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            String message = e.getMessage();

            if (message.contains("Owner not found")) {
                ErrorResponse errorResponse = new ErrorResponse(
                        HttpStatus.NOT_FOUND.value(),
                        "Not Found",
                        message
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
            }

            // Other validation errors
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    message
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ========================================
    // UC-3: View All Owner's Pets (Dashboard)
    // ========================================

    /**
     * Get all pets for the specified owner.
     * Returns lightweight summary DTOs for dashboard display.
     * 
     * HTTP Status Codes:
     *   200 OK - List of pets returned (may be empty)
     *   404 Not Found - Owner doesn't exist
     * 
     * @param ownerId the owner whose pets to retrieve
     * @return ResponseEntity with list of pet summaries
     */
    @GetMapping
    public ResponseEntity<?> getAllPets(@PathVariable Long ownerId) {
        try {
            List<Pet> pets = petService.getAllPetsByOwnerId(ownerId);

            List<PetSummaryResponse> response = pets.stream()
                    .map(this::convertToSummaryResponse)
                    .collect(Collectors.toList());

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
    // UC-4: View Pet Details (Profile Page)
    // ========================================

    /**
     * Get detailed information for a specific pet.
     * Includes nested medications, feeding schedules, and vet visits.
     * 
     * HTTP Status Codes:
     *   200 OK - Pet details returned
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Pet doesn't exist
     * 
     * @param ownerId the owner requesting the pet details
     * @param petId the pet to retrieve
     * @return ResponseEntity with detailed pet info or error
     */
    @GetMapping("/{petId}")
    public ResponseEntity<?> getPetDetails(
            @PathVariable Long ownerId,
            @PathVariable Long petId) {
        
        try {
            Pet pet = petService.getPetById(petId, ownerId);
            PetDetailedResponse response = convertToDetailedResponse(pet);
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

    // ========================================
    // Update Pet Profile
    // ========================================

    /**
     * Update pet profile (partial update).
     * All fields are optional - only provided fields are updated.
     * 
     * HTTP Status Codes:
     *   200 OK - Pet updated and returned
     *   400 Bad Request - Validation error
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Pet doesn't exist
     * 
     * @param ownerId the owner making the update
     * @param petId the pet to update
     * @param request update data (all fields optional)
     * @return ResponseEntity with updated pet or error
     */
    @PatchMapping("/{petId}")
    public ResponseEntity<?> updatePet(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @Valid @RequestBody PetUpdateRequest request) {
        
        try {
            Pet updatedPet = petService.updatePet(
                    petId,
                    ownerId,
                    request.getName(),
                    request.getType(),
                    request.getBreed(),
                    request.getDateOfBirth(),
                    request.getWeight(),
                    request.getWeightType(),
                    request.getActivityLevel()
            );

            PetDetailedResponse response = convertToDetailedResponse(updatedPet);
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

            // Other validation errors
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    message
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ========================================
    // Delete Pet Profile
    // ========================================

    /**
     * Delete a pet profile.
     * Cascades to delete all related medications, feeding schedules, and vet visits.
     * 
     * HTTP Status Codes:
     *   204 No Content - Pet successfully deleted
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Pet doesn't exist
     * 
     * @param ownerId the owner requesting deletion
     * @param petId the pet to delete
     * @return ResponseEntity with no content (204) or error
     */
    @DeleteMapping("/{petId}")
    public ResponseEntity<?> deletePet(
            @PathVariable Long ownerId,
            @PathVariable Long petId) {
        
        try {
            petService.deletePet(petId, ownerId);
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
    // UC-5: Upload Pet Image
    // ========================================

    /**
     * Upload a photo for the pet.
     * Accepts multipart/form-data with a file named "photo".
     * 
     * HTTP Status Codes:
     *   200 OK - Photo uploaded, returns updated pet
     *   400 Bad Request - Invalid file type or size
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Pet doesn't exist
     *   413 Payload Too Large - File exceeds size limit
     * 
     * @param ownerId the owner uploading the photo
     * @param petId the pet to update
     * @param photo the image file (JPG/PNG, max 5MB)
     * @return ResponseEntity with updated pet summary or error
     */
    @PostMapping("/{petId}/photo")
    public ResponseEntity<?> uploadPetPhoto(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @RequestParam("photo") MultipartFile photo) {
        
        try {
            Pet updatedPet = petService.updatePetPhoto(petId, ownerId, photo);
            PetSummaryResponse response = convertToSummaryResponse(updatedPet);
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

            if (message.contains("size") || message.contains("large")) {
                ErrorResponse errorResponse = new ErrorResponse(
                        413,
                        "Payload Too Large",
                        message
                );
                return ResponseEntity.status(413).body(errorResponse);
            }

            // Invalid file type or other validation errors
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.BAD_REQUEST.value(),
                    "Bad Request",
                    message
            );
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    // ========================================
    // UC-6: Generate QR Code for Pet
    // ========================================

    /**
     * Generate a QR code containing pet emergency contact information.
     * Returns a downloadable PNG image (300x300px).
     * 
     * HTTP Status Codes:
     *   200 OK - QR code generated and returned as PNG
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Pet doesn't exist
     *   500 Internal Server Error - QR generation failed
     * 
     * @param ownerId the owner requesting the QR code
     * @param petId the pet to generate QR for
     * @return ResponseEntity with PNG image bytes or error
     */
    @GetMapping("/{petId}/qr-code")
    public ResponseEntity<?> generateQRCode(
            @PathVariable Long ownerId,
            @PathVariable Long petId) {
        
        try {
            byte[] qrCodeImage = qrCodeService.generateQRCodeForPet(petId, ownerId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_PNG);
            headers.setContentDispositionFormData("attachment", "pet_" + petId + "_qr.png");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(qrCodeImage);

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

        } catch (RuntimeException e) {
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Internal Server Error",
                    "Failed to generate QR code: " + e.getMessage()
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    // ========================================
    // Helper Methods - Convert Entity to DTOs
    // ========================================

    /**
     * Convert Pet entity to PetSummaryResponse DTO.
     * Used for dashboard lists and creation responses.
     */
    private PetSummaryResponse convertToSummaryResponse(Pet pet) {
        return new PetSummaryResponse(
                pet.getId(),
                pet.getName(),
                pet.getType(),
                pet.getBreed(),
                pet.getDateOfBirth(),
                pet.getPhotoURL()
        );
    }

    /**
     * Convert Pet entity to PetDetailedResponse DTO.
     * Includes nested collections for medications, feeding schedules, and vet visits.
     */
    private PetDetailedResponse convertToDetailedResponse(Pet pet) {
        // Convert nested collections to response DTOs
        List<MedicationResponse> medications = pet.getMedications().stream()
                .map(this::convertToMedicationResponse)
                .collect(Collectors.toList());

        List<FeedingScheduleResponse> feedingSchedules = pet.getFeedingSchedule().stream()
                .map(this::convertToFeedingScheduleResponse)
                .collect(Collectors.toList());

        List<VetVisitResponse> vetVisits = pet.getVetAppointments().stream()
                .map(this::convertToVetVisitResponse)
                .collect(Collectors.toList());

        return new PetDetailedResponse(
                pet.getId(),
                pet.getName(),
                pet.getType(),
                pet.getBreed(),
                pet.getAge(),
                pet.getWeight(),
                pet.getWeightType(),
                pet.getDateOfBirth(),
                pet.getPhotoURL(),
                pet.getActivityLevel(),
                medications,
                feedingSchedules,
                vetVisits
        );
    }

    /**
     * Convert Medication entity to MedicationResponse DTO.
     */
    private MedicationResponse convertToMedicationResponse(Medication medication) {
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

    /**
     * Convert FeedingSchedule entity to FeedingScheduleResponse DTO.
     */
    private FeedingScheduleResponse convertToFeedingScheduleResponse(FeedingSchedule feedingSchedule) {
        return new FeedingScheduleResponse(
                feedingSchedule.getId(),
                feedingSchedule.getTime(),
                feedingSchedule.getFoodType(),
                feedingSchedule.getQuantity(),
                feedingSchedule.getQuantityUnit()
        );
    }

    /**
     * Convert VetVisit entity to VetVisitResponse DTO.
     */
    private VetVisitResponse convertToVetVisitResponse(VetVisit vetVisit) {
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