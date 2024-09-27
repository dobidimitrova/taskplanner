package com.example.taskplanner.service;

import com.example.taskplanner.dto.UserLoginServiceModel;
import com.example.taskplanner.dto.UserRegisterModel;
import com.example.taskplanner.entities.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void registerModel(UserRegisterModel userServiceModel);

    UserLoginServiceModel findByUsernameAndPassword(String username, String password);


    void saveUserToDatabase(UserRegisterModel userRegisterModel);

    List<User> findAllUsers();


    User findByEmail(String name);
    boolean usernameExists(String username);

    User findById(Long assigneeId);

    List<User> getUsersSubscribedByUserId(Long userId);

    boolean emailExists(String email);
}
