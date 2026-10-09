package com.wakana.vefa.services;

import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.model.Subscription;
import com.wakana.vefa.model.User;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SubscriptionService {

    Subscription createSubscription(User user, Long planId);

    Subscription getSubscriptionByUserId(Long userId);

    List<Subscription> getAllSubscriptions();

    Subscription payInstallment(Long subscriptionId, double amount);

    void cancelSubscription(Long subscriptionId);

    boolean isSubscriptionActive(Long userId);

    boolean hasActiveSubscription(User user);

    Subscription renewSubscription(Long userId, double totalCost, int installmentCount);

    public Page<Subscription> getSubscriptions(Long userId, PaymentCallStatusEnum status, Pageable pageable);

    //
    Subscription processPayment(Long subscriptionId, int installmentCount);

    void renewSubscription(Subscription oldSubscription);

    Page<Subscription> getUserSubscriptionsOrderedByEndDate(Long userId, Pageable pageable);

    boolean hasActiveSubscription(Long userId);

    boolean hasActivePaidSubscription(Long userId);

    boolean isSubscriptionPaid(Long subscriptionId);
    Map<String, Object> getRevenueByMonth(Integer year);
}
