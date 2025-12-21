package com.PetTrackr.PetTrackr.repository;

import com.PetTrackr.PetTrackr.entity.Medication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MedicationRepository extends JpaRepository<Medication, Long> {
    // for individual pet medications
    List<Medication> findByPetId(Long petId);

    // for individual pet medications ordered by time to administer 
    List<Medication> findByPetIdOrderByTimeToAdministerAsc(Long petId);

    // for all medications by owner's pets
    List<Medication> findAllMedicationsForOwner(Long ownerId);
}