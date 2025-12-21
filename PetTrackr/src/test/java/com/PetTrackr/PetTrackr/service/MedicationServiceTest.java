package com.PetTrackr.PetTrackr.service;

import com.PetTrackr.PetTrackr.entity.Medication;
import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.entity.Pet;
import com.PetTrackr.PetTrackr.repository.MedicationRepository;
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
public class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @Mock
    private PetService petService;

    @InjectMocks
    private MedicationService medicationService;

    private Owner testOwner;
    private Pet testPet;
    private Medication testMedication;

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
        testPet.setOwner(testOwner);

        // Create test medication
        testMedication = new Medication();
        testMedication.setId(1L);
        testMedication.setName("Heartworm Prevention");
        testMedication.setDosage("1 tablet");
        testMedication.setFrequency("Monthly");
        testMedication.setTimeToAdminister(LocalTime.of(9, 0));
        testMedication.setStartDate(LocalDate.of(2024, 1, 1));
        testMedication.setEndDate(LocalDate.of(2024, 12, 31));
        testMedication.setPet(testPet);
    }

    // ========================================
    // addMedicationToPet Tests
    // ========================================

    @Test
    void testAddMedicationToPet_WithValidData_Success() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> {
            Medication med = invocation.getArgument(0);
            med.setId(1L);
            return med;
        });

        // Act
        Medication result = medicationService.addMedicationToPet(
            1L, 1L,
            "Antibiotics", "250mg", "Twice daily",
            LocalTime.of(8, 0), LocalDate.now(), LocalDate.now().plusDays(7)
        );

        // Assert
        assertNotNull(result);
        assertEquals("Antibiotics", result.getName());
        assertEquals("250mg", result.getDosage());
        assertEquals("Twice daily", result.getFrequency());
        verify(petService).getPetById(1L, 1L);
        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void testAddMedicationToPet_WithNullEndDate_Success() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> {
            Medication med = invocation.getArgument(0);
            med.setId(1L);
            return med;
        });

        // Act
        Medication result = medicationService.addMedicationToPet(
            1L, 1L,
            "Daily Vitamin", "1 tablet", "Daily",
            LocalTime.of(8, 0), LocalDate.now(), null
        );

        // Assert
        assertNotNull(result);
        assertNull(result.getEndDate()); // End date should be null
        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void testAddMedicationToPet_WithTrimmedInputs_Success() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenAnswer(invocation -> {
            Medication med = invocation.getArgument(0);
            assertEquals("Antibiotics", med.getName()); // Should be trimmed
            assertEquals("250mg", med.getDosage());
            assertEquals("Twice daily", med.getFrequency());
            return med;
        });

        // Act
        medicationService.addMedicationToPet(
            1L, 1L,
            "  Antibiotics  ", "  250mg  ", "  Twice daily  ",
            LocalTime.of(8, 0), LocalDate.now(), null
        );

        // Assert
        verify(medicationRepository).save(any(Medication.class));
    }

    @Test
    void testAddMedicationToPet_WithInvalidPet_ThrowsException() {
        // Arrange
        when(petService.getPetById(999L, 1L)).thenThrow(new IllegalArgumentException("Pet not found with ID: 999"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.addMedicationToPet(
                999L, 1L,
                "Antibiotics", "250mg", "Twice daily",
                LocalTime.of(8, 0), LocalDate.now(), null
            );
        });
        assertEquals("Pet not found with ID: 999", exception.getMessage());
        verify(medicationRepository, never()).save(any(Medication.class));
    }

    @Test
    void testAddMedicationToPet_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied: Pet does not belong to this owner"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            medicationService.addMedicationToPet(
                1L, 999L,
                "Antibiotics", "250mg", "Twice daily",
                LocalTime.of(8, 0), LocalDate.now(), null
            );
        });
        verify(medicationRepository, never()).save(any(Medication.class));
    }

    @Test
    void testAddMedicationToPet_WithNullName_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.addMedicationToPet(
                1L, 1L,
                null, "250mg", "Twice daily",
                LocalTime.of(8, 0), LocalDate.now(), null
            );
        });
        assertEquals("Medication name cannot be empty", exception.getMessage());
    }

    @Test
    void testAddMedicationToPet_WithBlankName_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.addMedicationToPet(
                1L, 1L,
                "   ", "250mg", "Twice daily",
                LocalTime.of(8, 0), LocalDate.now(), null
            );
        });
        assertEquals("Medication name cannot be empty", exception.getMessage());
    }

    @Test
    void testAddMedicationToPet_WithNullDosage_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.addMedicationToPet(
                1L, 1L,
                "Antibiotics", null, "Twice daily",
                LocalTime.of(8, 0), LocalDate.now(), null
            );
        });
        assertEquals("Dosage cannot be empty", exception.getMessage());
    }

    @Test
    void testAddMedicationToPet_WithBlankDosage_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.addMedicationToPet(
                1L, 1L,
                "Antibiotics", "   ", "Twice daily",
                LocalTime.of(8, 0), LocalDate.now(), null
            );
        });
        assertEquals("Dosage cannot be empty", exception.getMessage());
    }

    @Test
    void testAddMedicationToPet_WithNullFrequency_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.addMedicationToPet(
                1L, 1L,
                "Antibiotics", "250mg", null,
                LocalTime.of(8, 0), LocalDate.now(), null
            );
        });
        assertEquals("Frequency cannot be empty", exception.getMessage());
    }

    @Test
    void testAddMedicationToPet_WithBlankFrequency_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.addMedicationToPet(
                1L, 1L,
                "Antibiotics", "250mg", "   ",
                LocalTime.of(8, 0), LocalDate.now(), null
            );
        });
        assertEquals("Frequency cannot be empty", exception.getMessage());
    }

    @Test
    void testAddMedicationToPet_WithNullTimeToAdminister_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.addMedicationToPet(
                1L, 1L,
                "Antibiotics", "250mg", "Twice daily",
                null, LocalDate.now(), null
            );
        });
        assertEquals("Time to administer cannot be null", exception.getMessage());
    }

    @Test
    void testAddMedicationToPet_WithNullStartDate_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.addMedicationToPet(
                1L, 1L,
                "Antibiotics", "250mg", "Twice daily",
                LocalTime.of(8, 0), null, null
            );
        });
        assertEquals("Start date cannot be null", exception.getMessage());
    }

    @Test
    void testAddMedicationToPet_WithEndDateBeforeStartDate_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        LocalDate startDate = LocalDate.of(2024, 12, 1);
        LocalDate endDate = LocalDate.of(2024, 11, 1); // Before start date

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.addMedicationToPet(
                1L, 1L,
                "Antibiotics", "250mg", "Twice daily",
                LocalTime.of(8, 0), startDate, endDate
            );
        });
        assertEquals("End date cannot be before start date", exception.getMessage());
    }

    // ========================================
    // updateMedication Tests
    // ========================================

    @Test
    void testUpdateMedication_WithValidData_Success() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);

        // Act
        Medication result = medicationService.updateMedication(
            1L, 1L,
            "Updated Med", "500mg", "Once daily",
            LocalTime.of(10, 0), LocalDate.of(2024, 2, 1), LocalDate.of(2025, 2, 1)
        );

        // Assert
        assertNotNull(result);
        verify(medicationRepository).findById(1L);
        verify(petService).getPetById(1L, 1L);
        verify(medicationRepository).save(testMedication);
    }

    @Test
    void testUpdateMedication_WithInvalidMedicationId_ThrowsException() {
        // Arrange
        when(medicationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.updateMedication(
                999L, 1L,
                "Updated Med", "500mg", "Once daily",
                LocalTime.of(10, 0), null, null
            );
        });
        assertEquals("Medication not found with id: 999", exception.getMessage());
        verify(petService, never()).getPetById(anyLong(), anyLong());
    }

    @Test
    void testUpdateMedication_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            medicationService.updateMedication(
                1L, 999L,
                "Updated Med", null, null,
                null, null, null
            );
        });
        verify(medicationRepository, never()).save(any(Medication.class));
    }

    @Test
    void testUpdateMedication_WithOnlyName_UpdatesNameOnly() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);
        String originalDosage = testMedication.getDosage();

        // Act
        medicationService.updateMedication(
            1L, 1L,
            "New Name", null, null,
            null, null, null
        );

        // Assert
        assertEquals("New Name", testMedication.getName());
        assertEquals(originalDosage, testMedication.getDosage()); // Unchanged
        verify(medicationRepository).save(testMedication);
    }

    @Test
    void testUpdateMedication_WithBlankName_DoesNotUpdateName() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);
        String originalName = testMedication.getName();

        // Act
        medicationService.updateMedication(
            1L, 1L,
            "   ", null, null,
            null, null, null
        );

        // Assert
        assertEquals(originalName, testMedication.getName()); // Unchanged
    }

    @Test
    void testUpdateMedication_WithBlankDosage_DoesNotUpdateDosage() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);
        String originalDosage = testMedication.getDosage();

        // Act
        medicationService.updateMedication(
            1L, 1L,
            null, "   ", null,
            null, null, null
        );

        // Assert
        assertEquals(originalDosage, testMedication.getDosage()); // Unchanged
    }

    @Test
    void testUpdateMedication_WithBlankFrequency_DoesNotUpdateFrequency() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);
        String originalFrequency = testMedication.getFrequency();

        // Act
        medicationService.updateMedication(
            1L, 1L,
            null, null, "   ",
            null, null, null
        );

        // Assert
        assertEquals(originalFrequency, testMedication.getFrequency()); // Unchanged
    }

    @Test
    void testUpdateMedication_WithNewTimeToAdminister_UpdatesTime() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);
        LocalTime newTime = LocalTime.of(14, 30);

        // Act
        medicationService.updateMedication(
            1L, 1L,
            null, null, null,
            newTime, null, null
        );

        // Assert
        assertEquals(newTime, testMedication.getTimeToAdminister());
        verify(medicationRepository).save(testMedication);
    }

    @Test
    void testUpdateMedication_WithNewStartDate_UpdatesStartDate() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);
        LocalDate newStartDate = LocalDate.of(2024, 6, 1);

        // Act
        medicationService.updateMedication(
            1L, 1L,
            null, null, null,
            null, newStartDate, null
        );

        // Assert
        assertEquals(newStartDate, testMedication.getStartDate());
        verify(medicationRepository).save(testMedication);
    }

    @Test
    void testUpdateMedication_WithValidEndDate_UpdatesEndDate() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);
        LocalDate newEndDate = LocalDate.of(2025, 6, 1); // After current start date

        // Act
        medicationService.updateMedication(
            1L, 1L,
            null, null, null,
            null, null, newEndDate
        );

        // Assert
        assertEquals(newEndDate, testMedication.getEndDate());
        verify(medicationRepository).save(testMedication);
    }

    @Test
    void testUpdateMedication_WithEndDateBeforeCurrentStartDate_ThrowsException() {
        // Arrange
        testMedication.setStartDate(LocalDate.of(2024, 6, 1));
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        LocalDate invalidEndDate = LocalDate.of(2024, 5, 1); // Before current start date

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.updateMedication(
                1L, 1L,
                null, null, null,
                null, null, invalidEndDate
            );
        });
        assertEquals("End date cannot be before start date", exception.getMessage());
        verify(medicationRepository, never()).save(any(Medication.class));
    }

    @Test
    void testUpdateMedication_WithAllNullValues_DoesNotChangeAnything() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.save(any(Medication.class))).thenReturn(testMedication);
        
        String originalName = testMedication.getName();
        String originalDosage = testMedication.getDosage();
        String originalFrequency = testMedication.getFrequency();

        // Act
        medicationService.updateMedication(
            1L, 1L,
            null, null, null,
            null, null, null
        );

        // Assert
        assertEquals(originalName, testMedication.getName());
        assertEquals(originalDosage, testMedication.getDosage());
        assertEquals(originalFrequency, testMedication.getFrequency());
        verify(medicationRepository).save(testMedication);
    }

    // ========================================
    // deleteMedication Tests
    // ========================================

    @Test
    void testDeleteMedication_WithValidData_Success() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        doNothing().when(medicationRepository).delete(any(Medication.class));

        // Act
        Medication result = medicationService.deleteMedication(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testMedication.getId(), result.getId());
        verify(medicationRepository).findById(1L);
        verify(petService).getPetById(1L, 1L);
        verify(medicationRepository).delete(testMedication);
    }

    @Test
    void testDeleteMedication_WithInvalidMedicationId_ThrowsException() {
        // Arrange
        when(medicationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.deleteMedication(999L, 1L);
        });
        assertEquals("Medication not found with id: 999", exception.getMessage());
        verify(medicationRepository, never()).delete(any(Medication.class));
    }

    @Test
    void testDeleteMedication_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(medicationRepository.findById(1L)).thenReturn(Optional.of(testMedication));
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            medicationService.deleteMedication(1L, 999L);
        });
        verify(medicationRepository, never()).delete(any(Medication.class));
    }

    // ========================================
    // getMedicationsForPet Tests
    // ========================================

    @Test
    void testGetMedicationsForPet_WithValidPetAndOwner_ReturnsSortedList() {
        // Arrange
        Medication med1 = new Medication();
        med1.setId(1L);
        med1.setTimeToAdminister(LocalTime.of(9, 0));
        
        Medication med2 = new Medication();
        med2.setId(2L);
        med2.setTimeToAdminister(LocalTime.of(14, 0));
        
        Medication med3 = new Medication();
        med3.setId(3L);
        med3.setTimeToAdminister(LocalTime.of(20, 0));

        List<Medication> medications = Arrays.asList(med1, med2, med3);
        
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.findByPetIdOrderByTimeToAdministerAsc(1L)).thenReturn(medications);

        // Act
        List<Medication> result = medicationService.getMedicationsForPet(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(LocalTime.of(9, 0), result.get(0).getTimeToAdminister());
        assertEquals(LocalTime.of(14, 0), result.get(1).getTimeToAdminister());
        assertEquals(LocalTime.of(20, 0), result.get(2).getTimeToAdminister());
        verify(petService).getPetById(1L, 1L);
        verify(medicationRepository).findByPetIdOrderByTimeToAdministerAsc(1L);
    }

    @Test
    void testGetMedicationsForPet_WithInvalidPet_ThrowsException() {
        // Arrange
        when(petService.getPetById(999L, 1L)).thenThrow(new IllegalArgumentException("Pet not found with ID: 999"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            medicationService.getMedicationsForPet(999L, 1L);
        });
        assertEquals("Pet not found with ID: 999", exception.getMessage());
        verify(medicationRepository, never()).findByPetIdOrderByTimeToAdministerAsc(anyLong());
    }

    @Test
    void testGetMedicationsForPet_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            medicationService.getMedicationsForPet(1L, 999L);
        });
        verify(medicationRepository, never()).findByPetIdOrderByTimeToAdministerAsc(anyLong());
    }

    @Test
    void testGetMedicationsForPet_WithNoMedications_ReturnsEmptyList() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(medicationRepository.findByPetIdOrderByTimeToAdministerAsc(1L)).thenReturn(Arrays.asList());

        // Act
        List<Medication> result = medicationService.getMedicationsForPet(1L, 1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(medicationRepository).findByPetIdOrderByTimeToAdministerAsc(1L);
    }
}
