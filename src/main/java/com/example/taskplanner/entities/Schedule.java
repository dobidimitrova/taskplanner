package com.example.taskplanner.entities;

import com.example.taskplanner.config.AccessCodeGenerator;
import com.example.taskplanner.entities.enums.Visibility;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "schedules")
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String place;

    @Column(nullable = false)
    private LocalDateTime startData;

    @Enumerated(EnumType.STRING)
    private Visibility visibility;

    @ManyToOne
    private User createdBy;

    @Column(nullable = false)
    private String accessCode;

    @Column(nullable = false)
    private String status;

    @Column
    private Boolean isNew;

    @ManyToOne
    @JoinColumn(name = "assignee_id",nullable = false)
    private User assignee;
    @Column(name = "notification_sent")
    private boolean notificationSent = false;

    public Schedule(String name, String place) {
        this.id = Long.valueOf(UUID.randomUUID().toString());
        this.name = name;
        this.place = place;
    }

    public Schedule() {

    }

    public boolean isNotificationSent() {
        return notificationSent;
    }

    public void setNotificationSent(boolean notificationSent) {
        this.notificationSent = notificationSent;
    }

    public Boolean getIsNew() {
        return isNew;
    }

    public void setIsNew(Boolean aNew) {
        isNew = aNew;
    }

    public String getAccessCode() {
        return accessCode;
    }

    public void setAccessCode(String accessCode) {
        this.accessCode = accessCode;
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

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartData() {
        return startData;
    }

    public void setStartData(LocalDateTime startData) {
        this.startData = startData;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public User getAssignee() {
        return assignee;
    }

    public void setAssignee(User assignee) {
        this.assignee = assignee;
    }
}
