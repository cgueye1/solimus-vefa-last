package com.wakana.vefa.services;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wakana.vefa.dto.response.MonthlyStatsDTO;
import com.wakana.vefa.dto.response.SubscriptionKpiDto;
import com.wakana.vefa.dto.response.SubscriptionPlanPercentageDTO;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.Subscriptions;
import com.wakana.vefa.model.User;

import java.time.LocalDate;
import java.util.List;

public interface SubscriptionServices {

    Subscriptions createSubscription(User user, Long planId, int months);

    Subscriptions renewSubscription(Long subscriptionId, int months);

    boolean isSubscriptionActive(Long userId, String profil);

    boolean canCreateProject(Long userId, String profil);

    void onProjectCreated(Long userId, String profil);

    void onProjectDeleted(Long userId, String profil);

    Subscriptions payInstallment(Long subscriptionId, double amount);

    List<Subscriptions> getAllSubscriptions();

    Page<Subscriptions> getUserSubscriptionsOrderedByEndDate(Long userId, Pageable pageable);

    SubscriptionKpiDto getSubscriptionKpis();

   // Subscriptions getSubscriptionByUserId(Long userId);

    List<MonthlyStatsDTO> getSubscriptionEvolution(Integer year);

    List<SubscriptionPlanPercentageDTO> getSubscriptionPlanDistribution();

    // 🔹 Liste des abonnements d’un user
    List<Subscriptions> getUserSubscriptions(Long userId);

    // 🔹 Abonnement d’un user par plan
    Subscriptions getUserSubscriptionByPlan(Long userId, ProfilEnum planName);

}
