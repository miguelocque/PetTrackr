package com.PetTrackr.PetTrackr.DTO.OwnerDTOs;

/**
 * DTO for owner responses.
 * 
 * Never includes password or sensitive data.
 * Used for all owner-related API responses.
 */
public class OwnerResponse {
    
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    
    // Constructors
    
    public OwnerResponse() {
    }
    
    public OwnerResponse(Long id, String name, String email, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
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
