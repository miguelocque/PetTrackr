package com.PetTrackr.PetTrackr.repository;

import com.PetTrackr.PetTrackr.entity.Owner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional; // the optional import allows for handling null values gracefully

@Repository
public interface OwnerRepository extends JpaRepository<Owner, Long> {
    // Custom query method to find the existence of an owner by email
    Boolean existsByEmail(String email);

    // Custom query method to find an owner by email
    Optional<Owner> findByEmail(String email);

    // Custom query method to find an owner by name - optional, as names may not be unique
    Optional<Owner> findByName(String name);

    // Custom query method to find an owner by ID
    Optional<Owner> findById(Long id);

}