package com.PetTrackr.PetTrackr.DTO.OwnerDTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for owner profile update requests.
 * 
 * All fields are optional (partial update).
 * Does not include password (use separate endpoint for password changes).
 */
public class OwnerUpdateRequest {
    
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    private String name;
    
    @Email(message = "Email must be valid")
    private String email;
    
    @Pattern(regexp = "^[0-9\\-\\+\\(\\)\\s]{7,20}$", message = "Phone number must be valid")
    private String phoneNumber;
    
    // Constructors
    
    public OwnerUpdateRequest() {
    }
    
    public OwnerUpdateRequest(String name, String email, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }
    
    // Getters & Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhoneNumber() {
        return phoneNumber;
    }
    
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
