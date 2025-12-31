package com.PetTrackr.PetTrackr.DTO.PetDTOs;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.PetTrackr.PetTrackr.DTO.FeedingScheduleDTOs.FeedingScheduleResponse;
import com.PetTrackr.PetTrackr.DTO.MedicationDTOs.MedicationResponse;
import com.PetTrackr.PetTrackr.DTO.VetVisitDTOs.VetVisitResponse;
import com.PetTrackr.PetTrackr.entity.Pet.ActivityLevel;
import com.PetTrackr.PetTrackr.entity.Pet.WeightType;

/**
 * DTO for returning detailed pet information in API responses.
 * Used for the individual pet profile view (UC-4) where complete information is needed.
 * Includes all pet fields plus nested collections of medications, feeding schedules, and vet visits.
 */
public class PetDetailedResponse {
    
    private Long id;
    private String name;
    private String type;
    private String breed;
    private int age; // Calculated from dateOfBirth
    private double weight;
    private WeightType weightType;
    private LocalDate dateOfBirth;
    private String photoURL; // Nullable
    private ActivityLevel activityLevel;
    
    // Nested collections for related entities
    private List<MedicationResponse> medications = new ArrayList<>();
    private List<FeedingScheduleResponse> feedingSchedules = new ArrayList<>();
    private List<VetVisitResponse> vetVisits = new ArrayList<>();

    // Constructors
    public PetDetailedResponse() {
    }

    public PetDetailedResponse(Long id, String name, String type, String breed, int age,
                               double weight, WeightType weightType, LocalDate dateOfBirth,
                               String photoURL, ActivityLevel activityLevel,
                               List<MedicationResponse> medications,
                               List<FeedingScheduleResponse> feedingSchedules,
                               List<VetVisitResponse> vetVisits) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.age = age;
        this.weight = weight;
        this.weightType = weightType;
        this.dateOfBirth = dateOfBirth;
        this.photoURL = photoURL;
        this.activityLevel = activityLevel;
        this.medications = medications != null ? medications : new ArrayList<>();
        this.feedingSchedules = feedingSchedules != null ? feedingSchedules : new ArrayList<>();
        this.vetVisits = vetVisits != null ? vetVisits : new ArrayList<>();
    }

    // Getters & Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBreed() {
        return breed;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    public WeightType getWeightType() {
        return weightType;
    }

    public void setWeightType(WeightType weightType) {
        this.weightType = weightType;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhotoURL() {
        return photoURL;
    }

    public void setPhotoURL(String photoURL) {
        this.photoURL = photoURL;
    }

    public ActivityLevel getActivityLevel() {
        return activityLevel;
    }

    public void setActivityLevel(ActivityLevel activityLevel) {
        this.activityLevel = activityLevel;
    }

    public List<MedicationResponse> getMedications() {
        return medications;
    }

    public void setMedications(List<MedicationResponse> medications) {
        this.medications = medications;
    }

    public List<FeedingScheduleResponse> getFeedingSchedules() {
        return feedingSchedules;
    }

    public void setFeedingSchedules(List<FeedingScheduleResponse> feedingSchedules) {
        this.feedingSchedules = feedingSchedules;
    }

    public List<VetVisitResponse> getVetVisits() {
        return vetVisits;
    }

    public void setVetVisits(List<VetVisitResponse> vetVisits) {
        this.vetVisits = vetVisits;
    }
}
