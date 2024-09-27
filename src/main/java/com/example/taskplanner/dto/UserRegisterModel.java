package com.example.taskplanner.dto;

import com.example.taskplanner.entities.enums.SendBy;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.Length;

public class UserRegisterModel {
    private Long id;

    @Size(min = 3, max = 20, message = "Username must be between 3 and 20 letters")
    @NotEmpty(message = "Username cannot be empty")
    private String username;

    @Email(message = "Email should be valid")
    @NotEmpty(message = "Email cannot be empty")
    private String email;

    @Size(min = 13, max = 13, message = "Phone must be exactly 13 characters with (+359)")
    @NotEmpty(message = "Phone cannot be empty")
    private String phoneNumber;

    private String notificationPreference;
    @Size(min = 3, max = 20, message = "Password must be exactly 3-10 characters")
    @NotEmpty(message = "Password cannot be empty")
    private String password;

    @Size(min = 3, max = 20, message = "Confirm Password must be 3-10 characters")
    @NotEmpty(message = "Confirm Password cannot be empty")
    private String confirmPassword;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getNotificationPreference() {
        return notificationPreference;
    }

    public void setNotificationPreference(String notificationPreference) {
        this.notificationPreference = notificationPreference;
    }
}
