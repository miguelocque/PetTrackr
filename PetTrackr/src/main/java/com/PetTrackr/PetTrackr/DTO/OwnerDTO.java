package com.PetTrackr.PetTrackr.DTO;

/**
 * OwnerDTO (Data Transfer Object) for API requests/responses.
 * 
 * Purpose: Controls what owner data is exposed via REST API.
 * Benefits:
 * - Never expose password hash to clients
 * - Decouple API contract from database entity structure
 * - Can add validation annotations specific to API (not entity)
 * - Easy to add/remove fields without database migration
 * 
 * Design: Includes all owner-visible fields except password hash.
 */
public class OwnerDTO {
    
    private Long id;
    private String name;
    private String email;
    private String phoneNumber;
    private String password;  // Only used in registration/update requests, never in responses
    
    // ========================================
    // Constructors
    // ========================================
    
    public OwnerDTO() {
        // empty constructor required for JPA/JSON serialization
    }
    
    public OwnerDTO(String name, String email, String phoneNumber, String password) {
        this.name = name;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }
    
    // ========================================
    // Getters & Setters
    // ========================================
    
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
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
}
