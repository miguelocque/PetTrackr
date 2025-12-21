package com.PetTrackr.PetTrackr.service;

import com.PetTrackr.PetTrackr.entity.Owner;
import com.PetTrackr.PetTrackr.repository.OwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OwnerServiceTest {

    @Mock
    private OwnerRepository ownerRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private OwnerService ownerService;

    private Owner testOwner;

    @BeforeEach
    void setUp() {
        testOwner = new Owner();
        testOwner.setId(1L);
        testOwner.setEmail("john@example.com");
        testOwner.setName("John Doe");
        testOwner.setPhoneNumber("1234567890");
        testOwner.setPasswordHash("$2a$10$hashedPassword");
    }

    // ========== registerOwner Tests ==========

    @Test
    void testRegisterOwner_WithValidData_Success() {
        // Arrange
        String email = "jane@example.com";
        String name = "Jane Smith";
        String phoneNumber = "9876543210";
        String rawPassword = "password123";
        String hashedPassword = "$2a$10$hashedPassword";

        when(ownerRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(ownerRepository.save(any(Owner.class))).thenAnswer(invocation -> {
            Owner owner = invocation.getArgument(0);
            owner.setId(2L);
            return owner;
        });

        // Act
        Owner result = ownerService.registerOwner(email, name, phoneNumber, rawPassword);

        // Assert
        assertNotNull(result);
        assertEquals("jane@example.com", result.getEmail()); // email should be lowercase
        assertEquals(name, result.getName());
        assertEquals(phoneNumber, result.getPhoneNumber());
        assertEquals(hashedPassword, result.getPasswordHash());
        
        verify(ownerRepository).existsByEmail("jane@example.com");
        verify(passwordEncoder).encode(rawPassword);
        verify(ownerRepository).save(any(Owner.class));
    }

    @Test
    void testRegisterOwner_WithEmailTrim_Success() {
        // Arrange
        String email = "  JANE@EXAMPLE.COM  ";
        String name = "Jane Smith";
        String phoneNumber = "9876543210";
        String rawPassword = "password123";

        when(ownerRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn("hashed");
        when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

        // Act
        ownerService.registerOwner(email, name, phoneNumber, rawPassword);

        // Assert
        verify(ownerRepository).existsByEmail("jane@example.com");
    }

    @Test
    void testRegisterOwner_WithNullEmail_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner(null, "John", "1234567890", "password");
        });
        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithEmptyEmail_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("   ", "John", "1234567890", "password");
        });
        assertEquals("Email cannot be empty", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithInvalidEmailFormat_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("invalidemail", "John", "1234567890", "password");
        });
        assertEquals("Email format is invalid", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithInvalidEmailNoAtSymbol_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("invalidemail.com", "John", "1234567890", "password");
        });
        assertEquals("Email format is invalid", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithInvalidEmailNoDomain_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("user@", "John", "1234567890", "password");
        });
        assertEquals("Email format is invalid", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithNullPhoneNumber_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("john@example.com", "John", null, "password");
        });
        assertEquals("Phone number cannot be empty", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithEmptyPhoneNumber_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("john@example.com", "John", "   ", "password");
        });
        assertEquals("Phone number cannot be empty", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithInvalidPhoneNumberFormat_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("john@example.com", "John", "abc", "password");
        });
        assertEquals("Phone number format is invalid", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithShortPhoneNumber_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("john@example.com", "John", "123", "password");
        });
        assertEquals("Phone number format is invalid", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithNullName_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("john@example.com", null, "1234567890", "password");
        });
        assertEquals("Name cannot be empty", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithEmptyName_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("john@example.com", "   ", "1234567890", "password");
        });
        assertEquals("Name cannot be empty", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithNullPassword_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("john@example.com", "John", "1234567890", null);
        });
        assertEquals("Password cannot be empty", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithEmptyPassword_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("john@example.com", "John", "1234567890", "   ");
        });
        assertEquals("Password cannot be empty", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithShortPassword_ThrowsException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("john@example.com", "John", "1234567890", "12345");
        });
        assertEquals("Password must be at least 6 characters", exception.getMessage());
    }

    @Test
    void testRegisterOwner_WithDuplicateEmail_ThrowsException() {
        // Arrange
        when(ownerRepository.existsByEmail("john@example.com")).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.registerOwner("john@example.com", "John", "1234567890", "password123");
        });
        assertEquals("Email already registered", exception.getMessage());
        
        verify(ownerRepository).existsByEmail("john@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(ownerRepository, never()).save(any(Owner.class));
    }

    // ========== getOwnerById Tests ==========

    @Test
    void testGetOwnerById_WithValidId_ReturnsOwner() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));

        // Act
        Owner result = ownerService.getOwnerById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(testOwner.getId(), result.getId());
        assertEquals(testOwner.getEmail(), result.getEmail());
        verify(ownerRepository).findById(1L);
    }

    @Test
    void testGetOwnerById_WithInvalidId_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.getOwnerById(999L);
        });
        assertEquals("Owner not found with ID: 999", exception.getMessage());
        verify(ownerRepository).findById(999L);
    }

    // ========== getOwnerByEmail Tests ==========

    @Test
    void testGetOwnerByEmail_WithValidEmail_ReturnsOwner() {
        // Arrange
        when(ownerRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testOwner));

        // Act
        Optional<Owner> result = ownerService.getOwnerByEmail("john@example.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testOwner.getEmail(), result.get().getEmail());
        verify(ownerRepository).findByEmail("john@example.com");
    }

    @Test
    void testGetOwnerByEmail_WithInvalidEmail_ReturnsEmpty() {
        // Arrange
        when(ownerRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        Optional<Owner> result = ownerService.getOwnerByEmail("nonexistent@example.com");

        // Assert
        assertFalse(result.isPresent());
        verify(ownerRepository).findByEmail("nonexistent@example.com");
    }

    // ========== updateOwnerProfile Tests ==========

    @Test
    void testUpdateOwnerProfile_WithBothNameAndPhone_UpdatesBoth() {
        // Arrange
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

        // Act
        Owner result = ownerService.updateOwnerProfile(1L, "Jane Doe", "5555555555");

        // Assert
        assertNotNull(result);
        assertEquals("Jane Doe", testOwner.getName());
        assertEquals("5555555555", testOwner.getPhoneNumber());
        verify(ownerRepository).findById(1L);
        verify(ownerRepository).save(testOwner);
    }

    @Test
    void testUpdateOwnerProfile_WithOnlyName_UpdatesNameOnly() {
        // Arrange
        String originalPhone = testOwner.getPhoneNumber();
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

        // Act
        Owner result = ownerService.updateOwnerProfile(1L, "Jane Doe", null);

        // Assert
        assertNotNull(result);
        assertEquals("Jane Doe", testOwner.getName());
        assertEquals(originalPhone, testOwner.getPhoneNumber()); // Phone unchanged
        verify(ownerRepository).findById(1L);
        verify(ownerRepository).save(testOwner);
    }

    @Test
    void testUpdateOwnerProfile_WithOnlyPhone_UpdatesPhoneOnly() {
        // Arrange
        String originalName = testOwner.getName();
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

        // Act
        Owner result = ownerService.updateOwnerProfile(1L, null, "5555555555");

        // Assert
        assertNotNull(result);
        assertEquals(originalName, testOwner.getName()); // Name unchanged
        assertEquals("5555555555", testOwner.getPhoneNumber());
        verify(ownerRepository).findById(1L);
        verify(ownerRepository).save(testOwner);
    }

    @Test
    void testUpdateOwnerProfile_WithEmptyName_DoesNotUpdateName() {
        // Arrange
        String originalName = testOwner.getName();
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

        // Act
        ownerService.updateOwnerProfile(1L, "   ", "5555555555");

        // Assert
        assertEquals(originalName, testOwner.getName()); // Name unchanged
        assertEquals("5555555555", testOwner.getPhoneNumber());
    }

    @Test
    void testUpdateOwnerProfile_WithEmptyPhone_DoesNotUpdatePhone() {
        // Arrange
        String originalPhone = testOwner.getPhoneNumber();
        when(ownerRepository.findById(1L)).thenReturn(Optional.of(testOwner));
        when(ownerRepository.save(any(Owner.class))).thenReturn(testOwner);

        // Act
        ownerService.updateOwnerProfile(1L, "Jane Doe", "   ");

        // Assert
        assertEquals("Jane Doe", testOwner.getName());
        assertEquals(originalPhone, testOwner.getPhoneNumber()); // Phone unchanged
    }

    @Test
    void testUpdateOwnerProfile_WithInvalidOwnerId_ThrowsException() {
        // Arrange
        when(ownerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            ownerService.updateOwnerProfile(999L, "Jane", "5555555555");
        });
        assertEquals("Owner not found with ID: 999", exception.getMessage());
        verify(ownerRepository).findById(999L);
        verify(ownerRepository, never()).save(any(Owner.class));
    }

    // ========== verifyPassword Tests ==========

    @Test
    void testVerifyPassword_WithValidCredentials_ReturnsTrue() {
        // Arrange
        String rawPassword = "password123";
        when(ownerRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testOwner));
        when(passwordEncoder.matches(rawPassword, testOwner.getPasswordHash())).thenReturn(true);

        // Act
        boolean result = ownerService.verifyPassword("john@example.com", rawPassword);

        // Assert
        assertTrue(result);
        verify(ownerRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches(rawPassword, testOwner.getPasswordHash());
    }

    @Test
    void testVerifyPassword_WithInvalidPassword_ReturnsFalse() {
        // Arrange
        String rawPassword = "wrongpassword";
        when(ownerRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testOwner));
        when(passwordEncoder.matches(rawPassword, testOwner.getPasswordHash())).thenReturn(false);

        // Act
        boolean result = ownerService.verifyPassword("john@example.com", rawPassword);

        // Assert
        assertFalse(result);
        verify(ownerRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches(rawPassword, testOwner.getPasswordHash());
    }

    @Test
    void testVerifyPassword_WithNonexistentEmail_ReturnsFalse() {
        // Arrange
        when(ownerRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        boolean result = ownerService.verifyPassword("nonexistent@example.com", "password");

        // Assert
        assertFalse(result);
        verify(ownerRepository).findByEmail("nonexistent@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
