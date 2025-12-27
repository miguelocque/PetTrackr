package com.PetTrackr.PetTrackr.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.transaction.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Transactional
@Service
public class ImageUploadService {
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    // Allowed image file extensions
    private static final String[] ALLOWED_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".webp"};
    
    // Max file size: 5 MB
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    /**
     * Upload a pet image and return the filename.
     * 
     * @param petId ID of the pet (used in filename)
     * @param ownerId ID of the owner (for authorization verification)
     * @param file The image file to upload
     * @return The filename (relative path) where the image was saved
     * @throws IllegalArgumentException if file is invalid or validation fails
     * @throws IOException if file cannot be saved to disk
     */
    public String uploadPetImage(Long petId, Long ownerId, MultipartFile file) {
        // Validate file is not null
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }

        // Get original filename and extract extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("File must have a valid name");
        }

        // Extract file extension
        int dotIndex = originalFilename.lastIndexOf(".");
        if (dotIndex == -1 || dotIndex == originalFilename.length() - 1) {
            throw new IllegalArgumentException("File must have a valid extension");
        }
        String extension = originalFilename.substring(dotIndex).toLowerCase();

        // Validate file extension
        if (!isAllowedExtension(extension)) {
            throw new IllegalArgumentException("File type not allowed. Allowed types: jpg, jpeg, png, gif, webp");
        }

        // Generate unique filename to avoid collisions: petId_timestamp.extension
        String uniqueFilename = petId + "_" + System.currentTimeMillis() + extension;

        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Save file to disk
            Path filePath = uploadPath.resolve(uniqueFilename);
            Files.write(filePath, file.getBytes());

            // Return the filename (relative path)
            return uniqueFilename;

        } catch (IOException e) {
            throw new RuntimeException("Failed to save image file: " + e.getMessage(), e);
        }
    }

    /**
     * Check if the file extension is allowed.
     * 
     * @param extension The file extension (including the dot, e.g., ".jpg")
     * @return true if extension is allowed, false otherwise
     */
    private boolean isAllowedExtension(String extension) {
        for (String allowed : ALLOWED_EXTENSIONS) {
            if (allowed.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
}