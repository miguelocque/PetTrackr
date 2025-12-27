package com.PetTrackr.PetTrackr.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @InjectMocks
    private ImageUploadService imageUploadService;

    @TempDir
    private Path tempDir;

    @BeforeEach
    void setUp() {
        // Inject the temp directory as the upload directory for testing
        ReflectionTestUtils.setField(imageUploadService, "uploadDir", tempDir.toString());
    }

    // ========================================
    // Successful Upload Tests
    // ========================================

    @Test
    void testUploadPetImage_WithValidJpgFile_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", "jpg content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(1L, 1L, file);

        // Assert
        assertNotNull(filename);
        assertTrue(filename.startsWith("1_"));
        assertTrue(filename.endsWith(".jpg"));
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void testUploadPetImage_WithValidPngFile_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.png", "image/png", "png content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(2L, 1L, file);

        // Assert
        assertNotNull(filename);
        assertTrue(filename.startsWith("2_"));
        assertTrue(filename.endsWith(".png"));
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void testUploadPetImage_WithValidGifFile_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.gif", "image/gif", "gif content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(3L, 1L, file);

        // Assert
        assertTrue(filename.endsWith(".gif"));
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void testUploadPetImage_WithValidWebpFile_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.webp", "image/webp", "webp content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(4L, 1L, file);

        // Assert
        assertTrue(filename.endsWith(".webp"));
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void testUploadPetImage_WithValidJpegFile_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.jpeg", "image/jpeg", "jpeg content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(5L, 1L, file);

        // Assert
        assertTrue(filename.endsWith(".jpeg"));
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void testUploadPetImage_GeneratesUniqueFilename() throws InterruptedException {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", "content1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", "content2".getBytes()
        );

        // Act
        String filename1 = imageUploadService.uploadPetImage(1L, 1L, file1);
        Thread.sleep(10); // Ensure different timestamp
        String filename2 = imageUploadService.uploadPetImage(1L, 1L, file2);

        // Assert
        assertNotEquals(filename1, filename2);
        assertTrue(Files.exists(tempDir.resolve(filename1)));
        assertTrue(Files.exists(tempDir.resolve(filename2)));
    }

    @Test
    void testUploadPetImage_FileContentSavedCorrectly() throws IOException {
        // Arrange
        byte[] fileContent = "test image content".getBytes();
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", fileContent
        );

        // Act
        String filename = imageUploadService.uploadPetImage(1L, 1L, file);

        // Assert
        byte[] savedContent = Files.readAllBytes(tempDir.resolve(filename));
        assertArrayEquals(fileContent, savedContent);
    }

    @Test
    void testUploadPetImage_CreatesDirectoryIfNotExists() {
        // Arrange - Use a non-existent directory path
        Path newDir = tempDir.resolve("subdir");
        ReflectionTestUtils.setField(imageUploadService, "uploadDir", newDir.toString());
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", "content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(1L, 1L, file);

        // Assert
        assertTrue(Files.exists(newDir));
        assertTrue(Files.exists(newDir.resolve(filename)));
    }

    @Test
    void testUploadPetImage_WithUppercaseExtension_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.JPG", "image/jpeg", "content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(1L, 1L, file);

        // Assert
        assertNotNull(filename);
        assertTrue(filename.endsWith(".jpg"));
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void testUploadPetImage_WithMixedCaseExtension_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.PnG", "image/png", "content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(2L, 1L, file);

        // Assert
        assertTrue(filename.endsWith(".png"));
    }

    @Test
    void testUploadPetImage_WithDifferentPetIds_IncludesIdInFilename() {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile("file", "p1.jpg", "image/jpeg", "c1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "p2.jpg", "image/jpeg", "c2".getBytes());

        // Act
        String filename1 = imageUploadService.uploadPetImage(100L, 1L, file1);
        String filename2 = imageUploadService.uploadPetImage(200L, 1L, file2);

        // Assert
        assertTrue(filename1.startsWith("100_"));
        assertTrue(filename2.startsWith("200_"));
    }

    // ========================================
    // Null/Empty File Validation Tests
    // ========================================

    @Test
    void testUploadPetImage_WithNullFile_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.uploadPetImage(1L, 1L, null);
        });
    }

    @Test
    void testUploadPetImage_WithEmptyFile_ThrowsException() {
        // Arrange
        MockMultipartFile emptyFile = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", new byte[0]
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.uploadPetImage(1L, 1L, emptyFile);
        });
        assertTrue(exception.getMessage().contains("cannot be empty"));
    }

    // ========================================
    // File Size Validation Tests
    // ========================================

    @Test
    void testUploadPetImage_WithFileExceedingMaxSize_ThrowsException() {
        // Arrange - Create a file larger than 5MB
        byte[] largeContent = new byte[5 * 1024 * 1024 + 1];
        MockMultipartFile largeFile = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", largeContent
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.uploadPetImage(1L, 1L, largeFile);
        });
        assertTrue(exception.getMessage().contains("exceeds maximum allowed size"));
    }

    @Test
    void testUploadPetImage_WithMaxAllowedSize_Success() {
        // Arrange - Create a file exactly 5MB
        byte[] maxSizeContent = new byte[5 * 1024 * 1024];
        MockMultipartFile maxFile = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", maxSizeContent
        );

        // Act
        String filename = imageUploadService.uploadPetImage(1L, 1L, maxFile);

        // Assert
        assertNotNull(filename);
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void testUploadPetImage_WithSmallFile_Success() {
        // Arrange - Create a small file (1 byte)
        MockMultipartFile smallFile = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", new byte[]{1}
        );

        // Act
        String filename = imageUploadService.uploadPetImage(1L, 1L, smallFile);

        // Assert
        assertNotNull(filename);
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    // ========================================
    // File Extension Validation Tests
    // ========================================

    @Test
    void testUploadPetImage_WithInvalidExtension_ThrowsException() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file", "photo.pdf", "application/pdf", "content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.uploadPetImage(1L, 1L, invalidFile);
        });
        assertTrue(exception.getMessage().contains("not allowed"));
    }

    @Test
    void testUploadPetImage_WithExeExtension_ThrowsException() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file", "malware.exe", "application/x-msdownload", "content".getBytes()
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.uploadPetImage(1L, 1L, invalidFile);
        });
    }

    @Test
    void testUploadPetImage_WithTxtExtension_ThrowsException() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file", "notes.txt", "text/plain", "content".getBytes()
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.uploadPetImage(1L, 1L, invalidFile);
        });
    }

    @Test
    void testUploadPetImage_WithSvgExtension_ThrowsException() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file", "image.svg", "image/svg+xml", "content".getBytes()
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.uploadPetImage(1L, 1L, invalidFile);
        });
    }

    @Test
    void testUploadPetImage_WithBmpExtension_ThrowsException() {
        // Arrange
        MockMultipartFile invalidFile = new MockMultipartFile(
            "file", "image.bmp", "image/bmp", "content".getBytes()
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.uploadPetImage(1L, 1L, invalidFile);
        });
    }

    // ========================================
    // Filename Validation Tests
    // ========================================

    @Test
    void testUploadPetImage_WithNullOriginalFilename_ThrowsException() {
        // Arrange
        MockMultipartFile fileWithNullName = new MockMultipartFile(
            "file", null, "image/jpeg", "content".getBytes()
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.uploadPetImage(1L, 1L, fileWithNullName);
        });
        assertTrue(exception.getMessage().contains("valid name"));
    }

    @Test
    void testUploadPetImage_WithEmptyFilename_ThrowsException() {
        // Arrange
        MockMultipartFile fileWithEmptyName = new MockMultipartFile(
            "file", "", "image/jpeg", "content".getBytes()
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.uploadPetImage(1L, 1L, fileWithEmptyName);
        });
    }

    @Test
    void testUploadPetImage_WithFilenameNoExtension_ThrowsException() {
        // Arrange
        MockMultipartFile fileNoExtension = new MockMultipartFile(
            "file", "photofile", "image/jpeg", "content".getBytes()
        );

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            imageUploadService.uploadPetImage(1L, 1L, fileNoExtension);
        });
    }

    @Test
    void testUploadPetImage_WithMultipleExtensions_UsesLastExtension() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.jpg.png", "image/png", "content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(1L, 1L, file);

        // Assert
        assertTrue(filename.endsWith(".png"));
    }

    @Test
    void testUploadPetImage_WithDotInFilename_ExtractsCorrectExtension() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "my.photo.file.jpg", "image/jpeg", "content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(1L, 1L, file);

        // Assert
        assertTrue(filename.endsWith(".jpg"));
    }

    // ========================================
    // Edge Cases and Special Scenarios
    // ========================================

    @Test
    void testUploadPetImage_WithMultipleUploadsForSamePet_AllSucceed() {
        // Arrange
        MockMultipartFile file1 = new MockMultipartFile("file", "p1.jpg", "image/jpeg", "c1".getBytes());
        MockMultipartFile file2 = new MockMultipartFile("file", "p2.png", "image/png", "c2".getBytes());
        MockMultipartFile file3 = new MockMultipartFile("file", "p3.gif", "image/gif", "c3".getBytes());

        // Act
        String filename1 = imageUploadService.uploadPetImage(1L, 1L, file1);
        String filename2 = imageUploadService.uploadPetImage(1L, 1L, file2);
        String filename3 = imageUploadService.uploadPetImage(1L, 1L, file3);

        // Assert
        assertTrue(Files.exists(tempDir.resolve(filename1)));
        assertTrue(Files.exists(tempDir.resolve(filename2)));
        assertTrue(Files.exists(tempDir.resolve(filename3)));
        assertNotEquals(filename1, filename2);
        assertNotEquals(filename2, filename3);
    }

    @Test
    void testUploadPetImage_WithLongOwnerId_Success() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", "content".getBytes()
        );
        Long largeOwnerId = Long.MAX_VALUE;

        // Act
        String filename = imageUploadService.uploadPetImage(1L, largeOwnerId, file);

        // Assert
        assertNotNull(filename);
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void testUploadPetImage_WithLongPetId_IncludedInFilename() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", "content".getBytes()
        );
        Long largePetId = 999999999L;

        // Act
        String filename = imageUploadService.uploadPetImage(largePetId, 1L, file);

        // Assert
        assertTrue(filename.startsWith("999999999_"));
    }

    @Test
    void testUploadPetImage_FilenameBeginsWithPetId() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", "content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(42L, 1L, file);

        // Assert
        assertTrue(filename.startsWith("42_"));
        String[] parts = filename.split("_");
        assertEquals(2, parts.length);
        assertEquals("42", parts[0]);
    }

    @Test
    void testUploadPetImage_FilenameHasTimestampPortion() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", "content".getBytes()
        );

        // Act
        String filename = imageUploadService.uploadPetImage(1L, 1L, file);

        // Assert
        String[] parts = filename.split("_");
        assertEquals("1", parts[0]);
        // Middle part should be a number (timestamp)
        assertTrue(parts[1].matches("\\d+\\.jpg"));
    }
}
