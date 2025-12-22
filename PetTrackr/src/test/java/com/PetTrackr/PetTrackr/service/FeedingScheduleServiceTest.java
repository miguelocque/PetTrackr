package com.PetTrackr.PetTrackr.service;

import com.PetTrackr.PetTrackr.entity.FeedingSchedule;
import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.entity.Pet;
import com.PetTrackr.PetTrackr.repository.FeedingScheduleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedingScheduleServiceTest {

    @Mock
    private FeedingScheduleRepository feedingScheduleRepository;

    @Mock
    private PetService petService;

    @InjectMocks
    private FeedingScheduleService feedingScheduleService;

    private Owner testOwner;
    private Pet testPet;
    private FeedingSchedule testSchedule;

    @BeforeEach
    void setUp() {
        // Create test owner
        testOwner = new Owner();
        testOwner.setId(1L);
        testOwner.setName("Test Owner");
        testOwner.setEmail("test@example.com");

        // Create test pet
        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("Max");
        testPet.setType("Dog");
        testPet.setBreed("Golden Retriever");
        testPet.setAge(3);
        testPet.setWeight(30.0);
        testPet.setWeightType(Pet.WeightType.KG);
        testPet.setActivityLevel(Pet.ActivityLevel.HIGH);
        testPet.setDateOfBirth(LocalDate.of(2021, 1, 1));
        testPet.setOwner(testOwner);

        // Create test feeding schedule
        testSchedule = new FeedingSchedule();
        testSchedule.setId(1L);
        testSchedule.setTime(LocalTime.of(8, 0));
        testSchedule.setFoodType("Dry Kibble");
        testSchedule.setQuantity(2.0);
        testSchedule.setQuantityUnit(FeedingSchedule.QuantityUnit.CUPS);
        testSchedule.setPet(testPet);
    }

    // ========================================
    // addFeedingScheduleToPet Tests
    // ========================================

    @Test
    void testAddFeedingScheduleToPet_WithValidData_Success() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.save(any(FeedingSchedule.class))).thenAnswer(invocation -> {
            FeedingSchedule schedule = invocation.getArgument(0);
            schedule.setId(1L);
            return schedule;
        });

        // Act
        FeedingSchedule result = feedingScheduleService.addFeedingScheduleToPet(
            1L, 1L,
            LocalTime.of(8, 0), "Dry Kibble",
            FeedingSchedule.QuantityUnit.CUPS, 2.0
        );

        // Assert
        assertNotNull(result);
        assertEquals("Dry Kibble", result.getFoodType());
        assertEquals(2.0, result.getQuantity());
        assertEquals(FeedingSchedule.QuantityUnit.CUPS, result.getQuantityUnit());
        assertEquals(testPet, result.getPet());
        verify(petService).getPetById(1L, 1L);
        verify(feedingScheduleRepository).save(any(FeedingSchedule.class));
    }

    @Test
    void testAddFeedingScheduleToPet_WithTrimmedFoodType_Success() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.save(any(FeedingSchedule.class))).thenAnswer(invocation -> {
            FeedingSchedule schedule = invocation.getArgument(0);
            assertEquals("Wet Food", schedule.getFoodType()); // Should be trimmed
            return schedule;
        });

        // Act
        feedingScheduleService.addFeedingScheduleToPet(
            1L, 1L,
            LocalTime.of(8, 0), "  Wet Food  ",
            FeedingSchedule.QuantityUnit.CANS, 1.0
        );

        // Assert
        verify(feedingScheduleRepository).save(any(FeedingSchedule.class));
    }

    @Test
    void testAddFeedingScheduleToPet_WithInvalidPet_ThrowsException() {
        // Arrange
        when(petService.getPetById(999L, 1L)).thenThrow(new IllegalArgumentException("Pet not found with ID: 999"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            feedingScheduleService.addFeedingScheduleToPet(
                999L, 1L,
                LocalTime.of(8, 0), "Dry Kibble",
                FeedingSchedule.QuantityUnit.CUPS, 2.0
            );
        });
        assertEquals("Pet not found with ID: 999", exception.getMessage());
        verify(feedingScheduleRepository, never()).save(any(FeedingSchedule.class));
    }

    @Test
    void testAddFeedingScheduleToPet_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied: Pet does not belong to this owner"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            feedingScheduleService.addFeedingScheduleToPet(
                1L, 999L,
                LocalTime.of(8, 0), "Dry Kibble",
                FeedingSchedule.QuantityUnit.CUPS, 2.0
            );
        });
        verify(feedingScheduleRepository, never()).save(any(FeedingSchedule.class));
    }

    @Test
    void testAddFeedingScheduleToPet_WithNullQuantityUnit_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            feedingScheduleService.addFeedingScheduleToPet(
                1L, 1L,
                LocalTime.of(8, 0), "Dry Kibble",
                null, 2.0
            );
        });
        assertEquals("Quantity unit cannot be null", exception.getMessage());
    }

    @Test
    void testAddFeedingScheduleToPet_WithNullTime_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            feedingScheduleService.addFeedingScheduleToPet(
                1L, 1L,
                null, "Dry Kibble",
                FeedingSchedule.QuantityUnit.CUPS, 2.0
            );
        });
        assertEquals("Feeding time cannot be null", exception.getMessage());
    }

    @Test
    void testAddFeedingScheduleToPet_WithNullFoodType_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            feedingScheduleService.addFeedingScheduleToPet(
                1L, 1L,
                LocalTime.of(8, 0), null,
                FeedingSchedule.QuantityUnit.CUPS, 2.0
            );
        });
        assertEquals("Food type cannot be empty", exception.getMessage());
    }

    @Test
    void testAddFeedingScheduleToPet_WithBlankFoodType_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            feedingScheduleService.addFeedingScheduleToPet(
                1L, 1L,
                LocalTime.of(8, 0), "   ",
                FeedingSchedule.QuantityUnit.CUPS, 2.0
            );
        });
        assertEquals("Food type cannot be empty", exception.getMessage());
    }

    @Test
    void testAddFeedingScheduleToPet_WithZeroQuantity_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            feedingScheduleService.addFeedingScheduleToPet(
                1L, 1L,
                LocalTime.of(8, 0), "Dry Kibble",
                FeedingSchedule.QuantityUnit.CUPS, 0
            );
        });
        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    @Test
    void testAddFeedingScheduleToPet_WithNegativeQuantity_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            feedingScheduleService.addFeedingScheduleToPet(
                1L, 1L,
                LocalTime.of(8, 0), "Dry Kibble",
                FeedingSchedule.QuantityUnit.CUPS, -1.5
            );
        });
        assertEquals("Quantity must be greater than zero", exception.getMessage());
    }

    // ========================================
    // updateFeedingSchedule Tests
    // ========================================

    @Test
    void testUpdateFeedingSchedule_WithValidData_Success() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.save(any(FeedingSchedule.class))).thenReturn(testSchedule);

        // Act
        FeedingSchedule result = feedingScheduleService.updateFeedingSchedule(
            1L, 1L,
            LocalTime.of(18, 0), "Wet Food",
            FeedingSchedule.QuantityUnit.CANS, 1.5
        );

        // Assert
        assertNotNull(result);
        verify(feedingScheduleRepository).findById(1L);
        verify(petService).getPetById(1L, 1L);
        verify(feedingScheduleRepository).save(testSchedule);
    }

    @Test
    void testUpdateFeedingSchedule_WithInvalidScheduleId_ThrowsException() {
        // Arrange
        when(feedingScheduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            feedingScheduleService.updateFeedingSchedule(
                999L, 1L,
                LocalTime.of(18, 0), "Wet Food",
                FeedingSchedule.QuantityUnit.CANS, 1.5
            );
        });
        assertEquals("Feeding schedule not found with id: 999", exception.getMessage());
        verify(petService, never()).getPetById(anyLong(), anyLong());
    }

    @Test
    void testUpdateFeedingSchedule_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            feedingScheduleService.updateFeedingSchedule(
                1L, 999L,
                LocalTime.of(18, 0), "Wet Food",
                FeedingSchedule.QuantityUnit.CANS, 1.5
            );
        });
        verify(feedingScheduleRepository, never()).save(any(FeedingSchedule.class));
    }

    @Test
    void testUpdateFeedingSchedule_WithOnlyTime_UpdatesTimeOnly() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.save(any(FeedingSchedule.class))).thenReturn(testSchedule);
        String originalFoodType = testSchedule.getFoodType();
        LocalTime newTime = LocalTime.of(18, 0);

        // Act
        feedingScheduleService.updateFeedingSchedule(
            1L, 1L,
            newTime, null,
            null, 0
        );

        // Assert
        assertEquals(newTime, testSchedule.getTime());
        assertEquals(originalFoodType, testSchedule.getFoodType()); // Unchanged
        verify(feedingScheduleRepository).save(testSchedule);
    }

    @Test
    void testUpdateFeedingSchedule_WithOnlyFoodType_UpdatesFoodTypeOnly() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.save(any(FeedingSchedule.class))).thenReturn(testSchedule);
        LocalTime originalTime = testSchedule.getTime();

        // Act
        feedingScheduleService.updateFeedingSchedule(
            1L, 1L,
            null, "New Food Type",
            null, 0
        );

        // Assert
        assertEquals("New Food Type", testSchedule.getFoodType());
        assertEquals(originalTime, testSchedule.getTime()); // Unchanged
        verify(feedingScheduleRepository).save(testSchedule);
    }

    @Test
    void testUpdateFeedingSchedule_WithBlankFoodType_DoesNotUpdateFoodType() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.save(any(FeedingSchedule.class))).thenReturn(testSchedule);
        String originalFoodType = testSchedule.getFoodType();

        // Act
        feedingScheduleService.updateFeedingSchedule(
            1L, 1L,
            null, "   ",
            null, 0
        );

        // Assert
        assertEquals(originalFoodType, testSchedule.getFoodType()); // Unchanged
    }

    @Test
    void testUpdateFeedingSchedule_WithOnlyQuantityUnit_UpdatesQuantityUnitOnly() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.save(any(FeedingSchedule.class))).thenReturn(testSchedule);
        String originalFoodType = testSchedule.getFoodType();

        // Act
        feedingScheduleService.updateFeedingSchedule(
            1L, 1L,
            null, null,
            FeedingSchedule.QuantityUnit.GRAMS, 0
        );

        // Assert
        assertEquals(FeedingSchedule.QuantityUnit.GRAMS, testSchedule.getQuantityUnit());
        assertEquals(originalFoodType, testSchedule.getFoodType()); // Unchanged
        verify(feedingScheduleRepository).save(testSchedule);
    }

    @Test
    void testUpdateFeedingSchedule_WithOnlyQuantity_UpdatesQuantityOnly() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.save(any(FeedingSchedule.class))).thenReturn(testSchedule);
        String originalFoodType = testSchedule.getFoodType();

        // Act
        feedingScheduleService.updateFeedingSchedule(
            1L, 1L,
            null, null,
            null, 3.5
        );

        // Assert
        assertEquals(3.5, testSchedule.getQuantity());
        assertEquals(originalFoodType, testSchedule.getFoodType()); // Unchanged
        verify(feedingScheduleRepository).save(testSchedule);
    }

    @Test
    void testUpdateFeedingSchedule_WithZeroQuantity_DoesNotUpdateQuantity() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.save(any(FeedingSchedule.class))).thenReturn(testSchedule);
        double originalQuantity = testSchedule.getQuantity();

        // Act
        feedingScheduleService.updateFeedingSchedule(
            1L, 1L,
            null, null,
            null, 0
        );

        // Assert
        assertEquals(originalQuantity, testSchedule.getQuantity()); // Unchanged
    }

    @Test
    void testUpdateFeedingSchedule_WithNegativeQuantity_DoesNotUpdateQuantity() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.save(any(FeedingSchedule.class))).thenReturn(testSchedule);
        double originalQuantity = testSchedule.getQuantity();

        // Act
        feedingScheduleService.updateFeedingSchedule(
            1L, 1L,
            null, null,
            null, -1.5
        );

        // Assert
        assertEquals(originalQuantity, testSchedule.getQuantity()); // Unchanged
    }

    @Test
    void testUpdateFeedingSchedule_WithAllNullValues_DoesNotChangeAnything() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.save(any(FeedingSchedule.class))).thenReturn(testSchedule);
        
        LocalTime originalTime = testSchedule.getTime();
        String originalFoodType = testSchedule.getFoodType();
        FeedingSchedule.QuantityUnit originalUnit = testSchedule.getQuantityUnit();
        double originalQuantity = testSchedule.getQuantity();

        // Act
        feedingScheduleService.updateFeedingSchedule(
            1L, 1L,
            null, null,
            null, 0
        );

        // Assert
        assertEquals(originalTime, testSchedule.getTime());
        assertEquals(originalFoodType, testSchedule.getFoodType());
        assertEquals(originalUnit, testSchedule.getQuantityUnit());
        assertEquals(originalQuantity, testSchedule.getQuantity());
        verify(feedingScheduleRepository).save(testSchedule);
    }

    // ========================================
    // deleteFeedingSchedule Tests
    // ========================================

    @Test
    void testDeleteFeedingSchedule_WithValidData_Success() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        doNothing().when(feedingScheduleRepository).delete(any(FeedingSchedule.class));

        // Act
        FeedingSchedule result = feedingScheduleService.deleteFeedingSchedule(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testSchedule.getId(), result.getId());
        verify(feedingScheduleRepository).findById(1L);
        verify(petService).getPetById(1L, 1L);
        verify(feedingScheduleRepository).delete(testSchedule);
    }

    @Test
    void testDeleteFeedingSchedule_WithInvalidScheduleId_ThrowsException() {
        // Arrange
        when(feedingScheduleRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            feedingScheduleService.deleteFeedingSchedule(999L, 1L);
        });
        assertEquals("Feeding schedule not found with id: 999", exception.getMessage());
        verify(feedingScheduleRepository, never()).delete(any(FeedingSchedule.class));
    }

    @Test
    void testDeleteFeedingSchedule_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(feedingScheduleRepository.findById(1L)).thenReturn(Optional.of(testSchedule));
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            feedingScheduleService.deleteFeedingSchedule(1L, 999L);
        });
        verify(feedingScheduleRepository, never()).delete(any(FeedingSchedule.class));
    }

    // ========================================
    // getFeedingSchedulesForPet Tests
    // ========================================

    @Test
    void testGetFeedingSchedulesForPet_WithValidPetAndOwner_ReturnsSortedList() {
        // Arrange
        FeedingSchedule schedule1 = new FeedingSchedule();
        schedule1.setId(1L);
        schedule1.setTime(LocalTime.of(8, 0));
        schedule1.setFoodType("Breakfast");
        
        FeedingSchedule schedule2 = new FeedingSchedule();
        schedule2.setId(2L);
        schedule2.setTime(LocalTime.of(12, 0));
        schedule2.setFoodType("Lunch");
        
        FeedingSchedule schedule3 = new FeedingSchedule();
        schedule3.setId(3L);
        schedule3.setTime(LocalTime.of(18, 0));
        schedule3.setFoodType("Dinner");

        List<FeedingSchedule> schedules = Arrays.asList(schedule1, schedule2, schedule3);
        
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.findByPetIdOrderByTimeAsc(1L)).thenReturn(schedules);

        // Act
        List<FeedingSchedule> result = feedingScheduleService.getFeedingSchedulesForPet(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(LocalTime.of(8, 0), result.get(0).getTime());
        assertEquals(LocalTime.of(12, 0), result.get(1).getTime());
        assertEquals(LocalTime.of(18, 0), result.get(2).getTime());
        verify(petService).getPetById(1L, 1L);
        verify(feedingScheduleRepository).findByPetIdOrderByTimeAsc(1L);
    }

    @Test
    void testGetFeedingSchedulesForPet_WithInvalidPet_ThrowsException() {
        // Arrange
        when(petService.getPetById(999L, 1L)).thenThrow(new IllegalArgumentException("Pet not found with ID: 999"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            feedingScheduleService.getFeedingSchedulesForPet(999L, 1L);
        });
        assertEquals("Pet not found with ID: 999", exception.getMessage());
        verify(feedingScheduleRepository, never()).findByPetIdOrderByTimeAsc(anyLong());
    }

    @Test
    void testGetFeedingSchedulesForPet_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            feedingScheduleService.getFeedingSchedulesForPet(1L, 999L);
        });
        verify(feedingScheduleRepository, never()).findByPetIdOrderByTimeAsc(anyLong());
    }

    @Test
    void testGetFeedingSchedulesForPet_WithNoSchedules_ReturnsEmptyList() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(feedingScheduleRepository.findByPetIdOrderByTimeAsc(1L)).thenReturn(Arrays.asList());

        // Act
        List<FeedingSchedule> result = feedingScheduleService.getFeedingSchedulesForPet(1L, 1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(feedingScheduleRepository).findByPetIdOrderByTimeAsc(1L);
    }
}
