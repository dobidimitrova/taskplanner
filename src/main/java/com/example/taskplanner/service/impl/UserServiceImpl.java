package com.example.taskplanner.service.impl;

import com.example.taskplanner.dto.UserLoginServiceModel;
import com.example.taskplanner.dto.UserRegisterModel;
import com.example.taskplanner.entities.Schedule;
import com.example.taskplanner.entities.Subscription;
import com.example.taskplanner.entities.Task;
import com.example.taskplanner.entities.User;
import com.example.taskplanner.entities.enums.Role;
import com.example.taskplanner.entities.enums.SendBy;
import com.example.taskplanner.repository.ScheduleRepository;
import com.example.taskplanner.repository.TaskRepository;
import com.example.taskplanner.repository.UserRepository;
import com.example.taskplanner.service.SubscriptionService;
import com.example.taskplanner.service.UserService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SubscriptionService subscriptionService;
    private static final String ADMIN_EMAIL = "dobidimitrova12@abv.bg";

    public UserServiceImpl(UserRepository userRepository, ModelMapper modelMapper, SubscriptionService subscriptionService, ScheduleRepository scheduleRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.subscriptionService = subscriptionService;
    }

    @Override
    public UserLoginServiceModel findByUsernameAndPassword(String username, String password) {
        Optional<User> userOptional = userRepository.findByUsernameAndPassword(username, password);
        return userOptional.map(user -> modelMapper.map(user, UserLoginServiceModel.class)).orElse(null);
    }

    @Override
    public void registerModel(UserRegisterModel userModel) {
        saveUserToDatabase(userModel);
    }


    @Override
    public void saveUserToDatabase(UserRegisterModel userRegisterModel) {
        User user = new User();
        user.setUsername(userRegisterModel.getUsername());
        user.setEmail(userRegisterModel.getEmail());
        Role role = Role.valueOf(String.valueOf(user.getEmail().equals(ADMIN_EMAIL) ? Role.admin : Role.user));
        user.setPhoneNumber(userRegisterModel.getPhoneNumber());
        user.setPassword(userRegisterModel.getPassword());
        user.setRole(role);
        if (userRegisterModel.getNotificationPreference().equals("BOTH")) {
            user.setNotificationPreference(SendBy.BOTH);
        } else {
            user.setNotificationPreference(SendBy.valueOf(userRegisterModel.getNotificationPreference()));
        }
        userRepository.save(user);
    }


    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Override
    public User findById(Long assigneeId) {
        return userRepository.findById(assigneeId).orElse(null);
    }

    @Override
    public List<User> getUsersSubscribedByUserId(Long userId) {
        List<Subscription> subscriptions = subscriptionService.getSubscriptionsBySubscriberId(userId);

        return subscriptions.stream()
                .map(Subscription::getSubscribedTo)
                .filter(subscribedUser -> !subscribedUser.getId().equals(userId))
                .collect(Collectors.toList());
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

}
