package com.example.taskplanner.service.impl;

import com.example.taskplanner.config.AccessCodeGenerator;
import com.example.taskplanner.dto.CreateTaskModel;
import com.example.taskplanner.entities.Schedule;
import com.example.taskplanner.entities.Task;
import com.example.taskplanner.entities.User;
import com.example.taskplanner.entities.enums.SendBy;
import com.example.taskplanner.repository.TaskRepository;
import com.example.taskplanner.repository.UserRepository;
import com.example.taskplanner.send.SmsSender;
import com.example.taskplanner.service.TaskService;
import com.example.taskplanner.send.EmailSender; // Импортирайте EmailSender
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SmsSender smsSender;

    @Autowired
    public TaskServiceImpl(TaskRepository taskRepository, UserRepository userRepository, ModelMapper modelMapper, SmsSender smsSender) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.smsSender = smsSender;
    }

    @Override
    public void createTask(CreateTaskModel taskModel, Long creatorId) {
        Task task = modelMapper.map(taskModel, Task.class);

        User assignee = userRepository.findById(taskModel.getAssignee().getId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid assignee ID"));

        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));

        task.setCreatedBy(creator);
        task.setStatus("Pending");
        task.setAssignee(assignee);
        task.setAccessCode(AccessCodeGenerator.generateAccessCode());

        task.setIsNew(!creatorId.equals(taskModel.getAssignee().getId()));

        taskRepository.save(task);

        SendBy preference = assignee.getNotificationPreference();
        String accessCode = task.getAccessCode();

        if (!creatorId.equals(taskModel.getAssignee().getId())) {
            if (preference == SendBy.EMAIL) {
                String subject = "New Task Assigned";
                String body = String.format("You have been assigned a new task from %s. Access Code: %s", creator.getEmail(), accessCode);
                EmailSender.sendEmail(assignee.getEmail(), subject, body);
            } else if (preference == SendBy.SMS) {
                String message = String.format("New task assigned by %s. Access Code: %s", creator.getEmail(), accessCode);
                smsSender.sendSms(assignee.getPhoneNumber(), message);
            } else {
                String subject = "New Task Assigned";
                String body = String.format("You have been assigned a new task from %s. Access Code: %s", creator.getEmail(), accessCode);
                EmailSender.sendEmail(assignee.getEmail(), subject, body);
                String message = String.format("New task assigned by %s. Access Code: %s", creator.getEmail(), accessCode);
                smsSender.sendSms(assignee.getPhoneNumber(), message);
            }
        }
    }


    @Override
    public List<CreateTaskModel> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(task -> {
                    CreateTaskModel taskModel = new CreateTaskModel();
                    taskModel.setId(task.getId());
                    taskModel.setName(task.getName());
                    taskModel.setStartDate(task.getStartDate());
                    taskModel.setEndDate(task.getEndDate());
                    taskModel.setDescription(task.getDescription());
                    return taskModel;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Task findTaskByAccessCode(String code) {
        Optional<Task> task = taskRepository.findTaskByAccessCode(code);
        return task.orElse(null);
    }

    @Override
    public void markTaskAsCompleted(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        task.setStatus("Completed");
        taskRepository.save(task);

        User creator = task.getCreatedBy();
        SendBy preference = creator.getNotificationPreference();


        if (!task.getAssignee().getEmail().equals(creator.getEmail())) {
            String message = String.format("Task '%s' assigned to %s has been marked as completed.",
                    task.getName(),
                    task.getAssignee().getEmail());
            if (preference == SendBy.EMAIL) {
                String subject = "Task Completed";
                EmailSender.sendEmail(creator.getEmail(), subject, message);
            } else if (preference == SendBy.SMS) {
                smsSender.sendSms(creator.getPhoneNumber(), message);
            } else {
                String subject = "Task Completed";
                EmailSender.sendEmail(creator.getEmail(), subject, message);
                smsSender.sendSms(creator.getPhoneNumber(), message);
            }
        }
    }


    @Override
    public void deleteTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        task.setStatus("Deleted");
        taskRepository.save(task);
    }


    @Override
    public List<Task> getAllTasksCreatedByUser(Long userId) {
        return taskRepository.findAllByCreatedBy_Id(userId);
    }

    @Override
    public Task findTaskById(Long taskId) {
        return taskRepository.findById(taskId).orElse(null);
    }

    @Override
    public void updateTask(Task task) {
        taskRepository.save(task);
    }

    public Task findById(Long scheduleId) {
        return taskRepository.findById(scheduleId).orElse(null);
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Override
    public List<CreateTaskModel> getAllTasksForLoggedInUser(Long userId) {
        List<Task> tasks = taskRepository.findTasksByAssignee_IdAndStatusNotIn(userId,List.of("Completed", "Deleted"));

        return tasks.stream()
                .map(task -> modelMapper.map(task, CreateTaskModel.class))
                .collect(Collectors.toList());
    }

}
