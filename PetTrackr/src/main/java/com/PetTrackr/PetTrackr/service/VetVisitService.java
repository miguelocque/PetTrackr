package com.PetTrackr.PetTrackr.service;

import com.PetTrackr.PetTrackr.entity.Pet;
import com.PetTrackr.PetTrackr.entity.VetVisit;
import com.PetTrackr.PetTrackr.repository.VetVisitRepository;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;


// implement use case for vet visits (add and view vet visits for a pet) plus any update/delete as needed
@Transactional
@Service
public class VetVisitService {
    
    private final PetService petService;
    private final VetVisitRepository vetVisitRepository;

    public VetVisitService(PetService petService, VetVisitRepository vetVisitRepository) {
        this.petService = petService;
        this.vetVisitRepository = vetVisitRepository;
    }

    public VetVisit addVetVisitToPet(Long petId, Long requestingOwnerId, LocalDate visitDate, LocalDate nextVisitDate, String reason,
                                    String vetName, String notes) {

        // verifies the pet exists and belongs to the requesting owner
        Pet pet = petService.getPetById(petId, requestingOwnerId); 

        // trim inputs
        reason = reason != null ? reason.trim() : null;
        vetName = vetName != null ? vetName.trim() : null;
        notes = notes != null ? notes.trim() : null;

        // need to null check the inputs
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason for visit cannot be empty");
        }
        if (vetName == null || vetName.isBlank()) {
            throw new IllegalArgumentException("Vet name cannot be empty");
        }
        if (visitDate == null) {
            throw new IllegalArgumentException("Visit date cannot be null");
        }

        VetVisit vetVisit = new VetVisit();
        vetVisit.setPet(pet);
        vetVisit.setNextVisitDate(nextVisitDate); // can be null
        vetVisit.setVisitDate(visitDate);
        vetVisit.setReasonForVisit(reason);
        vetVisit.setVetName(vetName);
        vetVisit.setNotes(notes);
        return vetVisitRepository.save(vetVisit);
    }

    // update method for vet visit
    public VetVisit updateVetVisit(Long vetVisitId, Long requestingOwnerId, LocalDate visitDate, LocalDate nextVisitDate, String reason,
                                    String vetName, String notes) {
        // Fetch vet visit once
        VetVisit vetVisit = vetVisitRepository.findById(vetVisitId)
                .orElseThrow(() -> new IllegalArgumentException("Vet visit not found with id: " + vetVisitId));

        // Verify the pet associated with the vet visit belongs to the requesting owner
        petService.getPetById(vetVisit.getPet().getId(), requestingOwnerId);

        // trim inputs
        reason = reason != null ? reason.trim() : null;
        vetName = vetName != null ? vetName.trim() : null;
        notes = notes != null ? notes.trim() : null;

        // allow for partial updates - only update fields that are not null
        if (reason != null) {
            if (reason.isBlank()) {
                throw new IllegalArgumentException("Reason for visit cannot be empty");
            }
            vetVisit.setReasonForVisit(reason);
        }
        if (vetName != null) {
            if (vetName.isBlank()) {
                throw new IllegalArgumentException("Vet name cannot be empty");
            }
            vetVisit.setVetName(vetName);
        }
        if (visitDate != null) {
            vetVisit.setVisitDate(visitDate);
        }
        if (nextVisitDate != null) {
            vetVisit.setNextVisitDate(nextVisitDate);
        }
        if (notes != null) {
            vetVisit.setNotes(notes);
        }
        return vetVisitRepository.save(vetVisit);
    }

    // get all vet visits for a pet
    public List<VetVisit> getVetVisitsForPet(Long petId, Long requestingOwnerId) {
        // Verify pet exists and belongs to requesting owner
        petService.getPetById(petId, requestingOwnerId); 

        // Return visits in chronological order
        return vetVisitRepository.findByPetIdOrderByVisitDateAsc(petId);
    }

    // retrieve a specific vet visit by ID
    public VetVisit getVetVisitById(Long vetVisitId, Long requestingOwnerId) {
        VetVisit vetVisit = vetVisitRepository.findById(vetVisitId)
                .orElseThrow(() -> new IllegalArgumentException("Vet visit not found with id: " + vetVisitId));

        // Verify the pet associated with the vet visit belongs to the requesting owner
        petService.getPetById(vetVisit.getPet().getId(), requestingOwnerId);

        return vetVisit;
    }

    // delete vet visit by ID
    public VetVisit deleteVetVisit(Long vetVisitId, Long requestingOwnerId) {
        // Fetch vet visit once
        VetVisit vetVisit = vetVisitRepository.findById(vetVisitId)
                .orElseThrow(() -> new IllegalArgumentException("Vet visit not found with id: " + vetVisitId));
        // Verify the pet associated with the vet visit belongs to the requesting owner
        petService.getPetById(vetVisit.getPet().getId(), requestingOwnerId);

        vetVisitRepository.delete(vetVisit);
        return vetVisit;
    }

    
}