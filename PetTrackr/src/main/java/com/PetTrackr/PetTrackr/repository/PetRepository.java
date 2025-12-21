package com.PetTrackr.PetTrackr.repository;

import com.PetTrackr.PetTrackr.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PetRepository extends JpaRepository<Pet, Long> {
    // Custom query method to find pets by their owner's ID
    // fulfills the need to retrieve all pets associated with a specific owner
    // use case 4 from the use case document
    List<Pet> findByOwnerId(Long ownerId);
}