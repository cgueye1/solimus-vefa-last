package com.wakana.vefa.services.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.wakana.vefa.dto.response.MonthlyStatsDTO;
import com.wakana.vefa.dto.response.SubscriptionKpiDto;
import com.wakana.vefa.dto.response.SubscriptionPlanPercentageDTO;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.Invoice;
import com.wakana.vefa.model.SubscriptionPlans;
import com.wakana.vefa.model.Subscriptions;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.InvoiceRepository;
import com.wakana.vefa.repository.SubscriptionsPlanRepository;
import com.wakana.vefa.repository.SubscriptionsRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.InvoiceService;
import com.wakana.vefa.services.SubscriptionServices;
import com.wakana.vefa.util.ProfilEnumUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;

@Service
@Transactional
@RequiredArgsConstructor

public class SubscriptionsServiceImpl implements SubscriptionServices {

    private final SubscriptionsRepository subscriptionRepository;
    private final SubscriptionsPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final InvoiceService invoiceService;
    private final InvoiceRepository invoiceRepository;

    @Transactional
    @Override
    public Subscriptions createSubscription(User user, Long planId, int months) {

        SubscriptionPlans plan = subscriptionPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        double totalCost = plan.getTotalCost() * months;
        if (months >= 12 && plan.getYearlyDiscountRate() > 0) {
            double discountRate = plan.getYearlyDiscountRate() / 100.0;
            totalCost = totalCost  * (1 - discountRate);
        }

        // 🔑 Recherche par USER + PLAN NAME + ACTIF
        Subscriptions sub = subscriptionRepository
                .findByUserIdAndSubscriptionPlans_NameAndActiveTrue(
                        user.getId(),
                        plan.getName())
                .orElseGet(() -> {
                    Subscriptions newSub = new Subscriptions();
                    newSub.setUser(user);
                    newSub.setSubscriptionPlans(plan);
                    newSub.setCurrentProjectCount(0);
                    return newSub;
                });

        // 🔁 Renouvellement ou création
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(months);

        sub.setStartDate(startDate);
        sub.setEndDate(endDate);
        sub.setActive(true);
        sub.setPaidAmount(totalCost);
        sub.setInstallmentCount(months);
        sub.setSubscriptionPlans(plan);
        sub.setRemainingProjects(
                plan.isUnlimitedProjects() ? -1 : plan.getProjectLimit());
        sub.setStatus(PaymentCallStatusEnum.PAID);
        sub.setRenewed(true);
        sub.setDateInvoice(LocalDate.now());

        return subscriptionRepository.save(sub);
    }

    @Override
    public Subscriptions renewSubscription(Long subscriptionId, int months) {
        Subscriptions old = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        old.setRenewed(true);
        old.setEndDate(LocalDate.now().plusMonths(months));
        return subscriptionRepository.save(old);
    }

    @Override
    public boolean isSubscriptionActive(Long userId, String profil) {
        ProfilEnum profilEnum = ProfilEnumUtils.toProfilEnum(profil);

        return subscriptionRepository.existsByUserIdAndSubscriptionPlans_NameAndActiveTrue(userId, profilEnum);
    }

    @Override
    public boolean canCreateProject(Long userId, String profil) {
        ProfilEnum profilEnum = ProfilEnumUtils.toProfilEnum(profil);
        Subscriptions sub = subscriptionRepository
                .findByUserIdAndSubscriptionPlans_NameAndActiveTrue(userId, profilEnum)
                .orElseThrow(() -> new RuntimeException("Active subscription required"));
        return sub.canCreateNewProject();
    }

    @Override
    public void onProjectCreated(Long userId, String profil) {
        ProfilEnum profilEnum = ProfilEnumUtils.toProfilEnum(profil);
        Subscriptions sub = subscriptionRepository
                .findByUserIdAndSubscriptionPlans_NameAndActiveTrue(userId, profilEnum)
                .orElseThrow(() -> new RuntimeException("Active subscription required"));
        sub.incrementProjects();
        subscriptionRepository.save(sub);
    }

    @Override
    public void onProjectDeleted(Long userId, String profil) {

        ProfilEnum profilEnum = ProfilEnumUtils.toProfilEnum(profil);

        Subscriptions sub = subscriptionRepository
                .findByUserIdAndSubscriptionPlans_NameAndActiveTrue(userId, profilEnum)
                .orElseThrow(() -> new RuntimeException("Active subscription required"));
        sub.decrementProjects();
        subscriptionRepository.save(sub);
    }

    public static ProfilEnum toProfilEnum(String profil) {
        try {
            return ProfilEnum.valueOf(profil.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid profil: " + profil);
        }
    }

    @Override
    public Subscriptions payInstallment(Long subscriptionId, double amount) {
        Subscriptions sub = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));
        sub.setPaidAmount(sub.getPaidAmount() + amount);
        if (sub.getPaidAmount() >= sub.getSubscriptionPlans().getTotalCost()) {
            sub.setStatus(PaymentCallStatusEnum.PAID);
        }
        return subscriptionRepository.save(sub);
    }

    @Override
    public List<Subscriptions> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    public Page<Subscriptions> getUserSubscriptionsOrderedByEndDate(Long userId, Pageable pageable) {
        return subscriptionRepository.findByUserIdOrderByEndDateDesc(userId, pageable);
    }

    @Override
    public SubscriptionKpiDto getSubscriptionKpis() {
        List<Subscriptions> allSubs = subscriptionRepository.findAll();
        List<Invoice> allInvoices = invoiceRepository.findAll();

        long totalSubs = allSubs.size();
        long activeSubs = allSubs.stream().filter(Subscriptions::isActive).count();
        long expiredSubs = totalSubs - activeSubs;

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minus(7, ChronoUnit.DAYS);
        LocalDate monthAgo = today.minus(30, ChronoUnit.DAYS);
        LocalDate yearAgo = today.minus(365, ChronoUnit.DAYS);

        long weeklySubs = allSubs.stream().filter(s -> s.getStartDate().isAfter(weekAgo)).count();
        long monthlySubs = allSubs.stream().filter(s -> s.getStartDate().isAfter(monthAgo)).count();
        long yearlySubs = allSubs.stream().filter(s -> s.getStartDate().isAfter(yearAgo)).count();

        double totalRevenue = allInvoices.stream().mapToDouble(Invoice::getAmount).sum();
        double paidRevenue = allInvoices.stream().filter(Invoice::isPaid).mapToDouble(Invoice::getAmount).sum();
        double unpaidRevenue = totalRevenue - paidRevenue;

        double weeklyRevenue = allInvoices.stream()
                .filter(inv -> inv.getCreatedAt().isAfter(LocalDateTime.now().minusWeeks(1)))
                .mapToDouble(Invoice::getAmount).sum();

        double monthlyRevenue = allInvoices.stream()
                .filter(inv -> inv.getCreatedAt().isAfter(LocalDateTime.now().minusMonths(1)))
                .mapToDouble(Invoice::getAmount).sum();

        double yearlyRevenue = allInvoices.stream()
                .filter(inv -> inv.getCreatedAt().isAfter(LocalDateTime.now().minusYears(1)))
                .mapToDouble(Invoice::getAmount).sum();

        double paidPercentage = totalRevenue > 0 ? (paidRevenue / totalRevenue) * 100 : 0;
        double unpaidPercentage = 100 - paidPercentage;

        return new SubscriptionKpiDto(
                totalSubs,
                activeSubs,
                expiredSubs,
                weeklySubs,
                monthlySubs,
                yearlySubs,
                totalRevenue,
                weeklyRevenue,
                monthlyRevenue,
                yearlyRevenue,
                paidRevenue,
                unpaidRevenue,
                paidPercentage,
                unpaidPercentage);
    }

    /*
     * @Override
     * public Subscriptions getSubscriptionByUserId(Long userId) {
     * return subscriptionRepository.findByUserId(userId)
     * .orElseThrow(() -> new
     * RuntimeException("Aucun abonnement trouvé pour cet utilisateur."));
     * }
     */

    public List<MonthlyStatsDTO> getSubscriptionEvolution(Integer year) {

        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        // Récupère les données réelles
        List<Object[]> results = subscriptionRepository.countSubscriptionsByMonth(targetYear);

        // Map mois -> nombre trouvé
        Map<Integer, Long> data = new HashMap<>();
        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            Long total = (Long) row[1];
            data.put(month, total);
        }

        // Liste finale avec les 12 mois (même si = 0)
        List<MonthlyStatsDTO> stats = new ArrayList<>();

        String[] monthNames = {
                "Janvier", "Février", "Mars", "Avril", "Mai", "Juin",
                "Juillet", "Août", "Septembre", "Octobre", "Novembre", "Décembre"
        };

        for (int i = 1; i <= 12; i++) {
            stats.add(new MonthlyStatsDTO(
                    monthNames[i - 1],
                    data.getOrDefault(i, 0L)));
        }

        return stats;
    }

    public List<SubscriptionPlanPercentageDTO> getSubscriptionPlanDistribution() {

        long total = subscriptionRepository.countAllSubscriptions();

        // Récupérer les comptes existants
        List<Object[]> rawData = subscriptionRepository.countSubscriptionsByPlan();

        // Transformer en map pour accès rapide
        Map<ProfilEnum, Long> countMap = new HashMap<>();
        for (Object[] row : rawData) {
            ProfilEnum plan = (ProfilEnum) row[0];
            Long count = (Long) row[1];
            countMap.put(plan, count);
        }

        // Retourner tous les plans sauf WORKER
        List<SubscriptionPlanPercentageDTO> result = new ArrayList<>();

        for (ProfilEnum planEnum : ProfilEnum.values()) {

            if (planEnum == ProfilEnum.RESERVATAIRE)
                continue; // exclure WORKER

            long count = countMap.getOrDefault(planEnum, 0L);

            double percentage = (total == 0) ? 0.0 : (count * 100.0) / total;

            result.add(
                    new SubscriptionPlanPercentageDTO(
                            planEnum.name(),
                            Math.round(percentage * 100.0) / 100.0));
        }

        return result;
    }

    @Override
    public List<Subscriptions> getUserSubscriptions(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    @Override
    public Subscriptions getUserSubscriptionByPlan(Long userId, ProfilEnum planName) {
        return subscriptionRepository
                .findByUserIdAndSubscriptionPlans_Name(userId, planName)
                .orElseThrow(() -> new RuntimeException(
                        "No subscription found for user " + userId + " and plan " + planName));
    }

}
