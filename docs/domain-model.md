# PetTrackr Domain Model

## Entities

### Owner
- id (Long, PK)  
- name (String)  
- email (String)  
- pets (List[Pet])
- Relationships: One-to-many → Pets  
- Mutable: Yes

### Pet
- id, name, species, birthDate, photoUrl  
- Relationships: Many-to-one → Owner; One-to-many → VetVisits, MedicationAssignments, FeedingSchedules; One-to-one → LostPetFlyer  
- Mutable: Yes

### VetVisit
- id, pet, vetName, date, reason, notes  
- Relationships: Many-to-one → Pet  
- Immutable

### MedicationAssignment
- id, pet, name, dosage, startDate, endDate  
- Relationships: Many-to-one → Pet  
- Mutable: Yes (history preserved)

### FeedingSchedule
- id, pet, time, foodType, notes  
- Relationships: Many-to-one → Pet; One-to-many → FeedingLogs  
- Mutable

### FeedingLog
- id, feedingSchedule, actualTime, status (COMPLETED, SKIPPED, LATE)  
- Relationships: Many-to-one → FeedingSchedule  
- Immutable

### LostPetFlyer
- id, pet, qrCodeUrl  
- Relationships: One-to-one → Pet  
- Read-only public info

## Relationships Summary
- Owner → Pets: 1:N  
- Pet → VetVisits: 1:N  
- Pet → MedicationAssignments: 1:N  
- Pet → FeedingSchedules: 1:N  
- FeedingSchedule → FeedingLogs: 1:N  
- Pet → LostPetFlyer: 1:1
