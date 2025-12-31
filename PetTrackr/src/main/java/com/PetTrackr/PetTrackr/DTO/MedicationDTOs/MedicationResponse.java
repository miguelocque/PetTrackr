package com.PetTrackr.PetTrackr.DTO.MedicationDTOs;

import java.time.LocalDate;
import java.time.LocalTime;

import com.PetTrackr.PetTrackr.entity.Medication.DosageUnit;

/**
 * DTO for returning medication information in API responses.
 * Used when retrieving medication details, including nested in PetDetailResponse.
 * Does not include the Pet object to avoid circular references.
 */
public class MedicationResponse {
    
    private Long id;
    private String name;
    private double dosageAmount;
    private DosageUnit dosageUnit;
    private String frequency;
    private LocalTime timeToAdminister;
    private LocalDate startDate;
    private LocalDate endDate; // Nullable - null for ongoing medications

    // Constructors
    public MedicationResponse() {
    }

    public MedicationResponse(Long id, String name, double dosageAmount, DosageUnit dosageUnit, String frequency,
                              LocalTime timeToAdminister, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.name = name;
        this.dosageAmount = dosageAmount;
        this.dosageUnit = dosageUnit;
        this.frequency = frequency;
        this.timeToAdminister = timeToAdminister;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
