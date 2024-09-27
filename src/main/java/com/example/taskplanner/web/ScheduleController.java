package com.example.taskplanner.web;

import com.example.taskplanner.config.AccessCodeGenerator;
import com.example.taskplanner.dto.CreateScheduleModel;
import com.example.taskplanner.entities.Schedule;
import com.example.taskplanner.entities.User;
import com.example.taskplanner.entities.enums.Visibility;
import com.example.taskplanner.repository.UserRepository;
import com.example.taskplanner.service.ScheduleService;
import com.example.taskplanner.service.SubscriptionService;
import com.example.taskplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ScheduleController {

    private final UserService userService;
    private final ScheduleService scheduleService;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;

    @Autowired
    public ScheduleController(UserService userService, ScheduleService scheduleService, UserRepository userRepository, SubscriptionService subscriptionService) {
        this.userService = userService;
        this.scheduleService = scheduleService;
        this.userRepository = userRepository;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/create-schedule")
    public String showScheduleForm(Model model, HttpSession session) {
        CreateScheduleModel createScheduleModel = new CreateScheduleModel();
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return "redirect:/";
        }

        User currentUser = userService.findById(userId);
        List<User> users = userService.findAllUsers();

        List<User> nonAdminUsers = users.stream()
                .filter(user -> !"admin".equalsIgnoreCase(String.valueOf(user.getRole())))
                .collect(Collectors.toList());

        List<User> subscribedUsers = nonAdminUsers.stream()
                .filter(user -> subscriptionService.isSubscribed(userId, user.getId()) || user.getId().equals(userId))
                .collect(Collectors.toList());

        if ("admin".equalsIgnoreCase(String.valueOf(currentUser.getRole()))) {
            subscribedUsers.add(currentUser);
        }

        model.addAttribute("users", users);
        model.addAttribute("allUsers", subscribedUsers);
        model.addAttribute("schedules", createScheduleModel);
        model.addAttribute("currentUserEmail", currentUser.getEmail());
        model.addAttribute("role", currentUser.getRole());

        return "create-schedule";
    }

    @PostMapping("/create-schedule")
    public String createSchedule(HttpSession httpSession,
                                 @RequestParam("name") String name,
                                 @RequestParam("place") String place,
                                 @RequestParam("startData") LocalDateTime startDate,
                                 @RequestParam("visibility") Visibility visibility,
                                 @RequestParam(value = "assigneeId", required = false) Long assigneeId) {

        String accessCode = AccessCodeGenerator.generateAccessCode();

        Schedule schedule = new Schedule();
        schedule.setName(name);
        schedule.setPlace(place);
        schedule.setStartData(startDate);
        schedule.setStatus("Pending");
        schedule.setAccessCode(accessCode);
        schedule.setVisibility(visibility);
        Long userId = (Long) httpSession.getAttribute("userId");

        if (assigneeId != null) {
            User assignee = userRepository.findById(assigneeId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid assignee ID"));
            schedule.setAssignee(assignee);
        }

        scheduleService.createSchedule(schedule, userId, schedule.getVisibility());

        return "redirect:/schedules";
    }

    @GetMapping("/schedules")
    public String taskPage(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/";
        }
        Long userId = (Long) session.getAttribute("userId");
        User currentUser = userService.findById(userId);
        List<CreateScheduleModel> schedules = scheduleService.getAllSchedulesForLoggedInUser(userId);

        schedules.forEach(schedule -> {
            Boolean isNew = schedule.getIsNew();
            if (isNew == null) {
                isNew = Boolean.FALSE;
            }
            schedule.setIsNew(isNew);
        });

        model.addAttribute("schedules", schedules);
        model.addAttribute("role", currentUser.getRole());
        return "schedules-page";
    }

    @PostMapping("/schedules/complete")
    public String completeSchedule(@RequestParam("scheduleId") Long scheduleId) {
        scheduleService.markTaskAsCompleted(scheduleId);
        return "redirect:/schedules";
    }

    @PostMapping("/schedules/delete")
    public String deleteSchedule(@RequestParam("scheduleId") Long scheduleId) {
        scheduleService.markTaskAsDeleted(scheduleId);
        return "redirect:/schedules";
    }

    @GetMapping("/schedules/access")
    public String showAccessCodeForm(@RequestParam(value = "code", required = false) String code, Model model) {
        if (code != null) {
            model.addAttribute("code", code);
        }
        return "schedules-page";
    }

    @PostMapping("/schedules/access")
    public String validateAccessCode(@RequestParam("scheduleId") Long scheduleId,
                                     @RequestParam("code") String accessCode,
                                     Model model,HttpSession httpSession) {
        Schedule schedule = scheduleService.findById(scheduleId);

        if (schedule == null) {
            model.addAttribute("errorMessage", "Schedule not found.");
            model.addAttribute("schedules", scheduleService.findAll());
            return "schedules-page";
        }

        String validAccessCode = schedule.getAccessCode();

        if (!accessCode.equals(validAccessCode)) {
            Long userId = (Long) httpSession.getAttribute("userId");
            model.addAttribute("errorMessage", "Invalid access code.");
            model.addAttribute("schedules", scheduleService.getAllSchedulesForLoggedInUser(userId));
        } else {
            schedule.setIsNew(false);
            scheduleService.updateSchedule(schedule);
            Long userId = (Long) httpSession.getAttribute("userId");
            model.addAttribute("schedules", scheduleService.getAllSchedulesForLoggedInUser(userId));
        }
        return "schedules-page";
    }
}
