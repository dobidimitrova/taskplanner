package com.example.taskplanner.service.impl;

import com.example.taskplanner.dto.CreateScheduleModel;
import com.example.taskplanner.entities.Schedule;
import com.example.taskplanner.entities.Subscription;
import com.example.taskplanner.entities.User;
import com.example.taskplanner.entities.enums.SendBy;
import com.example.taskplanner.entities.enums.Visibility;
import com.example.taskplanner.repository.ScheduleRepository;
import com.example.taskplanner.repository.SubscriptionRepository;
import com.example.taskplanner.repository.UserRepository;
import com.example.taskplanner.send.EmailSender;
import com.example.taskplanner.send.SmsSender;
import com.example.taskplanner.service.ScheduleService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SmsSender smsSender;

    @Autowired
    public ScheduleServiceImpl(ScheduleRepository scheduleRepository, ModelMapper modelMapper, UserRepository userRepository, SubscriptionRepository subscriptionRepository, SmsSender smsSender) {
        this.scheduleRepository = scheduleRepository;
        this.modelMapper = modelMapper;
        this.userRepository = userRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.smsSender = smsSender;
    }

    @Override
    public void createSchedule(Schedule schedule, Long creatorId, Visibility visibility) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID"));
        schedule.setCreatedBy(creator);

        if (schedule.getAssignee() == null) {
            schedule.setAssignee(creator);
        }

        schedule.setIsNew(!schedule.getCreatedBy().getEmail().equals(schedule.getAssignee().getEmail()));


        boolean isAssignedToCreator = schedule.getAssignee() != null && schedule.getAssignee().getId().equals(creatorId);

        if (visibility == Visibility.Public) {
            List<Subscription> subscriptions = subscriptionRepository.findBySubscriber(creator);
            for (Subscription subscription : subscriptions) {
                User subscribedUser = subscription.getSubscribedTo();
                if (!subscribedUser.getId().equals(creator.getId())) {
                    Schedule userSchedule = createCopyOfSchedule(schedule);
                    userSchedule.setAssignee(subscribedUser);
                    userSchedule.setIsNew(true);
                    scheduleRepository.save(userSchedule);

                    SendBy preference = subscribedUser.getNotificationPreference();

                    sendNotification(schedule, subscribedUser, preference);
                }
            }
        } else if (visibility == Visibility.Private) {
            scheduleRepository.save(schedule);
            User assignee = userRepository.findById(schedule.getAssignee().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid assignee ID"));

            SendBy preference = assignee.getNotificationPreference();

            if (!isAssignedToCreator) {
                sendNotification(schedule, assignee, preference);
            }
        }
    }

    private void sendNotification(Schedule schedule, User assignee, SendBy preference) {
        if (preference == SendBy.EMAIL) {
            String subject = "New Schedule Assigned";
            String body = String.format("You have been assigned a new schedule from %s. Access Code: %s", schedule.getCreatedBy().getEmail(), schedule.getAccessCode());
            EmailSender.sendEmail(assignee.getEmail(), subject, body);
        } else if (preference == SendBy.SMS) {
            String message = String.format("New schedule assigned by %s. Access Code: %s", schedule.getCreatedBy().getEmail(), schedule.getAccessCode());
            smsSender.sendSms(assignee.getPhoneNumber(), message);
        } else {
            String subject = "New Schedule Assigned";
            String body = String.format("You have been assigned a new schedule from %s. Access Code: %s", schedule.getCreatedBy().getEmail(), schedule.getAccessCode());
            EmailSender.sendEmail(assignee.getEmail(), subject, body);
            String message = String.format("New schedule assigned by %s. Access Code: %s", schedule.getCreatedBy().getEmail(), schedule.getAccessCode());
            smsSender.sendSms(assignee.getPhoneNumber(), message);

        }
    }


    private Schedule createCopyOfSchedule(Schedule schedule) {
        Schedule copy = new Schedule();
        copy.setName(schedule.getName());
        copy.setPlace(schedule.getPlace());
        copy.setStartData(schedule.getStartData());
        copy.setStatus(schedule.getStatus());
        copy.setVisibility(schedule.getVisibility());
        copy.setAssignee(schedule.getAssignee());
        copy.setAccessCode(schedule.getAccessCode());
        copy.setCreatedBy(schedule.getCreatedBy());
        copy.setIsNew(schedule.getIsNew());
        return copy;
    }

    @Override
    public Schedule findScheduleByAccessCode(String code) {
        Optional<Schedule> schedule = scheduleRepository.findByAccessCode(code);
        return schedule.orElse(null);
    }

    @Override
    public List<CreateScheduleModel> getAllSchedulesForLoggedInUser(Long userId) {
        List<Schedule> schedules = scheduleRepository.findAllByAssignee_IdAndStatusNotIn(userId, List.of("Completed", "Deleted"));
        return schedules.stream()
                .map(schedule -> modelMapper.map(schedule, CreateScheduleModel.class))
                .collect(Collectors.toList());
    }

    @Override
    public void markTaskAsCompleted(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        schedule.setStatus("Completed");
        scheduleRepository.save(schedule);

        // Notify the creator
        User creator = schedule.getCreatedBy();
        SendBy preference = creator.getNotificationPreference();


        if(!schedule.getAssignee().getEmail().equals(creator.getEmail())) {
            String message = String.format("The schedule '%s' has been marked as completed by %s.",
                    schedule.getName(), schedule.getAssignee().getEmail());
            if (preference == SendBy.EMAIL) {
                String subject = "Schedule Completed Notification";
                EmailSender.sendEmail(creator.getEmail(), subject, message);
            } else if (preference == SendBy.SMS) {
                smsSender.sendSms(creator.getPhoneNumber(), message);
            }
        }
    }


    @Override
    public void markTaskAsDeleted(Long scheduleId) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new RuntimeException("Schedule not found"));
        schedule.setStatus("Deleted");
        scheduleRepository.save(schedule);
    }

    @Override
    public List<Schedule> getAllSchedulesCreatedByUser(Long userId) {
        return scheduleRepository.findAllByCreatedBy_Id(userId);
    }

    @Override
    public List<Schedule> getSchedulesStartingBetween(LocalDateTime start, LocalDateTime end) {
        return scheduleRepository.findAllByStartDataBetween(start, end);
    }

    @Override
    public void updateSchedule(Schedule schedule) {
        scheduleRepository.save(schedule);
    }

    public Schedule findById(Long scheduleId) {
        return scheduleRepository.findById(scheduleId).orElse(null);
    }

    public List<Schedule> findAll() {
        return scheduleRepository.findAll();
    }


}
