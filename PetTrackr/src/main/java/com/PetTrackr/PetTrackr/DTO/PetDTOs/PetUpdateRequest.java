package com.PetTrackr.PetTrackr.DTO.PetDTOs;

import java.time.LocalDate;

import com.PetTrackr.PetTrackr.entity.Pet.ActivityLevel;
import com.PetTrackr.PetTrackr.entity.Pet.WeightType;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing pet.
 * 
 * All fields are optional; only provided (non-null) fields will be updated.
 * When fields are provided, they are validated for format (but not nullability).
 */
public class PetUpdateRequest {

    @Size(min = 1, max = 100, message = "Pet name must be between 1 and 100 characters")
    private String name;
    
    @Size(min = 1, max = 50, message = "Pet type must be between 1 and 50 characters")
    private String type;
    
    @Size(min = 1, max = 50, message = "Pet breed must be between 1 and 50 characters")
    private String breed;

    @Positive(message = "Pet weight must be greater than zero")
    private Double weight; // Use Double wrapper to allow null (optional update)
    
    private WeightType weightType;
    
    private ActivityLevel activityLevel;
    
    @PastOrPresent(message = "Date of birth must be in the past or present")
    private LocalDate dateOfBirth;

    // Constructors
    public PetUpdateRequest() {
    }

    public PetUpdateRequest(String name, String type, String breed, Double weight,
                            WeightType weightType, ActivityLevel activityLevel, LocalDate dateOfBirth) {
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.weight = weight;
        this.weightType = weightType;
        this.activityLevel = activityLevel;
        this.dateOfBirth = dateOfBirth;
    }

    // Getters & Setters

    // name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // type
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    // breed
    public String getBreed() {
        return breed;
    }
    public void setBreed(String breed) {
        this.breed = breed;
    }

    // weight
    public Double getWeight() {
        return weight;
    }
    public void setWeight(Double weight) {
        this.weight = weight;
    }

    // weightType
    public WeightType getWeightType() {
        return weightType;
    }
    public void setWeightType(WeightType weightType) {
        this.weightType = weightType;
    }

    // activityLevel
    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }
    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    // dob
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

}