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
            3, 30.0, Pet.WeightType.KG,
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
                3, 30.0, Pet.WeightType.KG,
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
                3, 30.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
            );
        });
    }

    @Test
    void testCreatePet_WithNegativeAge_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            petService.createPet(
                1L, "Max", "Dog", "Golden Retriever",
                -1, 30.0, Pet.WeightType.KG,
                LocalDate.of(2021, 6, 15), Pet.ActivityLevel.HIGH
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
                3, 0.0, Pet.WeightType.KG,
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
                3, 30.0, Pet.WeightType.KG,
                LocalDate.now().plusDays(1), Pet.ActivityLevel.HIGH
            );
        });
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
            4, 32.0, Pet.WeightType.KG,
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
    // Photo Update Tests
    // ========================================

    @Test
    void testUpdatePetPhoto_WithValidData_Success() {
        // Arrange
        when(petRepository.findById(1L)).thenReturn(Optional.of(testPet));
        when(petRepository.save(any(Pet.class))).thenReturn(testPet);

        // Act
        Pet result = petService.updatePetPhoto(1L, "/uploads/pet_1_12345.jpg", 1L);

        // Assert
        assertNotNull(result);
        verify(petRepository, times(1)).save(any(Pet.class));
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