package com.PetTrackr.PetTrackr.DTO.VetVisitDTOs;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;

/**
 * DTO for updating an existing vet visit.
 * 
 * All fields are optional; only provided (non-null) fields will be updated.
 * When fields are provided, they are validated for format.
 */
public class VetVisitUpdateRequest {

    private LocalDate visitDate;

    private LocalDate nextVisitDate;

    @Size(min = 1, max = 100, message = "Vet name must be between 1 and 100 characters")
    private String vetName;

    @Size(min = 1, max = 255, message = "Reason must be between 1 and 255 characters")
    private String reasonForVisit;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;

    // Constructors
    public VetVisitUpdateRequest() {
    }

    public VetVisitUpdateRequest(LocalDate visitDate, LocalDate nextVisitDate, String vetName,
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
