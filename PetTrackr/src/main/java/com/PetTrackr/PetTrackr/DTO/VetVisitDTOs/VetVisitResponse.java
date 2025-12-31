package com.PetTrackr.PetTrackr.DTO.VetVisitDTOs;

import java.time.LocalDate;

/**
 * DTO for returning vet visit information in API responses.
 * Used when retrieving vet visit details, including nested in PetDetailResponse.
 * Does not include the Pet object to avoid circular references.
 */
public class VetVisitResponse {
    
    private Long id;
    private LocalDate visitDate;
    private LocalDate nextVisitDate; // Nullable - future appointment if scheduled
    private String vetName;
    private String reasonForVisit;
    private String notes; // Nullable - optional visit notes

    // Constructors
    public VetVisitResponse() {
    }

    public VetVisitResponse(Long id, LocalDate visitDate, LocalDate nextVisitDate, 
                            String vetName, String reasonForVisit, String notes) {
        this.id = id;
        this.visitDate = visitDate;
        this.nextVisitDate = nextVisitDate;
        this.vetName = vetName;
        this.reasonForVisit = reasonForVisit;
        this.notes = notes;
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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