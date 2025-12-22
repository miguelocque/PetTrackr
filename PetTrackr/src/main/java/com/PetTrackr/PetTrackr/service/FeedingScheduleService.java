package com.PetTrackr.PetTrackr.service;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import com.PetTrackr.PetTrackr.entity.FeedingSchedule;
import com.PetTrackr.PetTrackr.entity.Pet;
import com.PetTrackr.PetTrackr.repository.FeedingScheduleRepository;

import java.util.List;

@Service
@Transactional
public class FeedingScheduleService {
    // Implement feeding schedule use cases 4, 11, 12 here
    // 4 : view all feeding schedules for all pets of an owner
    // 11: add feeding schedule for a pet
    // 12: view feeding schedules for a single pet

    private final FeedingScheduleRepository feedingScheduleRepository;
    private final PetService petService; // to verify pet existence

    public FeedingScheduleService(FeedingScheduleRepository feedingScheduleRepository, PetService petService) {
        this.feedingScheduleRepository = feedingScheduleRepository;
        this.petService = petService;
    }

    public FeedingSchedule addFeedingScheduleToPet(Long petId, Long requestingOwnerId, java.time.LocalTime time, String foodType,
                                                    FeedingSchedule.QuantityUnit quantityUnit, double quantity) {
        // Verify pet exists and belongs to requesting owner
        Pet pet = petService.getPetById(petId, requestingOwnerId);

        // Trim foodType
        foodType = foodType != null ? foodType.trim() : null;

        // Validate inputs
        if (quantityUnit == null) {
            throw new IllegalArgumentException("Quantity unit cannot be null");
        }
        if (time == null) {
            throw new IllegalArgumentException("Feeding time cannot be null");
        }
        if (foodType == null || foodType.isBlank()) {
            throw new IllegalArgumentException("Food type cannot be empty");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }

        FeedingSchedule schedule = new FeedingSchedule();
        schedule.setPet(pet);
        schedule.setTime(time);
        schedule.setFoodType(foodType);
        schedule.setQuantityUnit(quantityUnit);
        schedule.setQuantity(quantity);
        return feedingScheduleRepository.save(schedule);
    }

    // update and delete methods can be added similarly
    public FeedingSchedule updateFeedingSchedule(Long scheduleId, Long requestingOwnerId, java.time.LocalTime time, String foodType,
                                                 FeedingSchedule.QuantityUnit quantityUnit, double quantity) {
        // Fetch feeding schedule
        FeedingSchedule schedule = feedingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Feeding schedule not found with id: " + scheduleId));

        // Verify pet existence and ownership
        petService.getPetById(schedule.getPet().getId(), requestingOwnerId);

        // Trim foodType
        foodType = foodType != null ? foodType.trim() : null;

        // Update fields if new values are provided
        if (time != null) {
            schedule.setTime(time);
        }
        if (foodType != null && !foodType.isBlank()) {
             schedule.setFoodType(foodType);
        }
        if (quantityUnit != null) {
            schedule.setQuantityUnit(quantityUnit);
        }
        if (quantity > 0) {
            schedule.setQuantity(quantity);
        }

        return feedingScheduleRepository.save(schedule);
    }

    public FeedingSchedule deleteFeedingSchedule(Long scheduleId, Long requestingOwnerId) {
        // Fetch feeding schedule
        FeedingSchedule schedule = feedingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new IllegalArgumentException("Feeding schedule not found with id: " + scheduleId));

        // Verify pet existence and ownership
        petService.getPetById(schedule.getPet().getId(), requestingOwnerId);

        feedingScheduleRepository.delete(schedule);
        return schedule;
    }

    // view feeding schedules for a single pet
    public List<FeedingSchedule> getFeedingSchedulesForPet(Long petId, Long requestingOwnerId) {
        // Verify pet exists and belongs to requesting owner
        petService.getPetById(petId, requestingOwnerId);

        // Fetch and return feeding schedules sorted by time
        return feedingScheduleRepository.findByPetIdOrderByTimeAsc(petId);
    }
}