package com.PetTrackr.PetTrackr.repository;

import com.PetTrackr.PetTrackr.entity.Owner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(statements = {"DELETE FROM feeding_schedule", "DELETE FROM medication", "DELETE FROM vet_visit", "DELETE FROM pet", "DELETE FROM owner"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
public class OwnerRepositoryTest {

    @Autowired
    private OwnerRepository ownerRepository;

    @Test
    void testFindByEmail_WithValidEmail_ReturnsOwner() {
        // Arrange
        Owner owner = new Owner();
        owner.setName("John Doe");
        owner.setEmail("john@example.com");
        owner.setPhoneNumber("555-1234");
        owner.setPasswordHash("hashedpassword");
        ownerRepository.save(owner);

        // Act
        Optional<Owner> found = ownerRepository.findByEmail("john@example.com");

        // Assert
        assertTrue(found.isPresent());
        assertEquals("John Doe", found.get().getName());
        assertEquals("john@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail_WithInvalidEmail_ReturnsEmpty() {
        // Act
        Optional<Owner> found = ownerRepository.findByEmail("nonexistent@example.com");

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testFindById_WithValidId_ReturnsOwner() {
        // Arrange
        Owner owner = new Owner();
        owner.setName("Jane Smith");
        owner.setEmail("jane@example.com");
        owner.setPhoneNumber("555-5678");
        owner.setPasswordHash("hashedpassword");
        owner = ownerRepository.save(owner);

        // Act
        Optional<Owner> found = ownerRepository.findById(owner.getId());

        // Assert
        assertTrue(found.isPresent());
        assertEquals("Jane Smith", found.get().getName());
    }

    @Test
    void testFindById_WithInvalidId_ReturnsEmpty() {
        // Act
        Optional<Owner> found = ownerRepository.findById(999L);

        // Assert
        assertFalse(found.isPresent());
    }

    @Test
    void testSave_WithValidOwner_PersistsSuccessfully() {
        // Arrange
        Owner owner = new Owner();
        owner.setName("Bob Wilson");
        owner.setEmail("bob@example.com");
        owner.setPhoneNumber("555-9999");
        owner.setPasswordHash("hashedpassword");

        // Act
        Owner saved = ownerRepository.save(owner);

        // Assert
        assertNotNull(saved.getId());
        assertEquals("Bob Wilson", saved.getName());
        assertEquals("bob@example.com", saved.getEmail());
    }
}
