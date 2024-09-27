package com.example.taskplanner.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class UserLoginModel {

    private Long id;
    @Size(min = 3,max = 20,message = "Username length must be between 3 and 20 characters!")
    @NotEmpty
    private String username;


    @Size(min = 3,max = 20,message = "Password length must be between 3 and 20 characters!")
    private String password;

    public UserLoginModel() {
    }

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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
