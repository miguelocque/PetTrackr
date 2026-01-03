package com.PetTrackr.PetTrackr.controller;

import com.PetTrackr.PetTrackr.DTO.ErrorResponse;
import com.PetTrackr.PetTrackr.DTO.FeedingScheduleDTOs.FeedingScheduleCreateRequest;
import com.PetTrackr.PetTrackr.DTO.FeedingScheduleDTOs.FeedingScheduleResponse;
import com.PetTrackr.PetTrackr.DTO.FeedingScheduleDTOs.FeedingScheduleUpdateRequest;
import com.PetTrackr.PetTrackr.entity.FeedingSchedule;
import com.PetTrackr.PetTrackr.service.FeedingScheduleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * FeedingScheduleController handles all feeding schedule-related HTTP endpoints.
 * 
 * Use Cases Implemented:
 *   UC-4: View All Feeding Schedules (via pet)
 *   UC-11: Add Feeding Schedule to Pet
 *   UC-12: View Feeding Schedules for a Single Pet
 *   Additional: Update feeding schedule, Delete feeding schedule
 * 
 * Design Principles:
 * 1. Nested under pet resource: /api/owners/{ownerId}/pets/{petId}/feeding-schedules
 * 2. Separate request/response DTOs
 * 3. Authorization checks via service layer (pet must belong to owner)
 * 4. Use PATCH for partial updates
 * 
 * RESTful Conventions:
 *   POST   /api/owners/{ownerId}/pets/{petId}/feeding-schedules                  - Add feeding schedule (201)
 *   GET    /api/owners/{ownerId}/pets/{petId}/feeding-schedules                  - List all feeding schedules (200)
 *   GET    /api/owners/{ownerId}/pets/{petId}/feeding-schedules/{scheduleId}     - Get single feeding schedule (200)
 *   PATCH  /api/owners/{ownerId}/pets/{petId}/feeding-schedules/{scheduleId}     - Update feeding schedule (200)
 *   DELETE /api/owners/{ownerId}/pets/{petId}/feeding-schedules/{scheduleId}     - Delete feeding schedule (204)
 */
@RestController
@RequestMapping("/api/owners/{ownerId}/pets/{petId}/feeding-schedules")
public class FeedingScheduleController {

    private final FeedingScheduleService feedingScheduleService;

    public FeedingScheduleController(FeedingScheduleService feedingScheduleService) {
        this.feedingScheduleService = feedingScheduleService;
    }

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
     * Add a new feeding schedule to a pet.
     * 
     * HTTP Status Codes:
     *   201 Created - Feeding schedule successfully added
     *   400 Bad Request - Validation error
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Pet doesn't exist
     */
    @PostMapping
    public ResponseEntity<?> addFeedingSchedule(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @Valid @RequestBody FeedingScheduleCreateRequest request) {

        try {
            FeedingSchedule createdSchedule = feedingScheduleService.addFeedingScheduleToPet(
                    petId,
                    ownerId,
                    request.getTime(),
                    request.getFoodType(),
                    request.getQuantityUnit(),
                    request.getQuantity()
            );

            FeedingScheduleResponse response = convertToResponse(createdSchedule);
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
     * Get all feeding schedules for a pet, ordered by time.
     * 
     * HTTP Status Codes:
     *   200 OK - List of feeding schedules returned (may be empty)
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Pet doesn't exist
     */
    @GetMapping
    public ResponseEntity<?> getFeedingSchedules(
            @PathVariable Long ownerId,
            @PathVariable Long petId) {

        try {
            List<FeedingSchedule> schedules = feedingScheduleService.getFeedingSchedulesForPet(petId, ownerId);

            List<FeedingScheduleResponse> responses = schedules.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(responses);

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
     * Get a single feeding schedule by ID.
     * 
     * HTTP Status Codes:
     *   200 OK - Feeding schedule returned
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Feeding schedule or pet doesn't exist
     */
    @GetMapping("/{scheduleId}")
    public ResponseEntity<?> getFeedingSchedule(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @PathVariable Long scheduleId) {

        try {
            // Fetch all schedules for the pet and filter by scheduleId
            List<FeedingSchedule> schedules = feedingScheduleService.getFeedingSchedulesForPet(petId, ownerId);
            
            FeedingSchedule schedule = schedules.stream()
                    .filter(s -> s.getId().equals(scheduleId))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Feeding schedule not found with id: " + scheduleId));

            FeedingScheduleResponse response = convertToResponse(schedule);
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
     * Update an existing feeding schedule.
     * Only provided (non-null) fields will be updated.
     * 
     * HTTP Status Codes:
     *   200 OK - Feeding schedule updated
     *   400 Bad Request - Validation error
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Feeding schedule or pet doesn't exist
     */
    @PatchMapping("/{scheduleId}")
    public ResponseEntity<?> updateFeedingSchedule(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @PathVariable Long scheduleId,
            @Valid @RequestBody FeedingScheduleUpdateRequest request) {

        try {
            // Convert nullable Double to primitive double, using 0 to indicate no update
            double quantity = request.getQuantity() != null ? request.getQuantity() : 0;

            FeedingSchedule updatedSchedule = feedingScheduleService.updateFeedingSchedule(
                    scheduleId,
                    ownerId,
                    request.getTime(),
                    request.getFoodType(),
                    request.getQuantityUnit(),
                    quantity
            );

            FeedingScheduleResponse response = convertToResponse(updatedSchedule);
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
     * Delete a feeding schedule.
     * 
     * HTTP Status Codes:
     *   204 No Content - Feeding schedule deleted
     *   403 Forbidden - Pet doesn't belong to owner
     *   404 Not Found - Feeding schedule or pet doesn't exist
     */
    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<?> deleteFeedingSchedule(
            @PathVariable Long ownerId,
            @PathVariable Long petId,
            @PathVariable Long scheduleId) {

        try {
            feedingScheduleService.deleteFeedingSchedule(scheduleId, ownerId);
            return ResponseEntity.noContent().build();

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
    // Helper Methods
    // ========================================

    /**
     * Convert FeedingSchedule entity to FeedingScheduleResponse DTO.
     */
    private FeedingScheduleResponse convertToResponse(FeedingSchedule schedule) {
        return new FeedingScheduleResponse(
                schedule.getId(),
                schedule.getTime(),
                schedule.getFoodType(),
                schedule.getQuantity(),
                schedule.getQuantityUnit()
        );
    }
}
