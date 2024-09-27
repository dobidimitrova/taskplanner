package com.example.taskplanner.web;

import com.example.taskplanner.dto.CreateTaskModel;
import com.example.taskplanner.entities.Schedule;
import com.example.taskplanner.entities.Task;
import com.example.taskplanner.entities.User;
import com.example.taskplanner.service.SubscriptionService;
import com.example.taskplanner.service.UserService;
import com.example.taskplanner.service.impl.TaskServiceImpl;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    private final TaskServiceImpl taskService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;

    @Autowired
    public TaskController(TaskServiceImpl taskService, UserService userService, SubscriptionService subscriptionService) {
        this.taskService = taskService;
        this.userService = userService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/create-task")
    public String createTaskPage(Model model, HttpSession session) {
        CreateTaskModel taskModel = new CreateTaskModel();
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

        model.addAttribute("taskModel", taskModel);
        model.addAttribute("allUsers", subscribedUsers);
        model.addAttribute("role",currentUser.getRole());


        if ("admin".equalsIgnoreCase(String.valueOf(currentUser.getRole()))) {
            subscribedUsers.add(currentUser);
        }

        return "create-task";
    }

    @PostMapping("/create-task")
    public String createTask(@ModelAttribute("taskModel") @Valid CreateTaskModel taskModel,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes,
                             HttpSession httpSession) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("taskModel", taskModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.taskModel", bindingResult);
            return "redirect:/tasks/create-task";
        }

        Long userId = (Long) httpSession.getAttribute("userId");
        taskService.createTask(taskModel, userId);
        return "redirect:/tasks/task-page";
    }

    @PostMapping("/complete-task")
    public String completeTask(@RequestParam("taskId") Long taskId) {

        taskService.markTaskAsCompleted(taskId);
        return "redirect:/tasks/task-page";
    }


    @PostMapping("/delete-task")
    public String deleteTask(@RequestParam("taskId") Long taskId) {
        taskService.deleteTask(taskId);
        return "redirect:/tasks/task-page";
    }

    @GetMapping("/task-page")
    public String taskPage(Model model, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        User currentUser = userService.findById(userId);

        if (userId == null) {
            return "redirect:/";
        }

        List<CreateTaskModel> tasks = taskService.getAllTasksForLoggedInUser(userId);

        tasks.forEach(task -> {
            Boolean isNew = task.getIsNew();
            if (isNew == null) {
                isNew = Boolean.FALSE;
            }
            task.setIsNew(isNew);
        });

        model.addAttribute("tasks", tasks);
        model.addAttribute("role", currentUser.getRole());
        return "tasks-page";
    }


    @GetMapping("/access")
    public String showAccessCodeForm(@RequestParam(value = "code", required = false) String code, Model model) {
        if (code != null) {
            model.addAttribute("code", code);
        }
        return "tasks-page";
    }

    @PostMapping("/access")
    public String validateAccessCode(@RequestParam("taskId") Long taskId,
                                     @RequestParam("code") String accessCode,
                                     Model model,HttpSession httpSession) {
        Task task = taskService.findById(taskId);

        if (task == null) {
            model.addAttribute("errorMessage", "Task not found.");
            model.addAttribute("tasks", taskService.findAll());
            return "tasks-page";
        }

        String validAccessCode = task.getAccessCode();

        if (!accessCode.equals(validAccessCode)) {
            Long userId = (Long) httpSession.getAttribute("userId");
            model.addAttribute("errorMessage", "Invalid access code.");
            model.addAttribute("tasks", taskService.getAllTasksForLoggedInUser(userId));
            model.addAttribute("accessCodeInvalid", true);
        } else {
            task.setIsNew(false);
            taskService.updateTask(task);
            Long userId = (Long) httpSession.getAttribute("userId");
            model.addAttribute("tasks", taskService.getAllTasksForLoggedInUser(userId));
        }
        return "tasks-page";
    }



    private boolean isValidCode(String code) {
        return code != null && !code.isEmpty();
    }
}
