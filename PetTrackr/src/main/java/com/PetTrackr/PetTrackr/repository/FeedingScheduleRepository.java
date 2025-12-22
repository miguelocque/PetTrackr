package com.PetTrackr.PetTrackr.repository;

import com.PetTrackr.PetTrackr.entity.FeedingSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FeedingScheduleRepository extends JpaRepository<FeedingSchedule, Long> {
    // for individual pet feeding schedules sorted by time
    List<FeedingSchedule> findByPetIdOrderByTimeAsc(Long petId);

}