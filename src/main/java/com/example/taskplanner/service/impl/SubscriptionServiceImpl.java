package com.example.taskplanner.service.impl;

import com.example.taskplanner.entities.Subscription;
import com.example.taskplanner.entities.User;
import com.example.taskplanner.entities.enums.Role;
import com.example.taskplanner.repository.SubscriptionRepository;
import com.example.taskplanner.repository.UserRepository;
import com.example.taskplanner.service.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Autowired
    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository, UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
    }

    @Override
    public List<Subscription> getSubscriptionsBySubscriberId(Long subscriberId) {
        return subscriptionRepository.findBySubscriberId(subscriberId);
    }

    @Override
    public void subscribe(Long subscriberId, Long subscribedToId) {
        User subscriber = userRepository.findById(subscriberId).orElseThrow(() -> new IllegalArgumentException("Subscriber with id " + subscriberId + " not found"));
        User subscribedTo = userRepository.findById(subscribedToId).orElseThrow(() -> new IllegalArgumentException("User to subscribe with id " + subscribedToId + " not found"));

        Subscription subscription = new Subscription();
        subscription.setSubscriber(subscriber);
        subscription.setSubscribedTo(subscribedTo);

        subscriptionRepository.save(subscription);
    }

    @Override
    public void unsubscribe(Long subscriberId, Long subscribedToId) {
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriberId(subscriberId);
        for (Subscription subscription : subscriptions) {
            if (subscription.getSubscribedTo().getId().equals(subscribedToId)) {
                subscriptionRepository.delete(subscription);
            }
        }
    }

    @Override
    public List<User> findAllUsersExcludingAdminAndCurrent(Long currentUserId) {
        return userRepository.findAll().stream()
                .filter(user -> !user.getId().equals(currentUserId) && user.getRole() != Role.admin)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isSubscribed(Long subscriberId, Long subscribedToId) {
        List<Subscription> subscriptions = subscriptionRepository.findBySubscriberId(subscriberId);
        return subscriptions.stream()
                .anyMatch(subscription -> subscription.getSubscribedTo().getId().equals(subscribedToId));
    }

}
