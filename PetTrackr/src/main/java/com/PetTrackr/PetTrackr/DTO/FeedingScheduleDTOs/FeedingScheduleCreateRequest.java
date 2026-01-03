package com.PetTrackr.PetTrackr.DTO.FeedingScheduleDTOs;

import java.time.LocalTime;

import com.PetTrackr.PetTrackr.entity.FeedingSchedule.QuantityUnit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new feeding schedule.
 * Used when adding a feeding schedule to a pet (UC-11).
 * 
 * Required fields: time, foodType, quantity, quantityUnit
 */
public class FeedingScheduleCreateRequest {

    @NotNull(message = "Feeding time is required")
    private LocalTime time;

    @NotBlank(message = "Food type is required")
    @Size(min = 1, max = 100, message = "Food type must be between 1 and 100 characters")
    private String foodType;

    @Positive(message = "Quantity must be positive")
    private double quantity;

    @NotNull(message = "Quantity unit is required")
    private QuantityUnit quantityUnit;

    // Constructors
    public FeedingScheduleCreateRequest() {
    }

    public FeedingScheduleCreateRequest(LocalTime time, String foodType, double quantity, QuantityUnit quantityUnit) {
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

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(QuantityUnit quantityUnit) {
        this.quantityUnit = quantityUnit;
    }
}
