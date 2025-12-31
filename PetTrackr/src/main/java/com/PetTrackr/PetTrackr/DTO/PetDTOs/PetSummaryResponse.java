package com.PetTrackr.PetTrackr.DTO.PetDTOs;

import java.time.LocalDate;

/**
 * DTO for retrieving the basic summary information of a pet.
 * Used for the dashboard view (UC-3) where only essential details are needed.
 * Includes pet ID, name, type, breed, date of birth, and optional photo URL.
 * Perfect for list views where multiple pets are displayed.
 */
public class PetSummaryResponse {
    private Long id; // Needed to link to detail page
    private String name;
    private String type;
    private String breed;
    private LocalDate dateOfBirth;
    private String photoURL; // Optional photo URL

    // Constructors
    public PetSummaryResponse() {
    }

    public PetSummaryResponse(Long id, String name, String type, String breed, LocalDate dateOfBirth, String photoURL) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.dateOfBirth = dateOfBirth;
        this.photoURL = photoURL;
    }

    // Getters & Setters

    // id
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    // name
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    // type
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    // breed
    public String getBreed() {
        return breed;
    }
    public void setBreed(String breed) {
        this.breed = breed;
    }

    // dateOfBirth
    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }
    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    // photoURL
    public String getPhotoURL() {
        return photoURL;
    }
    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

}