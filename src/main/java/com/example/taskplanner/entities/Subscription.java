package com.example.taskplanner.entities;

import jakarta.persistence.Table;
import jakarta.persistence.*;
@Entity
@Table(name = "subscriptions")
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    private User subscriber;

    @ManyToOne
    private User subscribedTo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSubscriber() {
        return subscriber;
    }

    public void setSubscriber(User subscriber) {
        this.subscriber = subscriber;
    }

    public User getSubscribedTo() {
        return subscribedTo;
    }

    public void setSubscribedTo(User subscribedTo) {
        this.subscribedTo = subscribedTo;
    }
}
