package com.PetTrackr.PetTrackr.service;

import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.entity.Pet;
import com.PetTrackr.PetTrackr.repository.OwnerRepository;
import com.PetTrackr.PetTrackr.repository.PetRepository;
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
class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private ImageUploadService imageUploadService;

    @InjectMocks
    private PetService petService;

    private Owner testOwner;
    private Pet testPet;

    @BeforeEach
    void setUp() {
        // Create test owner
        testOwner = new Owner();
        testOwner.setId(1L);
        testOwner.setName("Test Owner");
        testOwner.setEmail("test@example.com");
        testOwner.setPhoneNumber("555-0000");
        testOwner.setPasswordHash("hashedpassword");

        // Create test pet
        testPet = new Pet();
        testPet.setId(1L);
        testPet.setName("Max");
        testPet.setType("Dog");
        testPet.setBreed("Golden Retriever");
        testPet.setAge(3);
        testPet.setWeight(30.0);
        testPet.setWeightType(Pet.WeightType.KG);
        testPet.setDateOfBirth(LocalDate.of(2021, 6, 15));
        testPet.setActivityLevel(Pet.ActivityLevel.HIGH);
        testPet.setOwner(testOwner);
    }

    // ========================================
    // UC-2: Create Pet Profile Tests
    // ========================================

    @Test
    void testCreatePet_WithValidData_Success() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        // Act
        Pet result = petService.createPet(
            1L, "Max", "Dog", "Golden Retriever",
            30.0, Pet.WeightType.KG,
            LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
        );

        // Assert
        assertNotNull(result);
        assertEquals("Max", result.getName());
        assertEquals("Dog", result.getType());
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    void testCreatePet_WithInvalidOwnerId_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                999L, "Max", "Dog", "Golden Retriever",
                30.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
            );
        });
    }

    @Test
    void testCreatePet_WithEmptyName_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                1L, "", "Dog", "Golden Retriever",
                30.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
            );
        });
    }

    @Test
    void testCreatePet_WithFutureDateOfBirth_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                1L, "Max", "Dog", "Golden Retriever",
                30.0, Pet.WeightType.KG,
                LocalDate.now().plusDays(1), Pet.ActivityLevel.HIGH
            );
        });
    }

    @Test
    void testCreatePet_WithZeroWeight_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                1L, "Max", "Dog", "Golden Retriever",
                0.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
            );
        });
    }

    @Test
    void testCreatePet_WithNullName_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                1L, null, "Dog", "Golden Retriever",
                30.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
            );
        });
        assertEquals("Pet name cannot be empty", exception.getMessage());
    }

    @Test
    void testCreatePet_WithBlankName_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                1L, "   ", "Dog", "Golden Retriever",
                30.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
            );
        });
        assertEquals("Pet name cannot be empty", exception.getMessage());
    }

    @Test
    void testCreatePet_WithNullType_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                1L, "Max", null, "Golden Retriever",
                30.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
            );
        });
        assertEquals("Pet type cannot be empty", exception.getMessage());
    }

    @Test
    void testCreatePet_WithBlankType_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                1L, "Max", "   ", "Golden Retriever",
                30.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
            );
        });
        assertEquals("Pet type cannot be empty", exception.getMessage());
    }

    @Test
    void testCreatePet_WithNullBreed_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                1L, "Max", "Dog", null,
                30.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
            );
        });
        assertEquals("Pet breed cannot be empty", exception.getMessage());
    }

    @Test
    void testCreatePet_WithBlankBreed_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                1L, "Max", "Dog", "   ",
                30.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
            );
        });
        assertEquals("Pet breed cannot be empty", exception.getMessage());
    }

    @Test
    void testCreatePet_WithNullDateOfBirth_Success() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(petRepository.save(any(Pet.class))).thenAnswer(invocation -> {
            Pet pet = invocation.getArgument(0);
            pet.setId(1L);
            return pet;
        });

        // Act
        Pet result = petService.createPet(
            1L, "Max", "Dog", "Golden Retriever",
            30.0, Pet.WeightType.KG,
            null, Pet.ActivityLevel.HIGH
        );

        // Assert
        assertNotNull(result);
        assertNull(result.getDateOfBirth()); // DateOfBirth can be null
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void testCreatePet_WithNegativeWeight_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                1L, "Max", "Dog", "Golden Retriever",
                -5.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
            );
        });
        assertEquals("Pet weight must be greater than zero", exception.getMessage());
    }

    // ========================================
    // UC-3: View All Owner's Pets Tests
    // ========================================

    @Test
    void testGetAllPetsByOwnerId_WithValidOwnerId_ReturnsPets() {
        // Arrange
        Pet pet2 = new Pet();
        pet2.setId(2L);
        pet2.setName("Luna");
        pet2.setOwner(testOwner);

        List<Pet> pets = Arrays.asList(testPet, pet2);
        when(ownerRepository.existsById(1L)).thenReturn(true);
        when(petRepository.findByOwnerId(1L)).thenReturn(pets);

        // Act
        List<Pet> result = petService.getAllPetsByOwnerId(1L);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Max", result.get(0).getName());
        assertEquals("Luna", result.get(1).getName());
    }

    @Test
    void testGetAllPetsByOwnerId_WithInvalidOwnerId_ThrowsException() {
        // Arrange
        when(ownerRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            petService.getAllPetsByOwnerId(999L);
        });
    }

    @Test
    void testGetAllPetsByOwnerId_WithNoOwnedPets_ReturnsEmptyList() {
        // Arrange
        when(ownerRepository.existsById(1L)).thenReturn(true);
        when(petRepository.findByOwnerId(1L)).thenReturn(Arrays.asList());

        // Act
        List<Pet> result = petService.getAllPetsByOwnerId(1L);

        // Assert
        assertTrue(result.isEmpty());
    }

    // ========================================
    // UC-4: View Pet Details Tests
    // ========================================

    @Test
    void testGetPetById_WithValidPetAndOwner_Success() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act
        Pet result = petService.getPetById(1L, 1L);

        // Assert
        assertNotNull(result);
        assertEquals("Max", result.getName());
        assertEquals(1L, result.getOwner().getId());
    }

    @Test
    void testGetPetById_WithInvalidPetId_ThrowsException() {
        // Arrange
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            petService.getPetById(999L, 1L);
        });
    }

    @Test
    void testGetPetById_WithWrongOwner_ThrowsSecurityException() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            petService.getPetById(1L, 999L); // Wrong owner ID
        });
    }

    // ========================================
    // Update Pet Tests
    // ========================================

    @Test
    void testUpdatePet_WithValidData_Success() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        // Act
        Pet result = petService.updatePet(
            1L, 1L,
            "Max Updated", "Dog", "Labrador",
            LocalDate.of(2020, 6, 15), 32.0, Pet.WeightType.KG,
            Pet.ActivityLevel.MEDIUM
        );

        // Assert
        assertNotNull(result);
        verify(petRepository, times(1)).save(any(Pet.class));
    }

    @Test
    void testUpdatePet_WithWrongOwner_ThrowsSecurityException() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            petService.updatePet(
                1L, 999L, // Wrong owner ID
                "Max Updated", null, null,
                null, null, null, null
            );
        });
    }

    @Test
    void testUpdatePet_WithOnlyName_UpdatesNameOnly() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);
        String originalType = testPet.getType();

        // Act
        petService.updatePet(
            1L, 1L,
            "New Name", null, null,
            null, null, null, null
        );

        // Assert
        assertEquals("New Name", testPet.getName());
        assertEquals(originalType, testPet.getType()); // Type unchanged
        verify(petRepository).save(testPet);
    }

    @Test
    void testUpdatePet_WithOnlyType_UpdatesTypeOnly() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);
        String originalName = testPet.getName();

        // Act
        petService.updatePet(
            1L, 1L,
            null, "Cat", null,
            null, null, null, null
        );

        // Assert
        assertEquals(originalName, testPet.getName()); // Name unchanged
        assertEquals("Cat", testPet.getType());
        verify(petRepository).save(testPet);
    }

    @Test
    void testUpdatePet_WithOnlyBreed_UpdatesBreedOnly() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);
        String originalName = testPet.getName();

        // Act
        petService.updatePet(
            1L, 1L,
            null, null, "Poodle",
            null, null, null, null
        );

        // Assert
        assertEquals(originalName, testPet.getName()); // Name unchanged
        assertEquals("Poodle", testPet.getBreed());
        verify(petRepository).save(testPet);
    }

    @Test
    void testUpdatePet_WithBlankName_DoesNotUpdateName() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);
        String originalName = testPet.getName();

        // Act
        petService.updatePet(
            1L, 1L,
            "   ", null, null,
            null, null, null, null
        );

        // Assert
        assertEquals(originalName, testPet.getName()); // Name unchanged
        verify(petRepository).save(testPet);
    }

    @Test
    void testUpdatePet_WithBlankType_DoesNotUpdateType() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);
        String originalType = testPet.getType();

        // Act
        petService.updatePet(
            1L, 1L,
            null, "   ", null,
            null, null, null, null
        );

        // Assert
        assertEquals(originalType, testPet.getType()); // Type unchanged
        verify(petRepository).save(testPet);
    }

    @Test
    void testUpdatePet_WithBlankBreed_DoesNotUpdateBreed() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);
        String originalBreed = testPet.getBreed();

        // Act
        petService.updatePet(
            1L, 1L,
            null, null, "   ",
            null, null, null, null
        );

        // Assert
        assertEquals(originalBreed, testPet.getBreed()); // Breed unchanged
        verify(petRepository).save(testPet);
    }

    @Test
    void testUpdatePet_WithValidDateOfBirth_UpdatesDateOfBirthAndAge() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);
        LocalDate newDateOfBirth = LocalDate.of(2020, 1, 1);

        // Act
        petService.updatePet(
            1L, 1L,
            null, null, null,
            newDateOfBirth, null, null, null
        );

        // Assert
        assertEquals(newDateOfBirth, testPet.getDateOfBirth());
        // Age is calculated from dateOfBirth
        assertTrue(testPet.getAge() >= 4); // Should be about 5 years old
        verify(petRepository).save(testPet);
    }

    @Test
    void testUpdatePet_WithFutureDateOfBirth_ThrowsException() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            petService.updatePet(
                1L, 1L,
                null, null, null,
                LocalDate.now().plusDays(1), null, null, null
            );
        });
        assertEquals("Date of birth cannot be in the future", exception.getMessage());
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void testUpdatePet_WithValidWeight_UpdatesWeight() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        // Act
        petService.updatePet(
            1L, 1L,
            null, null, null,
            null, 35.5, null, null
        );

        // Assert
        assertEquals(35.5, testPet.getWeight());
        verify(petRepository).save(testPet);
    }

    @Test
    void testUpdatePet_WithZeroWeight_ThrowsException() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            petService.updatePet(
                1L, 1L,
                null, null, null,
                null, 0.0, null, null
            );
        });
        assertEquals("Weight must be greater than zero", exception.getMessage());
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void testUpdatePet_WithNegativeWeight_ThrowsException() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            petService.updatePet(
                1L, 1L,
                null, null, null,
                null, -5.0, null, null
            );
        });
        assertEquals("Weight must be greater than zero", exception.getMessage());
        verify(petRepository, never()).save(any(Pet.class));
    }

    @Test
    void testUpdatePet_WithWeightType_UpdatesWeightType() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        // Act
        petService.updatePet(
            1L, 1L,
            null, null, null,
            null, null, Pet.WeightType.LBS, null
        );

        // Assert
        assertEquals(Pet.WeightType.LBS, testPet.getWeightType());
        verify(petRepository).save(testPet);
    }

    @Test
    void testUpdatePet_WithActivityLevel_UpdatesActivityLevel() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        // Act
        petService.updatePet(
            1L, 1L,
            null, null, null,
            null, null, null, Pet.ActivityLevel.HIGH
        );

        // Assert
        assertEquals(Pet.ActivityLevel.HIGH, testPet.getActivityLevel());
        verify(petRepository).save(testPet);
    }

    @Test
    void testUpdatePet_WithAllNullValues_DoesNotChangeAnything() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);
        String originalName = testPet.getName();
        String originalType = testPet.getType();
        String originalBreed = testPet.getBreed();

        // Act
        petService.updatePet(
            1L, 1L,
            null, null, null,
            null, null, null, null
        );

        // Assert
        assertEquals(originalName, testPet.getName());
        assertEquals(originalType, testPet.getType());
        assertEquals(originalBreed, testPet.getBreed());
        verify(petRepository).save(testPet);
    }

    // ========================================
    // Delete Pet Tests
    // ========================================

    @Test
    void testDeletePet_WithValidPetAndOwner_Success() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        doNothing().when(petRepository).delete(any(Pet.class));

        // Act
        petService.deletePet(1L, 1L);

        // Assert
        verify(petRepository, times(1)).delete(testPet);
    }

    @Test
    void testDeletePet_WithWrongOwner_ThrowsSecurityException() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            petService.deletePet(1L, 999L); // Wrong owner ID
        });
    }

    // ========================================
    // Authorization Helper Tests
    // ========================================

    @Test
    void testIsPetOwnedBy_WithCorrectOwner_ReturnsTrue() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act
        boolean result = petService.isPetOwnedBy(1L, 1L);

        // Assert
        assertTrue(result);
    }

    @Test
    void testIsPetOwnedBy_WithWrongOwner_ReturnsFalse() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));

        // Act
        boolean result = petService.isPetOwnedBy(1L, 999L);

        // Assert
        assertFalse(result);
    }

    @Test
    void testIsPetOwnedBy_WithNonExistentPet_ReturnsFalse() {
        // Arrange
        when(petRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        boolean result = petService.isPetOwnedBy(999L, 1L);

        // Assert
        assertFalse(result);
    }
}