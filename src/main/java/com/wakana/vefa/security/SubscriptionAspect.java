package com.wakana.vefa.security;

import com.wakana.vefa.model.User;
import com.wakana.vefa.services.SubscriptionService;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SubscriptionAspect {

    private final SubscriptionService subscriptionService;

    public SubscriptionAspect(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @Before("@annotation(com.wakana.vefa.security.CheckSubscription)")
    public void checkSubscription() {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Check if the user has an active subscription
        if (!subscriptionService.hasActiveSubscription(user)) {
            throw new IllegalStateException("Your subscription has expired or is inactive. Please renew your subscription.");
        }
    }
}
