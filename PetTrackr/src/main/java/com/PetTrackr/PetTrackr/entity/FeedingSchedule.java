package com.PetTrackr.PetTrackr.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.EnumType;


// an entity to represent a feeding schedule for a pet
// a pet will have multiple feeding schedules (e.g., breakfast, lunch, dinner)
@Entity
public class FeedingSchedule {
    // quantity unit enum
    public enum QuantityUnit {
        CUPS,
        GRAMS,
        OUNCES
    }
    // attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String time; // e.g., "08:00 AM"

    @Column(nullable = false)
    private String foodType; // e.g., "Dry Kibble"

    @Column(nullable = false)
    private double quantity; // e.g., 1.5

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuantityUnit quantityUnit; // e.g., "cups"

    @ManyToOne
    @JoinColumn(name = "petId", nullable = false)
    private Pet pet; // reference to the pet a given schedule belongs to

    // constructors
    public FeedingSchedule() {
        // empty constructor for JPA
    }

    public FeedingSchedule(String time, String foodType, double quantity, QuantityUnit quantityUnit) {
        this.time = time;
        this.foodType = foodType;
        this.quantity = quantity;
        this.quantityUnit = quantityUnit;
    }

    // getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
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

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }
}