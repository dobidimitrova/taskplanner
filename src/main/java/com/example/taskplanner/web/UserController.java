package com.example.taskplanner.web;

import com.example.taskplanner.dto.*;
import com.example.taskplanner.entities.Schedule;
import com.example.taskplanner.entities.Task;
import com.example.taskplanner.entities.User;
import com.example.taskplanner.repository.UserRepository;
import com.example.taskplanner.service.ScheduleService;
import com.example.taskplanner.service.TaskService;
import com.example.taskplanner.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final ScheduleService scheduleService;
    private final TaskService taskService;
    private final UserRepository userRepository;

    @Autowired
    public UserController(UserService userService, ScheduleService scheduleService, TaskService taskService, UserRepository userRepository) {
        this.userService = userService;
        this.scheduleService = scheduleService;
        this.taskService = taskService;
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}/details")
    public String getUserDetails(@PathVariable("id") Long userId, Model model) {
        List<Schedule> schedulesCreatedByUser = scheduleService.getAllSchedulesCreatedByUser(userId);
        List<Task> tasksCreatedByUser = taskService.getAllTasksCreatedByUser(userId);
        List<User> subscribedUsers = userService.getUsersSubscribedByUserId(userId);
        User user = userService.findById(userId);

        model.addAttribute("schedulesCreatedByUser", schedulesCreatedByUser);
        model.addAttribute("tasksCreatedByUser", tasksCreatedByUser);
        model.addAttribute("subscribedUsers", subscribedUsers);
        model.addAttribute("user", user);

        return "user-details";
    }

    @GetMapping("/register")
    public String registerPage(Model model) {
        if (!model.containsAttribute("userRegisterModel")) {
            model.addAttribute("userRegisterModel", new UserRegisterModel());
        }
        return "register-page";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("userRegisterModel") @Valid UserRegisterModel userRegisterModel,
                               BindingResult bindingResult, Model model) {

        boolean hasErrors = false;

        if (!userRegisterModel.getPassword().equals(userRegisterModel.getConfirmPassword())) {
            model.addAttribute("passwordError", "Passwords do not match");
            hasErrors = true;
        }

        if (userService.usernameExists(userRegisterModel.getUsername())) {
            model.addAttribute("usernameError", "Username is already taken. Please choose another one.");
            hasErrors = true;
        }

        if (userService.emailExists(userRegisterModel.getEmail())) {
            model.addAttribute("emailError", "Email is already registered. Please use another one.");
            hasErrors = true;
        }


        if (bindingResult.hasErrors() || hasErrors) {
            model.addAttribute("userRegisterModel", userRegisterModel);
            model.addAttribute("org.springframework.validation.BindingResult.userRegisterModel", bindingResult);
            return "register-page";
        }

        userService.registerModel(userRegisterModel);
        return "login-page";
    }



    @GetMapping("/login")
    public String loginPage(Model model){
        if(!model.containsAttribute("userLoginModel")){
            model.addAttribute("userLoginModel", new UserLoginModel());
        }
        return "login-page";
    }

    @PostMapping("/login")
    public String loginConfirm(@ModelAttribute("userLoginModel") @Valid UserLoginModel userLoginModel,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes,
                               HttpSession httpSession) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("userLoginModel", userLoginModel);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.userLoginModel", bindingResult);
            return "redirect:/user/login";
        }

        UserLoginServiceModel userServiceModel = userService.findByUsernameAndPassword(userLoginModel.getUsername(), userLoginModel.getPassword());

        if (userServiceModel == null) {
            redirectAttributes.addFlashAttribute("userLoginModel", userLoginModel);
            redirectAttributes.addFlashAttribute("loginError", "Invalid username or password.");
            return "redirect:/user/login";
        }

        httpSession.setAttribute("user", userServiceModel.getUsername());
        httpSession.setAttribute("userId", userServiceModel.getId());
        httpSession.setAttribute("role", userServiceModel.getRole());
        return "redirect:/user/calendar-page";
    }



    @GetMapping("/calendar-page")
    public String calendarPage(HttpSession httpSession) {
        if (httpSession.getAttribute("user") == null) {
            return "redirect:/";
        }
        return "calendar-page";
    }

    @GetMapping("/all-users")
    public String getAllUsers(HttpSession httpSession, Model model) {
        if (httpSession.getAttribute("user") == null) {
            return "redirect:/";
        }
        String role = (String) httpSession.getAttribute("role");
        if (!"admin".equals(role)) {
            return "redirect:/"; 
        }
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "all-users";
    }

    @GetMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return "index-page";
    }
}
