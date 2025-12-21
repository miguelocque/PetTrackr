package com.PetTrackr.PetTrackr.repository;

import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.entity.Pet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(statements = {"DELETE FROM feeding_schedule", "DELETE FROM medication", "DELETE FROM vet_visit", "DELETE FROM pet", "DELETE FROM owner"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class PetRepositoryTest {

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    void testFindByOwnerId_WithValidOwnerId_ReturnsPets() {
        // Arrange
        Owner owner = createAndSaveOwner("Owner1", "owner1@example.com");
        createAndSavePet("Max", "Dog", owner);

        // Act
        List<Pet> pets = petRepository.findByOwnerId(owner.getId());

        // Assert
        assertEquals(1, pets.size());
        assertEquals("Max", pets.get(0).getName());
    }

    @Test
    void testFindByOwnerId_WithInvalidOwnerId_ReturnsEmpty() {
        // Act
        List<Pet> pets = petRepository.findByOwnerId(999L);

        // Assert
        assertTrue(pets.isEmpty());
    }

    @Test
    void testFindByOwnerId_WithMultiplePets_ReturnsAll() {
        // Arrange
        Owner owner = createAndSaveOwner("Owner2", "owner2@example.com");
        createAndSavePet("Max", "Dog", owner);
        createAndSavePet("Luna", "Cat", owner);
        createAndSavePet("Buddy", "Dog", owner);

        // Act
        List<Pet> pets = petRepository.findByOwnerId(owner.getId());

        // Assert
        assertEquals(3, pets.size());
    }

    @Test
    void testFindById_WithValidPetId_ReturnsPet() {
        // Arrange
        Owner owner = createAndSaveOwner("Owner3", "owner3@example.com");
        Pet pet = createAndSavePet("Max", "Dog", owner);

        // Act
        Optional<Pet> found = petRepository.findById(pet.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Max", found.get().getName());
    }

    @Test
    void testSave_WithValidPet_PersistsSuccessfully() {
        // Arrange
        Owner owner = createAndSaveOwner("Owner4", "owner4@example.com");
        Pet pet = new Pet();
        pet.setName("Bella");
        pet.setType("Cat");
        pet.setBreed("Persian");
        pet.setAge(2);
        pet.setWeight(4.5);
        pet.setWeightType(Pet.WeightType.KG);
        pet.setDateOfBirth(LocalDate.of(2022, 1, 1));
        pet.setActivityLevel(Pet.ActivityLevel.LOW);
        pet.setOwner(owner);

        // Act
        Pet saved = petRepository.save(pet);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Bella", saved.getName());
        assertEquals("Persian", saved.getBreed());
    }

    @Test
    void testDelete_WithValidPet_RemovesSuccessfully() {
        // Arrange
        Owner owner = createAndSaveOwner("Owner5", "owner5@example.com");
        Pet pet = createAndSavePet("ToDelete", "Dog", owner);

        // Act
        petRepository.delete(pet);

        // Assert
        Optional<Pet> found = petRepository.findById(pet.getId());
        assertFalse(found.isPresent());
    }

    private Owner createAndSaveOwner(String name, String email) {
        Owner owner = new Owner();
        owner.setName(name);
        owner.setEmail(email);
        owner.setPhoneNumber("555-0000");
        owner.setPasswordHash("hashedpassword");
        return ownerRepository.save(owner);
    }

    private Pet createAndSavePet(String name, String type, Owner owner) {
        Pet pet = new Pet();
        pet.setName(name);
        pet.setType(type);
        pet.setBreed("Mixed");
        pet.setAge(1);
        pet.setWeight(25.0);
        pet.setWeightType(Pet.WeightType.KG);
        pet.setDateOfBirth(LocalDate.now().minusYears(1));
        pet.setActivityLevel(Pet.ActivityLevel.MEDIUM);
        pet.setOwner(owner);
        return petRepository.save(pet);
    }
}
