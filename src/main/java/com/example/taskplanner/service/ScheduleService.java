package com.example.taskplanner.service;


import com.example.taskplanner.dto.CreateScheduleModel;
import com.example.taskplanner.entities.Schedule;
import com.example.taskplanner.entities.User;
import com.example.taskplanner.entities.enums.Visibility;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ScheduleService {
    void createSchedule(Schedule schedule, Long creatorId, Visibility visibility);

    Schedule findScheduleByAccessCode(String code);

    List<CreateScheduleModel> getAllSchedulesForLoggedInUser(Long userId);

    void markTaskAsCompleted(Long scheduleId);
    void markTaskAsDeleted(Long scheduleId);

    List<Schedule> getAllSchedulesCreatedByUser(Long userId);

    List<Schedule> getSchedulesStartingBetween(LocalDateTime now, LocalDateTime oneHourLater);

    void updateSchedule(Schedule schedule);

    Schedule findById(Long scheduleId);

    Object findAll();
}
