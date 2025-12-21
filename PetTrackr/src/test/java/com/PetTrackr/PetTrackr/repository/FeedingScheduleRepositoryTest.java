package com.PetTrackr.PetTrackr.repository;

import com.PetTrackr.PetTrackr.entity.FeedingSchedule;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(statements = {"DELETE FROM feeding_schedule", "DELETE FROM medication", "DELETE FROM vet_visit", "DELETE FROM pet", "DELETE FROM owner"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class FeedingScheduleRepositoryTest {

    @Autowired
    private FeedingScheduleRepository feedingScheduleRepository;

    @Autowired
    private PetRepository petRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    void testFindByPetId_WithValidPetId_ReturnsFeedingSchedules() {
        // Arrange
        Pet pet = createAndSavePet();
        createAndSaveFeedingSchedule(LocalTime.of(9, 0), "Kibble", pet);

        // Act
        List<FeedingSchedule> schedules = feedingScheduleRepository.findByPetId(pet.getId());

        // Assert
        assertEquals(1, schedules.size());
        assertEquals("Kibble", schedules.get(0).getFoodType());
    }

    @Test
    void testFindByPetId_WithInvalidPetId_ReturnsEmpty() {
        // Act
        List<FeedingSchedule> schedules = feedingScheduleRepository.findByPetId(999L);

        // Assert
        assertTrue(schedules.isEmpty());
    }

    @Test
    void testFindByPetId_WithMultipleSchedules_ReturnsAll() {
        // Arrange
        Pet pet = createAndSavePet();
        createAndSaveFeedingSchedule(LocalTime.of(9, 0), "Kibble", pet);
        createAndSaveFeedingSchedule(LocalTime.of(18, 0), "Kibble", pet);
        createAndSaveFeedingSchedule(LocalTime.of(14, 0), "Wet Food", pet);

        // Act
        List<FeedingSchedule> schedules = feedingScheduleRepository.findByPetId(pet.getId());

        // Assert
        assertEquals(3, schedules.size());
    }

    @Test
    void testSave_WithValidFeedingSchedule_PersistsSuccessfully() {
        // Arrange
        Pet pet = createAndSavePet();
        FeedingSchedule schedule = new FeedingSchedule();
        schedule.setTime(LocalTime.of(8, 0));
        schedule.setFoodType("Premium Kibble");
        schedule.setQuantity(2.5);
        schedule.setQuantityUnit(FeedingSchedule.QuantityUnit.CUPS);
        schedule.setPet(pet);

        // Act
        FeedingSchedule saved = feedingScheduleRepository.save(schedule);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Premium Kibble", saved.getFoodType());
        assertEquals(2.5, saved.getQuantity());
    }

    @Test
    void testDelete_WithValidFeedingSchedule_RemovesSuccessfully() {
        // Arrange
        Pet pet = createAndSavePet();
        FeedingSchedule schedule = createAndSaveFeedingSchedule(LocalTime.of(9, 0), "ToDelete", pet);

        // Act
        feedingScheduleRepository.delete(schedule);

        // Assert
        Optional<FeedingSchedule> found = feedingScheduleRepository.findById(schedule.getId());
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

    private FeedingSchedule createAndSaveFeedingSchedule(LocalTime time, String foodType, Pet pet) {
        FeedingSchedule schedule = new FeedingSchedule();
        schedule.setTime(time);
        schedule.setFoodType(foodType);
        schedule.setQuantity(2.0);
        schedule.setQuantityUnit(FeedingSchedule.QuantityUnit.CUPS);
        schedule.setPet(pet);
        return feedingScheduleRepository.save(schedule);
    }
}
