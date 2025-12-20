package com.PetTrackr.PetTrackr.entity;


import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.JoinColumn;

@Entity
public class VetVisit {
    // attributes
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate visitDate;

    @Column(nullable = true) // can be null if no next visit scheduled
    private LocalDate nextVisitDate;

    @Column(nullable = false)
    private String vetName;

    @Column(nullable = false)
    private String reasonForVisit;

    @Column(nullable = true)
    private String notes; // optional notes about the visit

    @ManyToOne
    @JoinColumn(name = "petId", nullable = false)
    private Pet pet; // reference to the pet for the visit

    // constructors
    public VetVisit() {
        // empty constructor for JPA
    }

    public VetVisit(LocalDate visitDate, LocalDate nextVisitDate, String vetName, String reasonForVisit, String notes) {
        this.visitDate = visitDate;
        this.nextVisitDate = nextVisitDate;
        this.vetName = vetName;
        this.reasonForVisit = reasonForVisit;
        this.notes = notes;
    }

    // getters and setters
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

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }
}