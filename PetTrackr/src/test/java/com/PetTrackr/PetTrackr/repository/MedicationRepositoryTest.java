package com.PetTrackr.PetTrackr.repository;

import com.PetTrackr.PetTrackr.entity.Medication;
import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.entity.Pet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(statements = {"DELETE FROM feeding_schedule", "DELETE FROM medication", "DELETE FROM vet_visit", "DELETE FROM pet", "DELETE FROM owner"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class MedicationRepositoryTest {

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    void testFindByPetId_WithValidPetId_ReturnsMedications() {
        // Arrange
        Pet pet = createAndSavePet();
        createAndSaveMedication("Aspirin", LocalTime.of(9, 0), pet);

        // Act
        List<Medication> medications = medicationRepository.findByPetId(pet.getId());

        // Assert
        assertEquals(1, medications.size());
        assertEquals("Aspirin", medications.get(0).getName());
    }

    @Test
    void testFindByPetId_WithInvalidPetId_ReturnsEmpty() {
        // Act
        List<Medication> medications = medicationRepository.findByPetId(999L);

        // Assert
        assertTrue(medications.isEmpty());
    }

    @Test
    void testFindByPetIdOrderByTimeToAdministerAsc_ReturnsSortedMedications() {
        // Arrange
        Pet pet = createAndSavePet();
        createAndSaveMedication("Morning Med", LocalTime.of(9, 0), pet);
        createAndSaveMedication("Evening Med", LocalTime.of(18, 0), pet);
        createAndSaveMedication("Afternoon Med", LocalTime.of(14, 0), pet);

        // Act
        List<Medication> medications = medicationRepository.findByPetIdOrderByTimeToAdministerAsc(pet.getId());

        // Assert
        assertEquals(3, medications.size());
    }

    @Test
    void testFindByPetIdOrderByTimeToAdministerAsc_VerifyOrder() {
        // Arrange
        Pet pet = createAndSavePet();
        createAndSaveMedication("Evening Med", LocalTime.of(18, 0), pet);
        createAndSaveMedication("Morning Med", LocalTime.of(9, 0), pet);
        createAndSaveMedication("Afternoon Med", LocalTime.of(14, 0), pet);

        // Act
        List<Medication> medications = medicationRepository.findByPetIdOrderByTimeToAdministerAsc(pet.getId());

        // Assert
        assertEquals("Morning Med", medications.get(0).getName());
        assertEquals("Afternoon Med", medications.get(1).getName());
        assertEquals("Evening Med", medications.get(2).getName());
    }

    @Test
    void testSave_WithValidMedication_PersistsSuccessfully() {
        // Arrange
        Pet pet = createAndSavePet();
        Medication med = new Medication();
        med.setName("Ibuprofen");
        med.setDosage("200mg");
        med.setFrequency("Twice daily");
        med.setTimeToAdminister(LocalTime.of(9, 0));
        med.setStartDate(LocalDate.now());
        med.setEndDate(null);
        med.setPet(pet);

        // Act
        Medication saved = medicationRepository.save(med);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Ibuprofen", saved.getName());
        assertEquals("200mg", saved.getDosage());
    }

    private Pet createAndSavePet() {
        Owner owner = new Owner();
        owner.setName("Test Owner");
        owner.setEmail("test@example.com");
        owner.setPhoneNumber("555-0000");
        owner.setPasswordHash("hashedpassword");
        owner = ownerRepository.save(owner);

        Pet pet = new Pet();
        pet.setName("TestPet");
        pet.setType("Dog");
        pet.setBreed("Mixed");
        pet.setAge(1);
        pet.setWeight(25.0);
        pet.setWeightType(Pet.WeightType.KG);
        pet.setDateOfBirth(LocalDate.now().minusYears(1));
        pet.setActivityLevel(Pet.ActivityLevel.MEDIUM);
        pet.setOwner(owner);
        return petRepository.save(pet);
    }

    private Medication createAndSaveMedication(String name, LocalTime time, Pet pet) {
        Medication med = new Medication();
        med.setName(name);
        med.setDosage("1 tablet");
        med.setFrequency("Daily");
        med.setTimeToAdminister(time);
        med.setStartDate(LocalDate.now());
        med.setEndDate(null);
        med.setPet(pet);
        return medicationRepository.save(med);
    }
}
