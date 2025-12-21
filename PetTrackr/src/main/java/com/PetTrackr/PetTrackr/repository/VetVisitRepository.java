package com.PetTrackr.PetTrackr.repository;

import com.PetTrackr.PetTrackr.entity.VetVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VetVisitRepository extends JpaRepository<VetVisit, Long> {
    // Custom query method to find vet visits by the pet's ID
    List<VetVisit> findByPetId(Long petId);
}