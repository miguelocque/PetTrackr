package com.PetTrackr.PetTrackr.service;

import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.repository.OwnerRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
@Transactional
public class OwnerService {
    
    // final variables for repository and password encoder
    private final OwnerRepository ownerRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    // Email regex pattern for validation
    private static final String EMAIL_PATTERN = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    // phone number regex pattern for validation (optional enhancement)
    private static final String PHONE_PATTERN = "^(\\+\\d{1,3}[- ]?)?\\d{10}$";
    private static final Pattern phonePattern = Pattern.compile(PHONE_PATTERN);
    
    // constructor injection
    public OwnerService(OwnerRepository ownerRepository, BCryptPasswordEncoder passwordEncoder) {
        this.ownerRepository = ownerRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    // helper method to validate email format
    private boolean isValidEmail(String email) {
        return email != null && pattern.matcher(email).matches();
    }

    // helper method to validate phone number format (optional enhancement)
    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phonePattern.matcher(phoneNumber).matches();
    }
    
    // registers a new owner
    public Owner registerOwner(String email, String name, String phoneNumber, String rawPassword) {
        // firstly validate the email to ensure no spaces and lowercase
        email = email != null ? email.trim().toLowerCase() : null;

        // phone number trim
        phoneNumber = phoneNumber != null ? phoneNumber.trim() : null;
        
        // Validate inputs
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email format is invalid");
        }
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new IllegalArgumentException("Phone number format is invalid");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be empty");
        }
        if (rawPassword.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters");
        }
        
        // Validate email uniqueness
        if (ownerRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        
        // Hash password using BCrypt
        String hashedPassword = passwordEncoder.encode(rawPassword);
        
        // Create and save new owner
        Owner owner = new Owner();
        owner.setEmail(email);
        owner.setName(name);
        owner.setPhoneNumber(phoneNumber);
        owner.setPasswordHash(hashedPassword);
        
        return ownerRepository.save(owner);
    }
    
    // get owner by ID -- throws exception if not found
    public Owner getOwnerById(Long id) {
        return ownerRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found with ID: " + id));
    }
    
    // get owner by email -- returns Optional in the case of not found
    public Optional<Owner> getOwnerByEmail(String email) {
        return ownerRepository.findByEmail(email);
    }
    
    // updates an owner's profile with a new name and phone number, no other fields
    public Owner updateOwnerProfile(Long id, String name, String phoneNumber) {
        Owner owner = getOwnerById(id);
        
        if (name != null && !name.isBlank()) {
            owner.setName(name);
        }
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            owner.setPhoneNumber(phoneNumber);
        }
        
        return ownerRepository.save(owner);
    }
    
    // verifies an owner's password -- used for login
    public boolean verifyPassword(String email, String rawPassword) {
        Optional<Owner> owner = getOwnerByEmail(email);
        if (owner.isEmpty()) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, owner.get().getPasswordHash());
    }
}