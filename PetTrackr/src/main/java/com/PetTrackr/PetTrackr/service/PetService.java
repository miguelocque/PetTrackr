package com.PetTrackr.PetTrackr.service;

import java.time.LocalDate;

import org.springframework.stereotype.Service;

import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.entity.Pet;
import com.PetTrackr.PetTrackr.repository.OwnerRepository;
import com.PetTrackr.PetTrackr.repository.PetRepository;

import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;


@Service
@Transactional
public class PetService {
    private final PetRepository petRepository;
    private final OwnerRepository ownerRepository;
    private final ImageUploadService imageUploadService;

    // Constructor injection
    public PetService(PetRepository petRepository, OwnerRepository ownerRepository, ImageUploadService imageUploadService) {
        this.petRepository = petRepository;
        this.ownerRepository = ownerRepository;
        this.imageUploadService = imageUploadService;
    }

    // Creates a new pet profile for the given owner -- Implements Use Case-2
    public Pet createPet(Long ownerId, String name, String type, String breed, 
                        int age, double weight, Pet.WeightType weightType,
                        LocalDate dateOfBirth, Pet.ActivityLevel activityLevel) {
        
        // Validate the owner exists
        Owner owner = ownerRepository.findById(ownerId)
            .orElseThrow(() -> new IllegalArgumentException("Owner not found with ID: " + ownerId));
        
        // Validate required fields
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pet name cannot be empty");
        }
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("Pet type cannot be empty");
        }
        if (breed == null || breed.isBlank()) {
            throw new IllegalArgumentException("Pet breed cannot be empty");
        }
        
        // Validate the numeric fields
        if (age < 0) {
            throw new IllegalArgumentException("Pet age cannot be negative");
        }
        if (weight <= 0) {
            throw new IllegalArgumentException("Pet weight must be greater than zero");
        }
        
        // Validate date of birth (shouldn't be in the future)
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth cannot be in the future");
        }
        
        // Validate enums
        if (weightType == null) {
            throw new IllegalArgumentException("Weight type must be specified (KG or LBS)");
        }
        if (activityLevel == null) {
            throw new IllegalArgumentException("Activity level must be specified");
        }
        
        // Finally, after validation, create and save pet
        Pet pet = new Pet();
        pet.setName(name);
        pet.setType(type);
        pet.setBreed(breed);
        pet.setAge(age);
        pet.setWeight(weight);
        pet.setWeightType(weightType);
        pet.setDateOfBirth(dateOfBirth);
        pet.setActivityLevel(activityLevel);
        pet.setOwner(owner);

        // ** photoURL is null by default (will be set on image upload) **
        
        return petRepository.save(pet);
    }

    // Get all pets for a given owner -- Implements Use Case-3
    public List<Pet> getAllPetsByOwnerId(Long ownerId) {
        // Verify owner exists first
        if (!ownerRepository.existsById(ownerId)) {
            throw new IllegalArgumentException("Owner not found with ID: " + ownerId);
        }
        
        // then retrieve and return all pets by owner ID
        return petRepository.findByOwnerId(ownerId);
    }


    // Retrieve a single pet's details with authorization - implements Use Case-4 (viewing an individual pet profile)
    public Pet getPetById(Long petId, Long requestingOwnerId) {
        // Retrieve pet from repository by ID
        Pet pet = petRepository.findById(petId)
            // throw exception if not found
            .orElseThrow(() -> new IllegalArgumentException("Pet not found with ID: " + petId));
        
        // Authorization check to verify pet belongs to requesting owner
        if (!pet.getOwner().getId().equals(requestingOwnerId)) {
            throw new SecurityException("Access denied: Pet does not belong to this owner");
        }
        
        // Return pet details if authorized
        return pet;
    }

    // Update an existing pet's profile -- not specified in use cases but necessary for completeness
    public Pet updatePet(Long petId, Long requestingOwnerId,
                        String name, String type, String breed,
                        Integer age, Double weight, Pet.WeightType weightType,
                        Pet.ActivityLevel activityLevel) {
        
        // Get pet with authorization check
        Pet pet = getPetById(petId, requestingOwnerId);
        
        // Update fields only if provided (null = no change)
        if (name != null && !name.isBlank()) {
            pet.setName(name);
        }
        if (type != null && !type.isBlank()) {
            pet.setType(type);
        }
        if (breed != null && !breed.isBlank()) {
            pet.setBreed(breed);
        }
        if (age != null) {
            if (age < 0) {
                throw new IllegalArgumentException("Age cannot be negative");
            }
            pet.setAge(age);
        }
        if (weight != null) {
            if (weight <= 0) {
                throw new IllegalArgumentException("Weight must be greater than zero");
            }
            pet.setWeight(weight);
        }
        if (weightType != null) {
            pet.setWeightType(weightType);
        }
        if (activityLevel != null) {
            pet.setActivityLevel(activityLevel);
        }
        
        // Save and return the updated pet
        return petRepository.save(pet);
    }

    // Delete a pet profile - not specified in use cases but necessary for completeness
    public void deletePet(Long petId, Long requestingOwnerId) {
        // Get pet with authorization check
        Pet pet = getPetById(petId, requestingOwnerId);
        
        // Delete (cascades to related entities due to @OneToMany cascade settings)
        petRepository.delete(pet);
    }


    // Update pet photo URL after image upload -- Implements Use Case-5
    public Pet updatePetPhoto(Long petId, Long requestingOwnerId, MultipartFile photoFile) {
   
        // Fetch pet and authorize
        Pet pet = getPetById(petId, requestingOwnerId);
    
        // Upload image via ImageUploadService
        String filename = imageUploadService.uploadPetImage(petId, requestingOwnerId, photoFile);
    
        // Update pet with filename
        pet.setPhotoURL(filename);
    
        // Save and return
        return petRepository.save(pet);
    }
    


    // helper method to check if a pet belongs to an owner
    // good for controller-level authorization checks
    public boolean isPetOwnedBy(Long petId, Long ownerId) {
        return petRepository.findById(petId)
            .map(pet -> pet.getOwner().getId().equals(ownerId))
            .orElse(false);
    }

}