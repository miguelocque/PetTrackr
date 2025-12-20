# PetTrackr Use Cases (MVP Scope)

## Project Goal
Build a full-stack pet management application demonstrating Spring Boot + React skills, with emphasis on file uploads and QR code generation as differentiating features.

---

## Actors
- **Pet Owner:** Primary user who manages their pets and related data
- **System:** Backend application handling business logic and data persistence

---

## Phase 1: Core Use Cases 

### UC-1: Register Owner Account
**Actor:** Pet Owner  
**Priority:** CRITICAL  
**Description:** Owner creates an account to access the system.

**Preconditions:**
- Owner has valid email address
- Email is not already registered

**Main Flow:**
1. Owner provides name, email, phone number, password
2. System validates input (email format, password strength)
3. System hashes password
4. System creates Owner record in database
5. System returns success confirmation

**Postconditions:**
- Owner account created
- Owner can log in

**Technical Notes:**
- Use BCrypt for password hashing
- Validate email uniqueness at database level
- Not implementing auth

**Test Scenarios:**
- Valid registration → Success
- Duplicate email → Error
- Invalid email format → Error
- Weak password → Error

---

### UC-2: Create Pet Profile
**Actor:** Pet Owner  
**Priority:** CRITICAL  
**Description:** Owner adds a new pet to their account.

**Preconditions:**
- Owner is logged in

**Main Flow:**
1. Owner provides: name, type, breed, age, weight, weightType, dateOfBirth, activityLevel
2. System validates input (age > 0, weight > 0, valid date)
3. System creates Pet record linked to Owner
4. System returns Pet object with generated ID

**Postconditions:**
- Pet is created and associated with Owner
- Pet appears in owner's dashboard

**Technical Notes:**
- Owner ID comes from authenticated session
- Use `@Valid` annotation for input validation
- Weight type is enum (KG or LBS)
- Activity level is string (Low/Medium/High)

**Test Scenarios:**
- Valid pet creation → Success
- Missing required fields → Error
- Invalid age (negative) → Error
- Invalid weight (zero/negative) → Error

---

### UC-3: View All Owner's Pets (Dashboard)
**Actor:** Pet Owner  
**Priority:** CRITICAL  
**Description:** Owner sees a list of all their pets.

**Preconditions:**
- Owner is logged in
- Owner has at least one pet

**Main Flow:**
1. Owner requests dashboard view
2. System retrieves all Pets for logged-in Owner
3. System returns list of Pet objects (includes photo URLs if available)
4. Frontend displays pet cards in grid layout (3 columns)

**Postconditions:**
- Owner sees all their pets

**Technical Notes:**
- Use `findByOwnerId()` repository method
- Return DTOs (not full entities) to avoid circular references
- Include basic stats: age, weight, activity level
- Show placeholder image if no photo uploaded

**Test Scenarios:**
- Owner with 3 pets → Returns 3 pets
- Owner with 0 pets → Returns empty list
- Unauthorized access → Error 401

---

### UC-4: View Pet Details (Profile Page)
**Actor:** Pet Owner  
**Priority:** CRITICAL  
**Description:** Owner views detailed information for a specific pet.

**Preconditions:**
- Owner is logged in
- Pet belongs to owner

**Main Flow:**
1. Owner selects a pet from dashboard
2. System retrieves Pet by ID
3. System verifies Pet belongs to Owner
4. System retrieves related data:
   - Medications (if any)
   - Feeding schedules (if any)
   - Vet visits (if any)
5. System returns complete Pet profile

**Postconditions:**
- Owner sees full pet details
- Can navigate to edit/delete actions

**Technical Notes:**
- Use `@JsonManagedReference` / `@JsonBackReference` to avoid circular JSON
- Lazy load relationships (don't fetch all data by default)
- Authorization check: verify `pet.getOwner().getId() == currentUser.getId()`

**Test Scenarios:**
- Valid pet ID + owner match → Success
- Pet ID not found → Error 404
- Pet belongs to different owner → Error 403

---

### UC-5: Upload Pet Image 
**Actor:** Pet Owner  
**Priority:** HIGH 
**Description:** Owner uploads a photo for their pet.

**Preconditions:**
- Owner is logged in
- Pet exists and belongs to owner

**Main Flow:**
1. Owner selects image file (JPG or PNG)
2. Frontend validates file size (< 5MB) and type
3. Frontend sends multipart form data to backend
4. System validates file again (backend validation)
5. System generates unique filename (e.g., `pet_{id}_{timestamp}.jpg`)
6. System saves file to `/uploads` directory
7. System updates Pet's `photoUrl` field with file path
8. System returns updated Pet object

**Postconditions:**
- Image stored in file system
- Pet's `photoUrl` field updated
- Image displayed on dashboard and profile page

**Technical Notes:**
- Use `MultipartFile` in Spring controller
- Store files in `./uploads` directory (create if not exists)
- Validate MIME type on backend (JPG/PNG only)
- Max file size: 5MB
- Return error if upload fails (disk full, permissions)

**Error Handling:**
- File too large → Error 413
- Invalid file type → Error 400
- I/O error → Error 500

**Test Scenarios:**
- Valid JPG upload → Success
- Valid PNG upload → Success
- File > 5MB → Error
- PDF file → Error
- Corrupted image → Error

**Resume Bullet:**
> "Engineered secure multipart file upload for pet images with format/size validation, handling JPG/PNG files up to 5MB with error handling"

---

### UC-6: Generate QR Code for Pet
**Actor:** Pet Owner  
**Priority:** HIGH 
**Description:** Owner generates a QR code containing pet emergency contact info.

**Preconditions:**
- Owner is logged in
- Pet exists and belongs to owner

**Main Flow:**
1. Owner clicks "Generate QR Code" button on pet profile
2. System retrieves Pet and Owner data
3. System creates QR payload:
   ```
   Pet: {pet.name}
   Owner: {owner.name}
   Phone: {owner.phoneNumber}
   Pet ID: {pet.id}
   ```
4. System uses ZXing library to generate QR code image (PNG, 300x300px)
5. System returns QR code as downloadable image

**Postconditions:**
- QR code generated (not stored in DB - generated on-the-fly)
- Owner can download/print QR code

**Technical Notes:**
- Use ZXing `QRCodeWriter` class
- Generate 300x300px PNG image
- Encode pet ID + owner name + phone number
- Return as `ResponseEntity<byte[]>` with `image/png` content type
- No database storage needed (regenerate on each request)

**Alternative Flow:**
- QR code can link to public URL: `https://yourapp.com/pet/{id}/public`
- Public page shows read-only pet info (name, breed, owner contact)
- Requires separate public endpoint (optional enhancement)

**Test Scenarios:**
- Valid pet ID → QR code generated
- Invalid pet ID → Error 404
- Pet belongs to different owner → Error 403
- QR code scannable by any QR reader app → Success

**Resume Bullet:**
> "Implemented QR code generation using ZXing library for pet identification, enabling downloadable emergency contact codes for pet safety scenarios"

---

### UC-7: Add Medication to Pet
**Actor:** Pet Owner  
**Priority:** MEDIUM  
**Description:** Owner adds a medication schedule for their pet.

**Main Flow:**
1. Owner provides: name, dosage, frequency, timeToAdminister, startDate, endDate (optional)
2. System creates Medication linked to Pet
3. System returns success confirmation

**Postconditions:**
- Medication appears on pet profile

---

### UC-8: View Pet Medications
**Actor:** Pet Owner  
**Priority:** MEDIUM  
**Description:** Owner views all medications for a pet.

**Main Flow:**
1. Owner views pet profile
2. System retrieves all Medications for pet
3. System displays medications with schedules

**Postconditions:**
- Owner sees current and past medications

---

### UC-9: Add Vet Visit
**Actor:** Pet Owner  
**Priority:** MEDIUM  
**Description:** Owner records a veterinary visit.

**Main Flow:**
1. Owner provides: visitDate, vetName, reason, notes, nextVisitDate (optional)
2. System creates VetVisit linked to Pet
3. System returns success confirmation

**Postconditions:**
- Vet visit appears on pet profile

---

### UC-10: View Vet Visit History
**Actor:** Pet Owner  
**Priority:** MEDIUM  
**Description:** Owner views all vet visits for a pet.

**Main Flow:**
1. Owner views pet profile
2. System retrieves all VetVisits for pet
3. System displays visits in chronological order

**Postconditions:**
- Owner sees complete vet history

---

### UC-11: Add Feeding Schedule
**Actor:** Pet Owner  
**Priority:** MEDIUM  
**Description:** Owner creates a feeding schedule for their pet.

**Main Flow:**
1. Owner provides: time, foodType, quantity, quantityUnit
2. System creates FeedingSchedule linked to Pet
3. System returns success confirmation

**Postconditions:**
- Feeding schedule appears on pet profile

---

### UC-12: View Feeding Schedules
**Actor:** Pet Owner  
**Priority:** MEDIUM  
**Description:** Owner views all feeding times for a pet.

**Main Flow:**
1. Owner views pet profile
2. System retrieves all FeedingSchedules for pet
3. System displays feeding times

**Postconditions:**
- Owner sees daily feeding plan

---

## Use Cases EXCLUDED from MVP

### Generate Daily Dashboard
**Reason:** Requires complex aggregation logic across multiple entities. Not critical for demonstrating CRUD + file handling skills.

### Log Medication Given
**Reason:** Adds MedicationLog entity + compliance tracking logic. Schedules are sufficient for MVP.

### Log Feeding Event
**Reason:** Adds FeedingLog entity + time-series analysis. Schedules are sufficient for MVP.

### Generate Lost Pet Public Page
**Reason:** Requires public-facing routes + different authorization logic. QR code generation alone is sufficient.

---

## Development Priority Order

**Week 1 (Dec 19-23): Backend Foundation**
1. Fix entity relationships (Pet, Owner, Medication, FeedingSchedule, VetVisit)
2. Configure H2 database (file-based, `ddl-auto=update`)
3. Create all Repository interfaces
4. Create DataSeeder for test data
5. Write Service layer (OwnerService, PetService)

**Week 2 (Dec 24-30): Backend Features**
6. Implement UC-1, UC-4, UC-5, UC-6 (CRUD operations)
7. Implement UC-9 (Image upload) 
8. Implement UC-10 (QR code generation)
9. Write unit tests for services (JUnit + Mockito)

**Week 3 (Dec 31-Jan 6): Frontend + Integration**
10. Build React components (Dashboard, PetProfile, PetForm)
11. Connect frontend to backend APIs (Axios)
12. Test image upload flow end-to-end
13. Test QR code generation flow end-to-end

**Final Days (Jan 7-10): Polish**
14. Write comprehensive README with screenshots
15. Calculate metrics (endpoints, test coverage, LOC)

---

## Success Criteria

**Main Implementation:**
- All core use cases fully implemented
- Backend + Frontend integrated and working
- Image upload functional with file validation
- QR code generation functional with downloadable output

**Final Touches:**
- Medications and feeding schedules implemented (if time permits)
- 70%+ test coverage on service layer
- README with screenshots and setup instructions

---

## Testing Strategy

**Unit Tests (JUnit + Mockito):**
- Service layer methods
- Business logic validation
- Exception handling

**Integration Tests:**
- Repository queries
- Controller endpoints (MockMvc)
- File upload workflow
- QR code generation

**Manual Testing:**
- Full user flow (register → create pet → upload image → generate QR)