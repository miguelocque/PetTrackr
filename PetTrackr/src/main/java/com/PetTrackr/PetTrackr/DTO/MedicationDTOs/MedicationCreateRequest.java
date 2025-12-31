package com.PetTrackr.PetTrackr.DTO.MedicationDTOs;

import java.time.LocalDate;
import java.time.LocalTime;

import com.PetTrackr.PetTrackr.entity.Medication.DosageUnit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new medication.
 * Used when adding a medication to a pet (UC-7).
 * 
 * Required fields: name, dosage, frequency, timeToAdminister, startDate
 * Optional fields: endDate (null for ongoing medications)
 */
public class MedicationCreateRequest {

    @NotBlank(message = "Medication name is required")
    @Size(min = 1, max = 100, message = "Medication name must be between 1 and 100 characters")
    private String name;

    @Positive(message = "Dosage amount must be positive")
    private double dosageAmount;

    @NotNull(message = "Dosage unit is required")
    private DosageUnit dosageUnit;

    @NotBlank(message = "Frequency is required")
    @Size(min = 1, max = 100, message = "Frequency must be between 1 and 100 characters")
    private String frequency;

    @NotNull(message = "Time to administer is required")
    private LocalTime timeToAdminister;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate; // Optional - null for ongoing medications

    // Constructors
    public MedicationCreateRequest() {
    }

    public MedicationCreateRequest(String name, double dosageAmount, DosageUnit dosageUnit, String frequency,
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

    public double getDosageAmount() {
        return dosageAmount;
    }

    public void setDosageAmount(double dosageAmount) {
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
