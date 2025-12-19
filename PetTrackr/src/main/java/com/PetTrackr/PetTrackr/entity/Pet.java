package com.PetTrackr.PetTrackr.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Entity;

@Entity
public class Pet {
    // attributes

    // primary key will be an ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long ownerId; // foreign key reference to Owner

    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String type; // e.g., Dog, Cat

    @Column(nullable = false)
    private String breed;

    @Column(nullable = false)
    private int age;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = true)
    private String photoURL; // optional photo of the pet

    @ManyToOne
    @JoinColumn(name = "ownerId", insertable = false, updatable = false, nullable = false)
    private Owner owner;

    // constructors
    public Pet() {
        // empty constructor for JPA
    }

    public Pet(Long ownerId, String name, String type, String breed, int age, LocalDate dateOfBirth, String photoURL) {
        this.ownerId = ownerId;
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

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
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
    
}