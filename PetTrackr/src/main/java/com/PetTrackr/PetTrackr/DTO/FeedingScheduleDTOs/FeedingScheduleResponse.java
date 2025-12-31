package com.PetTrackr.PetTrackr.DTO.FeedingScheduleDTOs;

import java.time.LocalTime;
import com.PetTrackr.PetTrackr.entity.FeedingSchedule.QuantityUnit;

/**
 * DTO for returning feeding schedule information in API responses.
 * Used when retrieving feeding schedule details, including nested in PetDetailResponse.
 * Does not include the Pet object to avoid circular references.
 */
public class FeedingScheduleResponse {
    
    private Long id;
    private LocalTime time;
    private String foodType;
    private double quantity;
    private QuantityUnit quantityUnit;

    // Constructors
    public FeedingScheduleResponse() {
    }

    public FeedingScheduleResponse(Long id, LocalTime time, String foodType, 
                                    double quantity, QuantityUnit quantityUnit) {
        this.id = id;
        this.time = time;
        this.foodType = foodType;
        this.quantity = quantity;
        this.quantityUnit = quantityUnit;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
