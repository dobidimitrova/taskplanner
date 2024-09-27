package com.example.taskplanner.service;

import com.example.taskplanner.dto.CreateTaskModel;
import com.example.taskplanner.entities.Task;
import org.springframework.stereotype.Service;

import java.util.List;


public interface TaskService {
    void createTask(CreateTaskModel taskModel, Long creatorId);
    List<CreateTaskModel> getAllTasks();

    Task findTaskByAccessCode(String code);

    List<CreateTaskModel> getAllTasksForLoggedInUser(Long userId);

    void markTaskAsCompleted(Long taskId);
    void deleteTask(Long taskId);

    List<Task> getAllTasksCreatedByUser(Long userId);

    Task findTaskById(Long taskId);

    void updateTask(Task task);

    Task findById(Long taskId);

    Object findAll();
}
