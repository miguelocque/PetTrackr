package com.PetTrackr.PetTrackr.DTO.PetDTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

import com.PetTrackr.PetTrackr.entity.Pet.ActivityLevel;
import com.PetTrackr.PetTrackr.entity.Pet.WeightType;


/**
 * DTO for creating a new pet.
 * 
 * Includes validation constraints for automatic validation by Spring.
 */
public class PetCreateRequest {
    
    @NotBlank(message = "Pet name is required")
    @Size(min = 1, max = 100, message = "Pet name must be between 1 and 100 characters")
    private String name;
    
    @NotBlank(message = "Pet type is required")
    @Size(min = 1, max = 50, message = "Pet type must be between 1 and 50 characters")
    private String type;

    @NotBlank(message = "Pet breed is required")
    @Size(min = 1, max = 50, message = "Pet breed must be between 1 and 50 characters")
    private String breed;

    @Positive(message = "Pet weight must be greater than zero")
    private double weight;

    @NotNull(message = "Pet weight unit is required")
    private WeightType weightType;

    @NotNull(message = "Activity level is required")
    private ActivityLevel activityLevel;

    @NotNull(message = "Date of birth is required")
    @PastOrPresent(message = "Date of birth must be in the past or present")
    private LocalDate dateOfBirth;

    // *** Owner ID comes from path variable, not part of DTO ***
    
    // Constructors
    
    public PetCreateRequest() {
    }
    
    public PetCreateRequest(String name, String type, String breed, double weight, WeightType weightType,
                            ActivityLevel activityLevel, LocalDate dateOfBirth) {
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.weight = weight;
        this.weightType = weightType;
        this.activityLevel = activityLevel;
        this.dateOfBirth = dateOfBirth;
    }
    
    // Getters & Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public WeightType getWeightType() {
        return weightType;
    }

    public void setWeightType(WeightType weightType) {
        this.weightType = weightType;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

}