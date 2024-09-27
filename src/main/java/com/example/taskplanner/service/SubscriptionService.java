package com.example.taskplanner.service;

import com.example.taskplanner.entities.Subscription;
import com.example.taskplanner.entities.User;

import java.util.List;

public interface SubscriptionService {
    List<Subscription> getSubscriptionsBySubscriberId(Long userId);

    List<User> findAllUsersExcludingAdminAndCurrent(Long currentUserId);

    boolean isSubscribed(Long subscriberId, Long subscribedToId);

    void subscribe(Long userId, Long subscribedToId);

    void unsubscribe(Long userId, Long subscribedToId);
}
