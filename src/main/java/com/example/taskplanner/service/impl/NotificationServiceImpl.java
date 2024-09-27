package com.example.taskplanner.service.impl;

import com.example.taskplanner.entities.Schedule;
import com.example.taskplanner.entities.User;
import com.example.taskplanner.entities.enums.SendBy;
import com.example.taskplanner.repository.UserRepository;
import com.example.taskplanner.send.EmailSender;
import com.example.taskplanner.send.SmsSender;
import com.example.taskplanner.service.NotificationService;
import com.example.taskplanner.service.ScheduleService;
import com.example.taskplanner.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationServiceImpl implements NotificationService {
    private final ScheduleService scheduleService;
    private final SmsSender smsSender;

    @Autowired
    public NotificationServiceImpl(ScheduleService scheduleService, SmsSender smsSender) {
        this.scheduleService = scheduleService;
        this.smsSender = smsSender;
    }

    @Scheduled(fixedRate = 60000)
    public void checkForUpcomingSchedules() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneHourLater = now.plusHours(1);

        List<Schedule> upcomingSchedules = scheduleService.getSchedulesStartingBetween(now, oneHourLater);

        for (Schedule schedule : upcomingSchedules) {
            if (!schedule.isNotificationSent() && schedule.getStartData().isAfter(now) && schedule.getStartData().isBefore(oneHourLater)) {
                User assignee = schedule.getAssignee();
                if (assignee != null) {
                    SendBy preference = assignee.getNotificationPreference();
                    String message = String.format("Reminder: Your meeting '%s' is starting in 1 hour. Location: %s", schedule.getName(), schedule.getPlace());

                    if (preference == SendBy.EMAIL) {
                        EmailSender.sendEmail(assignee.getEmail(), "Meeting Reminder", message);
                    } else if (preference == SendBy.SMS) {
                        smsSender.sendSms(assignee.getPhoneNumber(), message);
                    }

                    schedule.setNotificationSent(true);
                    scheduleService.updateSchedule(schedule);
                }
            }
        }
    }
}
