package com.example.taskplanner.repository;

import com.example.taskplanner.entities.Schedule;
import com.example.taskplanner.entities.Subscription;
import com.example.taskplanner.entities.User;
import com.example.taskplanner.entities.enums.Visibility;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    Optional<Schedule> findByAccessCode(String accessCode);

    List<Schedule> findAllByAssignee_IdAndStatusNotIn(Long userId, List<String> statuses);

    List<Schedule> findAllByCreatedBy_Id(Long userId);

    List<Schedule> findAllByStartDataBetween(LocalDateTime start, LocalDateTime end);
}

