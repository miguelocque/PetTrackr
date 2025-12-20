package com.PetTrackr.PetTrackr.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.GenerationType;

import jakarta.persistence.ManyToOne;


@Entity
public class Medication {
    // attributes

    //primary key will be an ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String dosage;

    @Column(nullable = false)
    private String frequency;

    @Column(nullable = false)
    private LocalTime timeToAdminister;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = true) // can be null if ongoing
    private LocalDate endDate;

    // reference to Pet - many-to-one relationship
    @ManyToOne
    @JoinColumn(name = "petId", nullable = false)
    private Pet pet;

    // constructors
    public Medication() {
        // empty constructor for JPA
    }

    public Medication(String name, String dosage, String frequency, LocalTime timeToAdminister, LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.timeToAdminister = timeToAdminister;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    // getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

}