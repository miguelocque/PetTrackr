package com.PetTrackr.PetTrackr.service;

import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.entity.Pet;
import com.PetTrackr.PetTrackr.entity.VetVisit;
import com.PetTrackr.PetTrackr.repository.VetVisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VetVisitServiceTest {

    @Mock
    private VetVisitRepository vetVisitRepository;

    @Mock
    private PetService petService;

    @InjectMocks
    private VetVisitService vetVisitService;

    private Owner testOwner;
    private Pet testPet;
    private VetVisit testVetVisit;

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

        // Create test vet visit
        testVetVisit = new VetVisit();
        testVetVisit.setId(1L);
        testVetVisit.setVisitDate(LocalDate.of(2024, 12, 1));
        testVetVisit.setReasonForVisit("Annual checkup");
        testVetVisit.setVetName("Dr. Smith");
        testVetVisit.setNotes("Pet is healthy");
        testVetVisit.setNextVisitDate(LocalDate.of(2025, 12, 1));
        testVetVisit.setPet(testPet);
    }

    // ========================================
    // addVetVisitToPet Tests
    // ========================================

    @Test
    void testAddVetVisitToPet_WithValidData_Success() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(vetVisitRepository.save(any(VetVisit.class))).thenAnswer(invocation -> {
            VetVisit visit = invocation.getArgument(0);
            visit.setId(1L);
            return visit;
        });

        // Act
        VetVisit result = vetVisitService.addVetVisitToPet(
            1L, 1L,
            LocalDate.of(2024, 12, 1), LocalDate.of(2025, 12, 1), "Annual checkup",
            "Dr. Smith", "Pet is healthy"
        );

        // Assert
        assertNotNull(result);
        assertEquals("Annual checkup", result.getReasonForVisit());
        assertEquals("Dr. Smith", result.getVetName());
        assertEquals("Pet is healthy", result.getNotes());
        assertEquals(testPet, result.getPet());
        verify(petService).getPetById(1L, 1L);
        verify(vetVisitRepository).save(any(VetVisit.class));
    }

    @Test
    void testAddVetVisitToPet_WithTrimmedInputs_Success() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(vetVisitRepository.save(any(VetVisit.class))).thenAnswer(invocation -> {
            VetVisit visit = invocation.getArgument(0);
            assertEquals("Vaccination", visit.getReasonForVisit()); // Should be trimmed
            assertEquals("Dr. Jones", visit.getVetName());
            assertEquals("Good visit", visit.getNotes());
            return visit;
        });

        // Act
        vetVisitService.addVetVisitToPet(
            1L, 1L,
            LocalDate.of(2024, 12, 1), null, "  Vaccination  ",
            "  Dr. Jones  ", "  Good visit  "
        );

        // Assert
        verify(vetVisitRepository).save(any(VetVisit.class));
    }

    @Test
    void testAddVetVisitToPet_WithNullNotes_Success() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(vetVisitRepository.save(any(VetVisit.class))).thenAnswer(invocation -> {
            VetVisit visit = invocation.getArgument(0);
            assertNull(visit.getNotes());
            return visit;
        });

        // Act
        vetVisitService.addVetVisitToPet(
            1L, 1L,
            LocalDate.of(2024, 12, 1), null, "Surgery",
            "Dr. Brown", null
        );

        // Assert
        verify(vetVisitRepository).save(any(VetVisit.class));
    }

    @Test
    void testAddVetVisitToPet_WithInvalidPet_ThrowsException() {
        // Arrange
        when(petService.getPetById(999L, 1L)).thenThrow(new IllegalArgumentException("Pet not found with ID: 999"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.addVetVisitToPet(
                999L, 1L,
                LocalDate.of(2024, 12, 1), null, "Checkup",
                "Dr. Smith", "Notes"
            );
        });
        assertEquals("Pet not found with ID: 999", exception.getMessage());
        verify(vetVisitRepository, never()).save(any(VetVisit.class));
    }

    @Test
    void testAddVetVisitToPet_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied: Pet does not belong to this owner"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            vetVisitService.addVetVisitToPet(
                1L, 999L,
                LocalDate.of(2024, 12, 1), null, "Checkup",
                "Dr. Smith", "Notes"
            );
        });
        verify(vetVisitRepository, never()).save(any(VetVisit.class));
    }

    @Test
    void testAddVetVisitToPet_WithNullReason_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.addVetVisitToPet(
                1L, 1L,
                LocalDate.of(2024, 12, 1), null, null,
                "Dr. Smith", "Notes"
            );
        });
        assertEquals("Reason for visit cannot be empty", exception.getMessage());
    }

    @Test
    void testAddVetVisitToPet_WithBlankReason_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.addVetVisitToPet(
                1L, 1L,
                LocalDate.of(2024, 12, 1), null, "   ",
                "Dr. Smith", "Notes"
            );
        });
        assertEquals("Reason for visit cannot be empty", exception.getMessage());
    }

    @Test
    void testAddVetVisitToPet_WithNullVetName_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.addVetVisitToPet(
                1L, 1L,
                LocalDate.of(2024, 12, 1), null, "Checkup",
                null, "Notes"
            );
        });
        assertEquals("Vet name cannot be empty", exception.getMessage());
    }

    @Test
    void testAddVetVisitToPet_WithBlankVetName_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.addVetVisitToPet(
                1L, 1L,
                LocalDate.of(2024, 12, 1), null, "Checkup",
                "   ", "Notes"
            );
        });
        assertEquals("Vet name cannot be empty", exception.getMessage());
    }

    @Test
    void testAddVetVisitToPet_WithNullVisitDate_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.addVetVisitToPet(
                1L, 1L,
                null, null, "Checkup",
                "Dr. Smith", "Notes"
            );
        });
        assertEquals("Visit date cannot be null", exception.getMessage());
    }

    // ========================================
    // updateVetVisit Tests
    // ========================================

    @Test
    void testUpdateVetVisit_WithValidData_Success() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(vetVisitRepository.save(any(VetVisit.class))).thenReturn(testVetVisit);

        // Act
        VetVisit result = vetVisitService.updateVetVisit(
            1L, 1L,
            LocalDate.of(2024, 12, 15), LocalDate.of(2025, 12, 15), "Follow-up",
            "Dr. Williams", "Updated notes"
        );

        // Assert
        assertNotNull(result);
        verify(vetVisitRepository).findById(1L);
        verify(petService).getPetById(1L, 1L);
        verify(vetVisitRepository).save(testVetVisit);
    }

    @Test
    void testUpdateVetVisit_WithInvalidVisitId_ThrowsException() {
        // Arrange
        when(vetVisitRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.updateVetVisit(
                999L, 1L,
                LocalDate.of(2024, 12, 15), null, "Follow-up",
                "Dr. Williams", "Updated notes"
            );
        });
        assertEquals("Vet visit not found with id: 999", exception.getMessage());
        verify(petService, never()).getPetById(anyLong(), anyLong());
    }

    @Test
    void testUpdateVetVisit_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            vetVisitService.updateVetVisit(
                1L, 999L,
                LocalDate.of(2024, 12, 15), null, "Follow-up",
                "Dr. Williams", "Updated notes"
            );
        });
        verify(vetVisitRepository, never()).save(any(VetVisit.class));
    }

    @Test
    void testUpdateVetVisit_WithOnlyReason_UpdatesReasonOnly() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(vetVisitRepository.save(any(VetVisit.class))).thenReturn(testVetVisit);
        String originalVetName = testVetVisit.getVetName();

        // Act
        vetVisitService.updateVetVisit(
            1L, 1L,
            null, null, "New Reason",
            null, null
        );

        // Assert
        assertEquals("New Reason", testVetVisit.getReasonForVisit());
        assertEquals(originalVetName, testVetVisit.getVetName()); // Unchanged
        verify(vetVisitRepository).save(testVetVisit);
    }

    @Test
    void testUpdateVetVisit_WithBlankReason_ThrowsException() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.updateVetVisit(
                1L, 1L,
                null, null, "   ",
                null, null
            );
        });
        assertEquals("Reason for visit cannot be empty", exception.getMessage());
        verify(vetVisitRepository, never()).save(any(VetVisit.class));
    }

    @Test
    void testUpdateVetVisit_WithOnlyVetName_UpdatesVetNameOnly() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(vetVisitRepository.save(any(VetVisit.class))).thenReturn(testVetVisit);
        String originalReason = testVetVisit.getReasonForVisit();

        // Act
        vetVisitService.updateVetVisit(
            1L, 1L,
            null, null, null,
            "Dr. New", null
        );

        // Assert
        assertEquals("Dr. New", testVetVisit.getVetName());
        assertEquals(originalReason, testVetVisit.getReasonForVisit()); // Unchanged
        verify(vetVisitRepository).save(testVetVisit);
    }

    @Test
    void testUpdateVetVisit_WithBlankVetName_ThrowsException() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.updateVetVisit(
                1L, 1L,
                null, null, null,
                "   ", null
            );
        });
        assertEquals("Vet name cannot be empty", exception.getMessage());
        verify(vetVisitRepository, never()).save(any(VetVisit.class));
    }

    @Test
    void testUpdateVetVisit_WithOnlyVisitDate_UpdatesVisitDateOnly() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(vetVisitRepository.save(any(VetVisit.class))).thenReturn(testVetVisit);
        String originalReason = testVetVisit.getReasonForVisit();
        LocalDate newDate = LocalDate.of(2024, 12, 20);

        // Act
        vetVisitService.updateVetVisit(
            1L, 1L,
            newDate, null, null,
            null, null
        );

        // Assert
        assertEquals(newDate, testVetVisit.getVisitDate());
        assertEquals(originalReason, testVetVisit.getReasonForVisit()); // Unchanged
        verify(vetVisitRepository).save(testVetVisit);
    }

    @Test
    void testUpdateVetVisit_WithOnlyNotes_UpdatesNotesOnly() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(vetVisitRepository.save(any(VetVisit.class))).thenReturn(testVetVisit);
        String originalReason = testVetVisit.getReasonForVisit();

        // Act
        vetVisitService.updateVetVisit(
            1L, 1L,
            null, null, null,
            null, "Updated notes"
        );

        // Assert
        assertEquals("Updated notes", testVetVisit.getNotes());
        assertEquals(originalReason, testVetVisit.getReasonForVisit()); // Unchanged
        verify(vetVisitRepository).save(testVetVisit);
    }

    @Test
    void testUpdateVetVisit_WithAllNullValues_DoesNotChangeAnything() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(vetVisitRepository.save(any(VetVisit.class))).thenReturn(testVetVisit);
        
        LocalDate originalDate = testVetVisit.getVisitDate();
        String originalReason = testVetVisit.getReasonForVisit();
        String originalVetName = testVetVisit.getVetName();
        String originalNotes = testVetVisit.getNotes();

        // Act
        vetVisitService.updateVetVisit(
            1L, 1L,
            null, null, null,
            null, null
        );

        // Assert
        assertEquals(originalDate, testVetVisit.getVisitDate());
        assertEquals(originalReason, testVetVisit.getReasonForVisit());
        assertEquals(originalVetName, testVetVisit.getVetName());
        assertEquals(originalNotes, testVetVisit.getNotes());
        verify(vetVisitRepository).save(testVetVisit);
    }

    // ========================================
    // getVetVisitsForPet Tests
    // ========================================

    @Test
    void testGetVetVisitsForPet_WithValidPetAndOwner_ReturnsSortedList() {
        // Arrange
        VetVisit visit1 = new VetVisit();
        visit1.setId(1L);
        visit1.setVisitDate(LocalDate.of(2024, 1, 15));
        visit1.setReasonForVisit("Checkup");
        
        VetVisit visit2 = new VetVisit();
        visit2.setId(2L);
        visit2.setVisitDate(LocalDate.of(2024, 6, 20));
        visit2.setReasonForVisit("Vaccination");
        
        VetVisit visit3 = new VetVisit();
        visit3.setId(3L);
        visit3.setVisitDate(LocalDate.of(2024, 12, 10));
        visit3.setReasonForVisit("Surgery");

        List<VetVisit> visits = Arrays.asList(visit1, visit2, visit3);
        
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(vetVisitRepository.findByPetIdOrderByVisitDateAsc(1L)).thenReturn(visits);

        // Act
        List<VetVisit> result = vetVisitService.getVetVisitsForPet(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals(LocalDate.of(2024, 1, 15), result.get(0).getVisitDate());
        assertEquals(LocalDate.of(2024, 6, 20), result.get(1).getVisitDate());
        assertEquals(LocalDate.of(2024, 12, 10), result.get(2).getVisitDate());
        verify(petService).getPetById(1L, 1L);
        verify(vetVisitRepository).findByPetIdOrderByVisitDateAsc(1L);
    }

    @Test
    void testGetVetVisitsForPet_WithInvalidPet_ThrowsException() {
        // Arrange
        when(petService.getPetById(999L, 1L)).thenThrow(new IllegalArgumentException("Pet not found with ID: 999"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.getVetVisitsForPet(999L, 1L);
        });
        assertEquals("Pet not found with ID: 999", exception.getMessage());
        verify(vetVisitRepository, never()).findByPetIdOrderByVisitDateAsc(anyLong());
    }

    @Test
    void testGetVetVisitsForPet_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            vetVisitService.getVetVisitsForPet(1L, 999L);
        });
        verify(vetVisitRepository, never()).findByPetIdOrderByVisitDateAsc(anyLong());
    }

    @Test
    void testGetVetVisitsForPet_WithNoVisits_ReturnsEmptyList() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(vetVisitRepository.findByPetIdOrderByVisitDateAsc(1L)).thenReturn(Arrays.asList());

        // Act
        List<VetVisit> result = vetVisitService.getVetVisitsForPet(1L, 1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(vetVisitRepository).findByPetIdOrderByVisitDateAsc(1L);
    }

    // ========================================
    // getVetVisitById Tests
    // ========================================

    @Test
    void testGetVetVisitById_WithValidData_Success() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act
        VetVisit result = vetVisitService.getVetVisitById(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testVetVisit.getId(), result.getId());
        verify(vetVisitRepository).findById(1L);
        verify(petService).getPetById(1L, 1L);
    }

    @Test
    void testGetVetVisitById_WithInvalidVisitId_ThrowsException() {
        // Arrange
        when(vetVisitRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.getVetVisitById(999L, 1L);
        });
        assertEquals("Vet visit not found with id: 999", exception.getMessage());
        verify(petService, never()).getPetById(anyLong(), anyLong());
    }

    @Test
    void testGetVetVisitById_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            vetVisitService.getVetVisitById(1L, 999L);
        });
    }

    // ========================================
    // deleteVetVisit Tests
    // ========================================

    @Test
    void testDeleteVetVisit_WithValidData_Success() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        doNothing().when(vetVisitRepository).delete(any(VetVisit.class));

        // Act
        VetVisit result = vetVisitService.deleteVetVisit(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals(testVetVisit.getId(), result.getId());
        verify(vetVisitRepository).findById(1L);
        verify(petService).getPetById(1L, 1L);
        verify(vetVisitRepository).delete(testVetVisit);
    }

    @Test
    void testDeleteVetVisit_WithInvalidVisitId_ThrowsException() {
        // Arrange
        when(vetVisitRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            vetVisitService.deleteVetVisit(999L, 1L);
        });
        assertEquals("Vet visit not found with id: 999", exception.getMessage());
        verify(vetVisitRepository, never()).delete(any(VetVisit.class));
    }

    @Test
    void testDeleteVetVisit_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(vetVisitRepository.findById(1L)).thenReturn(Optional.of(testVetVisit));
        when(petService.getPetById(1L, 999L)).thenThrow(new SecurityException("Access denied"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            vetVisitService.deleteVetVisit(1L, 999L);
        });
        verify(vetVisitRepository, never()).delete(any(VetVisit.class));
    }
}
