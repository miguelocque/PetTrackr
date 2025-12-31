package com.PetTrackr.PetTrackr.DTO.MedicationDTOs;

import java.time.LocalDate;
import java.time.LocalTime;

import com.PetTrackr.PetTrackr.entity.Medication.DosageUnit;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing medication.
 * 
 * All fields are optional; only provided (non-null) fields will be updated.
 * When fields are provided, they are validated for format.
 */
public class MedicationUpdateRequest {

    @Size(min = 1, max = 100, message = "Medication name must be between 1 and 100 characters")
    private String name;

    @Positive(message = "Dosage amount must be positive")
    private Double dosageAmount; // nullable wrapper for optional updates

    private DosageUnit dosageUnit; // nullable for optional updates

    @Size(min = 1, max = 100, message = "Frequency must be between 1 and 100 characters")
    private String frequency;

    private LocalTime timeToAdminister;

    private LocalDate startDate;

    private LocalDate endDate;

    // Constructors
    public MedicationUpdateRequest() {
    }

    public MedicationUpdateRequest(String name, Double dosageAmount, DosageUnit dosageUnit, String frequency,
                                    LocalTime timeToAdminister, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.dosageAmount = dosageAmount;
        this.dosageUnit = dosageUnit;
        this.frequency = frequency;
        this.timeToAdminister = timeToAdminister;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters & Setters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getDosageAmount() {
        return dosageAmount;
    }

    public void setDosageAmount(Double dosageAmount) {
        this.dosageAmount = dosageAmount;
    }

    public DosageUnit getDosageUnit() {
        return dosageUnit;
    }

    public void setDosageUnit(DosageUnit dosageUnit) {
        this.dosageUnit = dosageUnit;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public LocalTime getTimeToAdminister() {
        return timeToAdminister;
    }

    public void setTimeToAdminister(LocalTime timeToAdminister) {
        this.timeToAdminister = timeToAdminister;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }
}
