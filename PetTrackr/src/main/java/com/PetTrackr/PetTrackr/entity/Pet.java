package com.PetTrackr.PetTrackr.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.CascadeType;

@Entity
public class Pet {
    // weightType enum  
    public enum WeightType {
        KG,
        LBS
    }

    // attributes

    // primary key will be an ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String type; // e.g., Dog, Cat

    @Column(nullable = false)
    private String breed;

    @Column(nullable = false)
    private int age;

    // weight
    @Column(nullable = false)
    private double weight;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private WeightType weightType;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = true)
    private String photoURL; // optional photo of the pet

    // medication - list of medications - can be null if no medications
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Medication> medications = new ArrayList<>();

    // feeding schedule - list of feeding times
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeedingSchedule> feedingSchedule = new ArrayList<>();

    // vet appointments (list of appointments)
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VetVisit> vetAppointments = new ArrayList<>();

    // reference to Owner - many-to-one relationship
    @ManyToOne
    @JoinColumn(name = "ownerId", nullable = false)
    private Owner owner;

    // constructors
    public Pet() {
        // empty constructor for JPA
    }

    public Pet(Owner owner, String name, String type, String breed, int age, LocalDate dateOfBirth, String photoURL) {
        this.owner = owner;
        this.name = name;
        this.type = type;
        this.breed = breed;
        this.age = age;
        this.dateOfBirth = dateOfBirth;
        this.photoURL = photoURL;
    }

    // getters and setters
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

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
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

    public List<Medication> getMedications() {
        return medications;
    }

    public void setMedications(List<Medication> medications) {
        this.medications = medications;
    }

    public List<FeedingSchedule> getFeedingSchedule() {
        return feedingSchedule;
    }

    public void setFeedingSchedule(List<FeedingSchedule> feedingSchedule) {
        this.feedingSchedule = feedingSchedule;
    }

    public List<VetVisit> getVetAppointments() {
        return vetAppointments;
    }

    public void setVetAppointments(List<VetVisit> vetAppointments) {
        this.vetAppointments = vetAppointments;
    }
}