package com.PetTrackr.PetTrackr.seeder;

import com.PetTrackr.PetTrackr.entity.*;
import com.PetTrackr.PetTrackr.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
@Profile("!prod") // Only run in non-production environments
public class DataSeeder implements CommandLineRunner {

    private final OwnerRepository ownerRepository;
    private final PetRepository petRepository;
    private final MedicationRepository medicationRepository;
    private final VetVisitRepository vetVisitRepository;
    private final FeedingScheduleRepository feedingScheduleRepository;

    public DataSeeder(OwnerRepository ownerRepository,
                      PetRepository petRepository,
                      MedicationRepository medicationRepository,
                      VetVisitRepository vetVisitRepository,
                      FeedingScheduleRepository feedingScheduleRepository) {
        this.ownerRepository = ownerRepository;
        this.petRepository = petRepository;
        this.medicationRepository = medicationRepository;
        this.vetVisitRepository = vetVisitRepository;
        this.feedingScheduleRepository = feedingScheduleRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // Check if data already exists to avoid duplicates
        if (ownerRepository.count() > 0) {
            System.out.println("Database already seeded. Skipping data seeding.");
            return;
        }

        System.out.println("Seeding database with test data...");

        // Create Owner 1
        Owner owner1 = new Owner();
        owner1.setName("John Smith");
        owner1.setEmail("john.smith@example.com");
        owner1.setPhoneNumber("555-0101");
        owner1.setPasswordHash("$2a$10$dummyHashForTesting123456"); // Dummy BCrypt hash
        owner1 = ownerRepository.save(owner1);

        // Create Owner 2
        Owner owner2 = new Owner();
        owner2.setName("Sarah Johnson");
        owner2.setEmail("sarah.johnson@example.com");
        owner2.setPhoneNumber("555-0202");
        owner2.setPasswordHash("$2a$10$dummyHashForTesting789012");
        owner2 = ownerRepository.save(owner2);

        // Create Pets for Owner 1
        Pet pet1 = new Pet();
        pet1.setName("Max");
        pet1.setType("Dog");
        pet1.setBreed("Golden Retriever");
        pet1.setAge(3);
        pet1.setWeight(32.5);
        pet1.setWeightType(Pet.WeightType.KG);
        pet1.setDateOfBirth(LocalDate.of(2021, 6, 15));
        pet1.setActivityLevel(Pet.ActivityLevel.HIGH);
        pet1.setOwner(owner1);
        pet1 = petRepository.save(pet1);

        Pet pet2 = new Pet();
        pet2.setName("Luna");
        pet2.setType("Cat");
        pet2.setBreed("Siamese");
        pet2.setAge(2);
        pet2.setWeight(4.2);
        pet2.setWeightType(Pet.WeightType.KG);
        pet2.setDateOfBirth(LocalDate.of(2022, 3, 10));
        pet2.setActivityLevel(Pet.ActivityLevel.MEDIUM);
        pet2.setOwner(owner1);
        pet2 = petRepository.save(pet2);

        // Create Pets for Owner 2
        Pet pet3 = new Pet();
        pet3.setName("Buddy");
        pet3.setType("Dog");
        pet3.setBreed("Labrador");
        pet3.setAge(5);
        pet3.setWeight(75.0);
        pet3.setWeightType(Pet.WeightType.LBS);
        pet3.setDateOfBirth(LocalDate.of(2019, 8, 20));
        pet3.setActivityLevel(Pet.ActivityLevel.HIGH);
        pet3.setOwner(owner2);
        pet3 = petRepository.save(pet3);

        Pet pet4 = new Pet();
        pet4.setName("Whiskers");
        pet4.setType("Cat");
        pet4.setBreed("Maine Coon");
        pet4.setAge(4);
        pet4.setWeight(15.0);
        pet4.setWeightType(Pet.WeightType.LBS);
        pet4.setDateOfBirth(LocalDate.of(2020, 11, 5));
        pet4.setActivityLevel(Pet.ActivityLevel.LOW);
        pet4.setOwner(owner2);
        pet4 = petRepository.save(pet4);

        // Create Medications for Max
        Medication med1 = new Medication();
        med1.setName("Heartgard Plus");
        med1.setDosage("1 tablet");
        med1.setFrequency("Monthly");
        med1.setTimeToAdminister(LocalTime.of(9, 0));
        med1.setStartDate(LocalDate.of(2024, 1, 1));
        med1.setEndDate(null); // Ongoing
        med1.setPet(pet1);
        medicationRepository.save(med1);

        Medication med2 = new Medication();
        med2.setName("Carprofen");
        med2.setDosage("50mg");
        med2.setFrequency("Twice daily");
        med2.setTimeToAdminister(LocalTime.of(8, 0));
        med2.setStartDate(LocalDate.of(2024, 12, 1));
        med2.setEndDate(LocalDate.of(2024, 12, 14));
        med2.setPet(pet1);
        medicationRepository.save(med2);

        // Create Medication for Buddy
        Medication med3 = new Medication();
        med3.setName("Flea & Tick Prevention");
        med3.setDosage("1 application");
        med3.setFrequency("Monthly");
        med3.setTimeToAdminister(LocalTime.of(10, 0));
        med3.setStartDate(LocalDate.of(2024, 1, 1));
        med3.setEndDate(null);
        med3.setPet(pet3);
        medicationRepository.save(med3);

        // Create Vet Visits for Max
        VetVisit visit1 = new VetVisit();
        visit1.setVisitDate(LocalDate.of(2024, 11, 10));
        visit1.setVetName("Dr. Emily Parker");
        visit1.setReasonForVisit("Annual checkup");
        visit1.setNotes("Healthy, all vaccinations up to date. Weight stable.");
        visit1.setNextVisitDate(LocalDate.of(2025, 11, 10));
        visit1.setPet(pet1);
        vetVisitRepository.save(visit1);

        VetVisit visit2 = new VetVisit();
        visit2.setVisitDate(LocalDate.of(2024, 9, 5));
        visit2.setVetName("Dr. Emily Parker");
        visit2.setReasonForVisit("Limping on front left paw");
        visit2.setNotes("Mild sprain. Prescribed Carprofen for 2 weeks. Rest recommended.");
        visit2.setNextVisitDate(null);
        visit2.setPet(pet1);
        vetVisitRepository.save(visit2);

        // Create Vet Visit for Luna
        VetVisit visit3 = new VetVisit();
        visit3.setVisitDate(LocalDate.of(2024, 10, 15));
        visit3.setVetName("Dr. Michael Chen");
        visit3.setReasonForVisit("Dental cleaning");
        visit3.setNotes("Dental cleaning completed. Two teeth extracted. Recovery normal.");
        visit3.setNextVisitDate(LocalDate.of(2025, 10, 15));
        visit3.setPet(pet2);
        vetVisitRepository.save(visit3);

        // Create Feeding Schedules for Max
        FeedingSchedule feed1 = new FeedingSchedule();
        feed1.setTime(LocalTime.of(7, 30));
        feed1.setFoodType("Dry kibble - Blue Buffalo Adult");
        feed1.setQuantity(2.0);
        feed1.setQuantityUnit(FeedingSchedule.QuantityUnit.CUPS);
        feed1.setPet(pet1);
        feedingScheduleRepository.save(feed1);

        FeedingSchedule feed2 = new FeedingSchedule();
        feed2.setTime(LocalTime.of(18, 0));
        feed2.setFoodType("Dry kibble - Blue Buffalo Adult");
        feed2.setQuantity(2.0);
        feed2.setQuantityUnit(FeedingSchedule.QuantityUnit.CUPS);
        feed2.setPet(pet1);
        feedingScheduleRepository.save(feed2);

        // Create Feeding Schedule for Luna
        FeedingSchedule feed3 = new FeedingSchedule();
        feed3.setTime(LocalTime.of(8, 0));
        feed3.setFoodType("Wet food - Fancy Feast");
        feed3.setQuantity(1.0);
        feed3.setQuantityUnit(FeedingSchedule.QuantityUnit.CANS);
        feed3.setPet(pet2);
        feedingScheduleRepository.save(feed3);

        FeedingSchedule feed4 = new FeedingSchedule();
        feed4.setTime(LocalTime.of(19, 0));
        feed4.setFoodType("Wet food - Fancy Feast");
        feed4.setQuantity(1.0);
        feed4.setQuantityUnit(FeedingSchedule.QuantityUnit.CANS);
        feed4.setPet(pet2);
        feedingScheduleRepository.save(feed4);

        // Create Feeding Schedules for Buddy
        FeedingSchedule feed5 = new FeedingSchedule();
        feed5.setTime(LocalTime.of(6, 30));
        feed5.setFoodType("Purina Pro Plan Adult");
        feed5.setQuantity(3.5);
        feed5.setQuantityUnit(FeedingSchedule.QuantityUnit.CUPS);
        feed5.setPet(pet3);
        feedingScheduleRepository.save(feed5);

        FeedingSchedule feed6 = new FeedingSchedule();
        feed6.setTime(LocalTime.of(17, 30));
        feed6.setFoodType("Purina Pro Plan Adult");
        feed6.setQuantity(3.5);
        feed6.setQuantityUnit(FeedingSchedule.QuantityUnit.CUPS);
        feed6.setPet(pet3);
        feedingScheduleRepository.save(feed6);

        System.out.println("Database seeding completed successfully!");
        System.out.println("Created 2 owners, 4 pets, 3 medications, 3 vet visits, and 6 feeding schedules.");
    }
}
