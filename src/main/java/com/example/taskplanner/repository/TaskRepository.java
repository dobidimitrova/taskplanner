package com.example.taskplanner.repository;

import com.example.taskplanner.entities.Schedule;
import com.example.taskplanner.entities.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findTaskByAccessCode(String code);
    List<Task> findTasksByAssignee_IdAndStatusNotIn(Long assignee_id, List<String>statuses);
    List<Task> findAllByCreatedBy_Id(Long userId);
}
