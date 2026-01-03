package com.PetTrackr.PetTrackr.DTO.FeedingScheduleDTOs;

import java.time.LocalTime;

import com.PetTrackr.PetTrackr.entity.FeedingSchedule.QuantityUnit;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing feeding schedule.
 * 
 * All fields are optional; only provided (non-null) fields will be updated.
 * When fields are provided, they are validated for format.
 */
public class FeedingScheduleUpdateRequest {

    private LocalTime time;

    @Size(min = 1, max = 100, message = "Food type must be between 1 and 100 characters")
    private String foodType;

    @Positive(message = "Quantity must be positive")
    private Double quantity; // nullable wrapper for optional updates

    private QuantityUnit quantityUnit; // nullable for optional updates

    // Constructors
    public FeedingScheduleUpdateRequest() {
    }

    public FeedingScheduleUpdateRequest(LocalTime time, String foodType, Double quantity, QuantityUnit quantityUnit) {
        this.time = time;
        this.foodType = foodType;
        this.quantity = quantity;
        this.quantityUnit = quantityUnit;
    }

    // Getters & Setters

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getFoodType() {
        return foodType;
    }

    public void setFoodType(String foodType) {
        this.foodType = foodType;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(QuantityUnit quantityUnit) {
        this.quantityUnit = quantityUnit;
    }
}
