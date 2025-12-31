package com.PetTrackr.PetTrackr.DTO.VetVisitDTOs;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating a new vet visit.
 * Used when adding a vet visit to a pet (UC-9).
 * 
 * Required fields: visitDate, vetName, reasonForVisit
 * Optional fields: nextVisitDate, notes
 */
public class VetVisitCreateRequest {

    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;

    private LocalDate nextVisitDate; // Optional - next scheduled appointment

    @NotBlank(message = "Vet name is required")
    @Size(min = 1, max = 100, message = "Vet name must be between 1 and 100 characters")
    private String vetName;

    @NotBlank(message = "Reason for visit is required")
    @Size(min = 1, max = 255, message = "Reason must be between 1 and 255 characters")
    private String reasonForVisit;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes; // Optional

    // Constructors
    public VetVisitCreateRequest() {
    }

    public VetVisitCreateRequest(LocalDate visitDate, LocalDate nextVisitDate, String vetName,
                                  String reasonForVisit, String notes) {
        this.visitDate = visitDate;
        this.nextVisitDate = nextVisitDate;
        this.vetName = vetName;
        this.reasonForVisit = reasonForVisit;
        this.notes = notes;
    }

    // Getters & Setters

    public LocalDate getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(LocalDate visitDate) {
        this.visitDate = visitDate;
    }

    public LocalDate getNextVisitDate() {
        return nextVisitDate;
    }

    public void setNextVisitDate(LocalDate nextVisitDate) {
        this.nextVisitDate = nextVisitDate;
    }

    public String getVetName() {
        return vetName;
    }

    public void setVetName(String vetName) {
        this.vetName = vetName;
    }

    public String getReasonForVisit() {
        return reasonForVisit;
    }

    public void setReasonForVisit(String reasonForVisit) {
        this.reasonForVisit = reasonForVisit;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
