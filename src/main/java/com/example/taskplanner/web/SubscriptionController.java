package com.example.taskplanner.web;

import com.example.taskplanner.entities.Subscription;
import com.example.taskplanner.entities.User;
import com.example.taskplanner.service.SubscriptionService;
import com.example.taskplanner.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final UserService userService;

    @Autowired
    public SubscriptionController(SubscriptionService subscriptionService, UserService userService) {
        this.subscriptionService = subscriptionService;
        this.userService = userService;
    }

    @GetMapping("/subscriptions")
    public String showSubscriptions(HttpSession session, Model model) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {

            return "redirect:/";
        }

        User loggedUser = userService.findById(userId);
        List<Subscription> subscriptions = subscriptionService.getSubscriptionsBySubscriberId(userId);
        model.addAttribute("subscriptions", subscriptions);


        List<User> users = subscriptionService.findAllUsersExcludingAdminAndCurrent(userId);
        model.addAttribute("users", users);
        model.addAttribute("loggedUser", loggedUser);
        model.addAttribute("role",loggedUser.getRole());


        model.addAttribute("subscriptionStatus", users.stream()
                .collect(Collectors.toMap(User::getId, user -> subscriptionService.isSubscribed(userId, user.getId()))));

        return "subscriptions";
    }

    @PostMapping("/subscriptions/subscribe")
    public String subscribe(Long subscriberId, Long subscribedToId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {

            return "redirect:/";
        }

        subscriptionService.subscribe(userId, subscribedToId);
        return "redirect:/subscriptions";
    }

    @PostMapping("/subscriptions/unsubscribe")
    public String unsubscribe(Long subscriberId, Long subscribedToId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/";
        }

        subscriptionService.unsubscribe(userId, subscribedToId);
        return "redirect:/subscriptions";
    }
}
