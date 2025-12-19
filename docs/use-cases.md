# PetTrackr Use Cases

PetTrackr is a Spring Boot application for managing pets, their medical history, medications, feeding schedules, and lost-pet recovery via QR codes.

## Actors
- **Pet Owner:** Manages pets, schedules, and health records
- **Vet:** Optional, associated with vet visits
- **Public User:** Scans lost pet QR codes

## Use Cases

### UC1: Register Owner and Pets
**Actor:** Pet Owner  
**Description:** Owner creates an account and registers one or more pets.  
**Outcome:** Owner can manage multiple pets.

### UC2: Record Vet Visit (Immutable)
**Actor:** Pet Owner / Vet  
**Description:** Logs vet visits for pets; cannot be modified.  
**Outcome:** Immutable vet history.

### UC3: Assign / Update Medication
**Actor:** Pet Owner  
**Description:** Assigns new medications, ending overlapping previous assignments.  
**Outcome:** Current medications tracked; past history preserved.

### UC4: Manage Feeding Schedule
**Actor:** Pet Owner  
**Description:** Creates feeding schedules for pets.  
**Outcome:** Schedules per pet can be viewed and updated.

### UC5: Log Feeding Events
**Actor:** Pet Owner  
**Description:** Records actual feedings (completed, skipped, late).  
**Outcome:** Immutable feeding logs stored for history.

### UC6: Generate Daily Dashboard
**Actor:** Pet Owner  
**Description:** Aggregates all pets’ schedules, medications, and vet visits for the day.  
**Outcome:** Consolidated daily view.

### UC7: Generate Lost Pet QR Flyer
**Actor:** Pet Owner / Public User  
**Description:** Generates QR codes linking to read-only pet info.  
**Outcome:** Public can access safe info for lost pets.

### UC8: Search / Filter Pets
**Actor:** Pet Owner  
**Description:** Search/filter pets by name, species, or tags.  
**Outcome:** Quick access to relevant pet info.

---

**Notes**
- Mutability: VetVisits and FeedingLogs are immutable; others are mutable.
- Data privacy: QR view only exposes minimal public info.
- Extensibility: Multi-pet support per owner.
