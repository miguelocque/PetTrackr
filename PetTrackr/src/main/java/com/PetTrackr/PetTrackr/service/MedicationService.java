package com.PetTrackr.PetTrackr.service;

import org.springframework.stereotype.Service;

// implement use case 7 and 8 for medications (add and view medications for a pet)
import com.PetTrackr.PetTrackr.entity.Medication;
import com.PetTrackr.PetTrackr.entity.Medication.DosageUnit;
import com.PetTrackr.PetTrackr.entity.Pet;
import com.PetTrackr.PetTrackr.repository.MedicationRepository;

import jakarta.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class MedicationService {

    private final MedicationRepository medicationRepository;

    private final PetService petService; // to verify pet existence

    public MedicationService(MedicationRepository medicationRepository, PetService petService) {
        this.medicationRepository = medicationRepository;

        this.petService = petService;
    }

    public Medication addMedicationToPet(Long petId, Long requestingOwnerId, String name, double dosageAmount, 
                                        DosageUnit dosageUnit, String frequency, 
                                        java.time.LocalTime timeToAdminister, java.time.LocalDate startDate,
                                        java.time.LocalDate endDate) {
        Pet pet = petService.getPetById(petId, requestingOwnerId); // verify pet exists
        // trim inputs
        name = name != null ? name.trim() : null;
        frequency = frequency != null ? frequency.trim() : null;


        // need to null check the inputs
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Medication name cannot be empty");
        }
        if (dosageAmount <= 0) {
            throw new IllegalArgumentException("Dosage amount must be positive");
        }
        if (dosageUnit == null) {
            throw new IllegalArgumentException("Dosage unit cannot be null");
        }
        if (frequency == null || frequency.isBlank()) {
            throw new IllegalArgumentException("Frequency cannot be empty");
        }
        if (timeToAdminister == null) {
            throw new IllegalArgumentException("Time to administer cannot be null");
        }
        if (startDate == null) {
            throw new IllegalArgumentException("Start date cannot be null");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        
        Medication medication = new Medication();
        medication.setName(name);
        medication.setDosageAmount(dosageAmount);
        medication.setDosageUnit(dosageUnit);
        medication.setFrequency(frequency);
        medication.setTimeToAdminister(timeToAdminister);
        medication.setPet(pet);
        medication.setStartDate(startDate);
        // end date can be null - check if not null before setting
        if (endDate != null) {
            medication.setEndDate(endDate);
        }

        return medicationRepository.save(medication);
    }

    // update medications for a pet - not listed in use cases but useful for completeness
    public Medication updateMedication(Long medicationId, Long requestingOwnerId, String name, Double dosageAmount, 
                                        DosageUnit dosageUnit, String frequency, 
                                        java.time.LocalTime timeToAdminister, java.time.LocalDate startDate,
                                        java.time.LocalDate endDate) {
        // Fetch medication 
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found with id: " + medicationId));

        // Verify pet exists and belongs to owner
        petService.getPetById(medication.getPet().getId(), requestingOwnerId);

        // trim inputs
        name = name != null ? name.trim() : null;
        frequency = frequency != null ? frequency.trim() : null;

        // update fields if new values are provided
        if (name != null && !name.isBlank()) {
            medication.setName(name);
        }
        if (dosageAmount != null && dosageAmount > 0) {
            medication.setDosageAmount(dosageAmount);
        }
        if (dosageUnit != null) {
            medication.setDosageUnit(dosageUnit);
        }
        if (frequency != null && !frequency.isBlank()) {
            medication.setFrequency(frequency);
        }
        if (timeToAdminister != null) {
            medication.setTimeToAdminister(timeToAdminister);
        }
        if (startDate != null) {
            medication.setStartDate(startDate);
        }
        if (endDate != null) {
            // Validate against medication's current startDate, not the parameter
            if (endDate.isBefore(medication.getStartDate())) {
                throw new IllegalArgumentException("End date cannot be before start date");
            }
            medication.setEndDate(endDate);
        }

        return medicationRepository.save(medication);
    }

    // delete a medication by ID - not listed in use cases but useful for completeness CRUD
    public Medication deleteMedication(Long medicationId, Long requestingOwnerId) {
        // Fetch medication once
        Medication medication = medicationRepository.findById(medicationId)
                .orElseThrow(() -> new IllegalArgumentException("Medication not found with id: " + medicationId));
        
        // Verify pet exists and belongs to owner
        petService.getPetById(medication.getPet().getId(), requestingOwnerId);
        
        medicationRepository.delete(medication);
        return medication;
    }

    // get all medications for a pet to display in UI
    public List<Medication> getMedicationsForPet(Long petId, Long requestingOwnerId) {
        // firstly verify pet exists with an authorization check
        petService.getPetById(petId, requestingOwnerId);
        
        // after test passes, return medications
        return medicationRepository.findByPetIdOrderByTimeToAdministerAsc(petId);
    }
}