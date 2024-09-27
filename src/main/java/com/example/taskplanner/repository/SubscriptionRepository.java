package com.example.taskplanner.repository;

import com.example.taskplanner.entities.Subscription;
import com.example.taskplanner.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findBySubscriberId(Long subscriberId);

    List<Subscription> findBySubscriber(User creator);

}
