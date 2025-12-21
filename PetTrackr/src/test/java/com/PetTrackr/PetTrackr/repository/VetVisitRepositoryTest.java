package com.PetTrackr.PetTrackr.repository;

import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.entity.Pet;
import com.PetTrackr.PetTrackr.entity.VetVisit;
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
public class VetVisitRepositoryTest {

    @Autowired
    private VetVisitRepository vetVisitRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    void testFindByPetId_WithValidPetId_ReturnsVetVisits() {
        // Arrange
        Pet pet = createAndSavePet();
        createAndSaveVetVisit("Checkup", "Dr. Smith", pet);

        // Act
        List<VetVisit> visits = vetVisitRepository.findByPetId(pet.getId());

        // Assert
        assertEquals(1, visits.size());
        assertEquals("Checkup", visits.get(0).getReasonForVisit());
    }

    @Test
    void testFindByPetId_WithInvalidPetId_ReturnsEmpty() {
        // Act
        List<VetVisit> visits = vetVisitRepository.findByPetId(999L);

        // Assert
        assertTrue(visits.isEmpty());
    }

    @Test
    void testFindByPetId_WithMultipleVisits_ReturnsAll() {
        // Arrange
        Pet pet = createAndSavePet();
        createAndSaveVetVisit("Checkup", "Dr. Smith", pet);
        createAndSaveVetVisit("Vaccination", "Dr. Jones", pet);
        createAndSaveVetVisit("Dental", "Dr. Brown", pet);

        // Act
        List<VetVisit> visits = vetVisitRepository.findByPetId(pet.getId());

        // Assert
        assertEquals(3, visits.size());
    }

    @Test
    void testSave_WithValidVetVisit_PersistsSuccessfully() {
        // Arrange
        Pet pet = createAndSavePet();
        VetVisit visit = new VetVisit();
        visit.setVisitDate(LocalDate.now());
        visit.setVetName("Dr. Wilson");
        visit.setReasonForVisit("Injury");
        visit.setNotes("Minor sprain, rest recommended");
        visit.setNextVisitDate(LocalDate.now().plusMonths(1));
        visit.setPet(pet);

        // Act
        VetVisit saved = vetVisitRepository.save(visit);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Dr. Wilson", saved.getVetName());
        assertEquals("Injury", saved.getReasonForVisit());
    }

    @Test
    void testDelete_WithValidVetVisit_RemovesSuccessfully() {
        // Arrange
        Pet pet = createAndSavePet();
        VetVisit visit = createAndSaveVetVisit("ToDelete", "Dr. Delete", pet);

        // Act
        vetVisitRepository.delete(visit);

        // Assert
        Optional<VetVisit> found = vetVisitRepository.findById(visit.getId());
        assertFalse(found.isPresent());
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

    private VetVisit createAndSaveVetVisit(String reason, String vetName, Pet pet) {
        VetVisit visit = new VetVisit();
        visit.setVisitDate(LocalDate.now());
        visit.setVetName(vetName);
        visit.setReasonForVisit(reason);
        visit.setNotes("Test notes");
        visit.setNextVisitDate(null);
        visit.setPet(pet);
        return vetVisitRepository.save(visit);
    }
}
