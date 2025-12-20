# PetTrackr Domain Model (MVP Scope)

## Core Philosophy
Focus on essential pet management features with clear relationships. Prioritize CRUD operations, file uploads, and QR code generation over complex logging/reporting.

---

## Entities

### Owner
**Purpose:** Represents a pet owner account  
**Attributes:**
- `id` (Long, PK, auto-generated)
- `name` (String, not null)
- `email` (String, not null, unique)
- `phoneNumber` (String, not null)
- `passwordHash` (String, not null)

**Relationships:**
- One-to-Many → `Pet` (an owner can have multiple pets)

**Mutability:** Yes (owner can update profile)

---

### Pet
**Purpose:** Represents a single pet with basic profile information  
**Attributes:**
- `id` (Long, PK, auto-generated)
- `name` (String, not null)
- `type` (String, not null) - e.g., "Dog", "Cat", "Bird"
- `breed` (String, not null)
- `age` (int, not null)
- `weight` (double, not null)
- `weightType` (Enum: KG or LBS, not null)
- `dateOfBirth` (LocalDate, not null)
- `photoUrl` (String, nullable) - path to uploaded image
- `activityLevel` (String, not null) - e.g., "Low", "Medium", "High"

**Relationships:**
- Many-to-One → `Owner` (each pet belongs to one owner)
- One-to-Many → `Medication` (optional, a pet can have multiple medications)
- One-to-Many → `FeedingSchedule` (optional, a pet can have multiple feeding times)
- One-to-Many → `VetVisit` (optional, a pet can have multiple vet visit records)

**Mutability:** Yes (pet profile can be updated)

**Notes:**
- `photoUrl` will store the file path after image upload (UC-9)
- QR code generation (UC-10) will encode pet ID + owner contact info

---

### Medication
**Purpose:** Tracks medications assigned to a pet  
**Attributes:**
- `id` (Long, PK, auto-generated)
- `name` (String, not null) - e.g., "Heartgard"
- `dosage` (String, not null) - e.g., "10mg"
- `frequency` (String, not null) - e.g., "Daily", "Twice a day"
- `timeToAdminister` (LocalTime, not null) - e.g., "08:00"
- `startDate` (LocalDate, not null)
- `endDate` (LocalDate, nullable) - null = ongoing

**Relationships:**
- Many-to-One → `Pet` (each medication belongs to one pet)

**Mutability:** Yes (can update dosage, end date, etc.)

**Notes:**
- No logging of "did we give it today?" - just tracks the schedule
- Future enhancement: Add `MedicationLog` for compliance tracking

---

### FeedingSchedule
**Purpose:** Defines when and what to feed a pet  
**Attributes:**
- `id` (Long, PK, auto-generated)
- `time` (String, not null) - e.g., "08:00 AM"
- `foodType` (String, not null) - e.g., "Dry Kibble", "Wet Food"
- `quantity` (double, not null)
- `quantityUnit` (Enum: CUPS, GRAMS, OUNCES, not null)

**Relationships:**
- Many-to-One → `Pet` (each schedule belongs to one pet)

**Mutability:** Yes (can change feeding times/amounts)

**Notes:**
- No logging of actual feedings - just tracks the plan
- Future enhancement: Add `FeedingLog` for tracking compliance

---

### VetVisit
**Purpose:** Records veterinary appointments and visits  
**Attributes:**
- `id` (Long, PK, auto-generated)
- `visitDate` (LocalDate, not null)
- `nextVisitDate` (LocalDate, nullable)
- `vetName` (String, not null)
- `reason` (String, not null) - e.g., "Annual checkup", "Vaccination"
- `notes` (String, nullable) - additional details

**Relationships:**
- Many-to-One → `Pet` (each visit belongs to one pet)

**Mutability:** Yes (can update notes, next visit date)

**Notes:**
- Simplified from original design (no immutability requirement)
- Focus on last visit + next scheduled visit for dashboard

---

## Relationship Summary

```
Owner (1) ──────< Pet (Many)
                   │
                   ├──────< Medication (Many)
                   │
                   ├──────< FeedingSchedule (Many)
                   │
                   └──────< VetVisit (Many)
```

**Key Points:**
- All relationships are bidirectional (child entities know their parent)
- Cascade operations: Deleting a Pet deletes all its Medications, FeedingSchedules, and VetVisits
- Deleting an Owner deletes all their Pets (and cascades down)

---

## MVP Scope Boundaries

### INCLUDED (Phase 1 - Must Have)
- Owner registration and authentication
- Pet CRUD (Create, Read, Update, Delete)
- Pet image upload (multipart file handling)
- QR code generation for pet ID
- Basic medication tracking (schedules only)
- Basic feeding schedule
- Vet visit tracking

### EXCLUDED (Future Enhancements)
- Medication compliance logging (did we give it today?)
- Feeding compliance logging (did we feed on time?)
- Daily dashboard aggregation
- Advanced reporting/analytics
- Lost pet flyer public pages
- Search/filter functionality

### OPTIONAL (Phase 2 - If Time Permits)
- Allergy tracking (separate entity)
- Activity level recommendations
- Weight tracking over time
- Medication reminders/notifications

---

## Design Decisions

**Why no logs?**
- Logs require time-series data and reporting logic
- Adds 2 more entities + repositories + services
- Not critical for demonstrating full-stack skills
- Can be mentioned as "future enhancement" in README

**Why simple vet visits?**
- Original design had "immutable" visits, but that's overkill for MVP
- Focus on showing data persistence, not complex business rules
- Simpler to test and demo

**Why keep feeding schedules?**
- Matches Figma design sidebar
- Easy to implement (just time + description)
- Shows relationship mapping skills
- Low complexity, high visual impact

**File storage strategy:**
- Store uploaded images in `/uploads` directory
- Save file path in `photoUrl` field
- Return image via REST endpoint when needed
- No cloud storage (local file system is fine for demo)

**QR code strategy:**
- Generate QR using ZXing library
- Encode: `pet.id + owner.name + owner.phoneNumber`
- Return as PNG image via REST endpoint
- No database storage needed (generate on-the-fly)