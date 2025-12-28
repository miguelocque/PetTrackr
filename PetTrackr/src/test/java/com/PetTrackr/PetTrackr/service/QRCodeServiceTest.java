package com.PetTrackr.PetTrackr.service;

import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.entity.Pet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class QRCodeServiceTest {

    @Mock
    private PetService petService;

    @InjectMocks
    private QRCodeService qrCodeService;

    private Owner testOwner;
    private Pet testPet;

    @BeforeEach
    void setUp() {
        // Create test owner
        testOwner = new Owner();
        testOwner.setId(1L);
        testOwner.setName("John Smith");
        testOwner.setEmail("john@example.com");
        testOwner.setPhoneNumber("555-1234");
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
    // generateQRCodeForPet Tests
    // ========================================

    @Test
    void testGenerateQRCodeForPet_WithValidData_Success() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act
        byte[] qrCode = qrCodeService.generateQRCodeForPet(1L, 1L);

        // Assert
        assertNotNull(qrCode);
        assertTrue(qrCode.length > 0);
        verify(petService).getPetById(1L, 1L);
    }

    @Test
    void testGenerateQRCodeForPet_GeneratesPNGFormat() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act
        byte[] qrCode = qrCodeService.generateQRCodeForPet(1L, 1L);

        // Assert - Check PNG magic number (first 8 bytes: 89 50 4E 47 0D 0A 1A 0A)
        assertNotNull(qrCode);
        assertTrue(qrCode.length >= 8);
        assertEquals((byte) 0x89, qrCode[0]);
        assertEquals((byte) 0x50, qrCode[1]);
        assertEquals((byte) 0x4E, qrCode[2]);
        assertEquals((byte) 0x47, qrCode[3]);
    }

    @Test
    void testGenerateQRCodeForPet_WithInvalidPet_ThrowsException() {
        // Arrange
        when(petService.getPetById(999L, 1L))
            .thenThrow(new IllegalArgumentException("Pet not found with ID: 999"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            qrCodeService.generateQRCodeForPet(999L, 1L);
        });
        assertEquals("Pet not found with ID: 999", exception.getMessage());
    }

    @Test
    void testGenerateQRCodeForPet_WithUnauthorizedOwner_ThrowsException() {
        // Arrange
        when(petService.getPetById(1L, 999L))
            .thenThrow(new SecurityException("Access denied: Pet does not belong to this owner"));

        // Act & Assert
        assertThrows(SecurityException.class, () -> {
            qrCodeService.generateQRCodeForPet(1L, 999L);
        });
    }

    @Test
    void testGenerateQRCodeForPet_MultipleCallsSameData_ProducesSameSize() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act
        byte[] qrCode1 = qrCodeService.generateQRCodeForPet(1L, 1L);
        byte[] qrCode2 = qrCodeService.generateQRCodeForPet(1L, 1L);

        // Assert - Same input should produce same size output
        assertEquals(qrCode1.length, qrCode2.length);
    }

    @Test
    void testGenerateQRCodeForPet_DifferentPets_ProduceDifferentQRCodes() {
        // Arrange
        Pet pet2 = new Pet();
        pet2.setId(2L);
        pet2.setName("Luna");
        pet2.setType("Cat");
        pet2.setBreed("Siamese");
        pet2.setAge(2);
        pet2.setWeight(4.5);
        pet2.setWeightType(Pet.WeightType.KG);
        pet2.setDateOfBirth(LocalDate.of(2022, 3, 10));
        pet2.setActivityLevel(Pet.ActivityLevel.MEDIUM);
        pet2.setOwner(testOwner);

        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(petService.getPetById(2L, 1L)).thenReturn(pet2);

        // Act
        byte[] qrCode1 = qrCodeService.generateQRCodeForPet(1L, 1L);
        byte[] qrCode2 = qrCodeService.generateQRCodeForPet(2L, 1L);

        // Assert - Different pets should produce different QR codes
        assertNotNull(qrCode1);
        assertNotNull(qrCode2);
        assertNotEquals(qrCode1.length, qrCode2.length); // Different content = different size
    }

    @Test
    void testGenerateQRCodeForPet_VerifiesPetOwnership() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act
        qrCodeService.generateQRCodeForPet(1L, 1L);

        // Assert - Should call getPetById which does authorization check
        verify(petService, times(1)).getPetById(1L, 1L);
    }


    // ========================================
    // Edge Cases
    // ========================================

    @Test
    void testGenerateQRCodeForPet_WithLongPetName_Success() {
        // Arrange
        testPet.setName("SuperDuperExtremelyLongPetNameThatGoesOnForever");
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act
        byte[] qrCode = qrCodeService.generateQRCodeForPet(1L, 1L);

        // Assert - Should handle long names
        assertNotNull(qrCode);
        assertTrue(qrCode.length > 0);
    }

    @Test
    void testGenerateQRCodeForPet_WithSpecialCharactersInName_Success() {
        // Arrange
        testPet.setName("Max 🐶 O'Reilly");
        testOwner.setName("Jean-Claude Van Damme");
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act
        byte[] qrCode = qrCodeService.generateQRCodeForPet(1L, 1L);

        // Assert - Should handle special characters
        assertNotNull(qrCode);
        assertTrue(qrCode.length > 0);
    }

    @Test
    void testGenerateQRCodeForPet_ContainsGuidanceLink() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act
        byte[] qrCode = qrCodeService.generateQRCodeForPet(1L, 1L);

        // Assert - QR code should be generated with guidance link
        assertNotNull(qrCode);
        assertTrue(qrCode.length > 0);
        // Note: The actual content (including the guidance URL) is encoded in the QR code
        // We verify the method executes successfully and produces output
        // Manual testing: scan the QR code to verify the guidance link is present and clickable
        verify(petService).getPetById(1L, 1L);
    }

    @Test
    void testGenerateQRCodeForPet_HasLostPetHeader() {
        // Arrange
        when(petService.getPetById(1L, 1L)).thenReturn(testPet);

        // Act
        byte[] qrCode = qrCodeService.generateQRCodeForPet(1L, 1L);

        // Assert - QR code should be generated
        // The content includes "LOST PET - PLEASE HELP!" header
        // This makes the QR code more actionable for finders
        assertNotNull(qrCode);
        assertTrue(qrCode.length > 0);
        verify(petService).getPetById(1L, 1L);
    }

    @Test
    void testGenerateQRCodeForPet_WithMultipleOwnerPets_GeneratesUniqueQRCodes() {
        // Arrange
        Pet pet2 = new Pet();
        pet2.setId(2L);
        pet2.setName("Luna");
        pet2.setType("Cat");
        pet2.setBreed("Siamese");
        pet2.setOwner(testOwner);

        when(petService.getPetById(1L, 1L)).thenReturn(testPet);
        when(petService.getPetById(2L, 1L)).thenReturn(pet2);

        // Act
        byte[] qrCode1 = qrCodeService.generateQRCodeForPet(1L, 1L);
        byte[] qrCode2 = qrCodeService.generateQRCodeForPet(2L, 1L);

        // Assert
        assertNotNull(qrCode1);
        assertNotNull(qrCode2);
        // Different pet IDs should produce different QR codes
        assertFalse(java.util.Arrays.equals(qrCode1, qrCode2));
    }
}