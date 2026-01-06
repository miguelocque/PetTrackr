package com.PetTrackr.PetTrackr.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;

import org.springframework.stereotype.Service;

import com.PetTrackr.PetTrackr.entity.Pet;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.qrcode.QRCodeWriter;
import com.PetTrackr.PetTrackr.entity.Owner;

import jakarta.transaction.Transactional;

@Transactional
@Service
public class QRCodeService {
    // we want to generate QR codes that contain text data about the pet
    // e.g., pet name, owner contact info, etc.
    // and a guide to what to do if the pet is found

    // used to verify pet existence
    private final PetService petService;

    // QR code dimensions
    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    // constructor
    public QRCodeService(PetService petService) {
        this.petService = petService;
    }

    // main method to generate QR code data for a pet
    public byte[] generateQRCodeForPet(Long petId, Long requestingOwnerId) {
        // firstly verify pet existence and ownership
        Pet pet = petService.getPetById(petId, requestingOwnerId);

        // get the owner info
        Owner owner = pet.getOwner();

        // build the QR code content
        String qrCodeContent = buildQRCodeContent(pet, owner);

        // generate QR code image bytes
        try {
            // use google zxing to generate QR code as a bit matrix, which is a 2D array of bits
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeContent, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT);

            // convert bit matrix to byte array (PNG format)
            // uses a byte array output stream to hold the image data
            // and saves the bit matrix as a PNG image into that stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);

            // return the byte array
            return outputStream.toByteArray();

        }
        catch (WriterException | IOException e) {
            // handles a writer exception which is thrown if QR code generation fails
            // and an IO exception which is thrown if writing to the output stream fails
            throw new RuntimeException("Failed to generate QR code: " + e.getMessage(), e);
        }


    }

    // helper method to create QR code image bytes from text data
    /**
     * Format:
     * LOST PET - PLEASE HELP!
     * Pet: [name]
     * Type: [type]
     * Breed: [breed]
     * 
     * CONTACT OWNER:
     * Name: [owner name]
     * Phone: [phone]
     * 
     * FOUND THIS PET?
     * Step-by-step guide:
     * [guidance URL]
     */
    private String buildQRCodeContent(Pet pet, Owner owner) {
        // we use a string builder for efficiency
        StringBuilder content = new StringBuilder();

        // header
        content.append("LOST PET - PLEASE HELP!\n");

        // pet details
        content.append("Pet: ").append(pet.getName()).append("\n");
        content.append("Type: ").append(pet.getType()).append("\n");
        content.append("Breed: ").append(pet.getBreed()).append("\n\n");

        // owner contact info
        content.append("CONTACT OWNER:\n");
        content.append("Name: ").append(owner.getName()).append("\n");
        content.append("Phone: ").append(owner.getPhoneNumber()).append("\n\n");

        // guidance
        content.append("FOUND THIS PET?\n");
        content.append("Visit this guide:\n");
        content.append("https://www.americanhumane.org/public-education/what-to-if-youve-lost-your-pet/\n");

        // and return the built string
        return content.toString();
    }
}