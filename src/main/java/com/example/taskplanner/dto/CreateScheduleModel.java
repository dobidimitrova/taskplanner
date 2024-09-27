package com.example.taskplanner.dto;

import com.example.taskplanner.entities.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class CreateScheduleModel {

    private Long id;

    @Size(min = 3, message = "Schedule name must be minimum 3 letters")
    @NotEmpty(message = "Schedule name cannot be empty")
    private String name;

    private LocalDateTime startData;

    @Size(min = 3, message = "Place must be minimum 3 letters")
    @NotEmpty(message = "Place can not be empty")
    private String place;

    @NotEmpty(message = "Visibility can not be empty")
    private String visibility;

    private User assignee;

    private String status;

    private User createdBy;

    private List<User> users;
    private String accessCode;
    private Boolean isNew = Boolean.TRUE;

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean isNew) {
        this.isNew = isNew;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getStartData() {
        return startData;
    }

    public void setStartData(LocalDateTime startData) {
        this.startData = startData;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
    }

}
