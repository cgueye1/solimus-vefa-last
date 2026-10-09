package com.wakana.vefa.services.impl;

import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.model.Subscription;
import com.wakana.vefa.model.SubscriptionPlan;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.SubscriptionRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.SubscriptionPlanService;
import com.wakana.vefa.services.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Service
@Transactional
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanService subscriptionPlanService;
    private final UserRepository userRepository;

    @Override
    public boolean hasActiveSubscription(User user) {
        Subscription subscription = subscriptionRepository.findByUserId(user.getId())
                .orElse(null);

        if (subscription == null) {
            return false;
        }

        // Check if the subscription has expired (date de fin dépassée)
        if (LocalDate.now().isAfter(subscription.getEndDate())) {
            subscription.setActive(false); // Désactive l'abonnement si la date de fin est dépassée
            subscriptionRepository.save(subscription); // Sauvegarde la désactivation
            return false;
        }

        return subscription.isActive();
    }

    @Override
    public Subscription createSubscription(User user, Long planId) {
        SubscriptionPlan plan = subscriptionPlanService.getPlanById(planId); // Récupère le plan choisi

        Subscription subscription = new Subscription();
        subscription.setUser(user);
        subscription.setSubscriptionPlan(plan); // Associe l'abonnement au plan choisi
        subscription.setInstallmentCount(plan.getInstallmentCount());
        subscription.setPaidAmount(0.0);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(LocalDate.now().plusMonths(plan.getInstallmentCount())); // Durée calculée en fonction
                                                                                         // des tranches
        subscription.setActive(false); // Abonnement activé lors de la création
        subscription.setStatus(PaymentCallStatusEnum.PENDING);
        // Génération de la référence unique
        subscription.setReference(generateReference());

        return subscriptionRepository.save(subscription);
    }

    private String generateReference() {
        int randomNum = (int) (Math.random() * 9000) + 1000; // Génère un nombre entre 1000 et 9999
        int year = LocalDate.now().getYear();
        return "SOLIMUS-" + randomNum + "-" + year;
    }

    @Override
    public Subscription getSubscriptionByUserId(Long userId) {
        return subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No subscription found for userId: " + userId));
    }

    @Override
    public List<Subscription> getAllSubscriptions() {
        return subscriptionRepository.findAll();
    }

    @Override
    public Subscription payInstallment(Long subscriptionId, double amount) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionId));

        if (!subscription.isActive()) {
            throw new RuntimeException("Cannot pay for an inactive subscription.");
        }

        // Mise à jour du montant payé
        double newPaidAmount = subscription.getPaidAmount() + amount;
        if (newPaidAmount > subscription.getSubscriptionPlan().getTotalCost()) {
            throw new RuntimeException("Paid amount exceeds the total subscription cost.");
        }

        subscription.setPaidAmount(newPaidAmount);

        // Si toutes les tranches sont payées, on désactive l'abonnement
        if (newPaidAmount >= subscription.getSubscriptionPlan().getTotalCost()) {
            subscription.setActive(true);
        }

        return subscriptionRepository.save(subscription);
    }

    @Override
    public void cancelSubscription(Long subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with id: " + subscriptionId));

        // L'abonnement est désactivé mais les paiements peuvent continuer si nécessaire
        subscription.setActive(!subscription.isActive());
        if (PaymentCallStatusEnum.PAID == subscription.getStatus() && !subscription.isActive()) {
            subscription.setStatus(PaymentCallStatusEnum.PENDING);

        }
        subscriptionRepository.save(subscription);
    }

    @Override
    public boolean isSubscriptionActive(Long userId) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No subscription found for userId: " + userId));

        // On vérifie si l'abonnement est actif (ni expiré ni totalement payé)
        return subscription.isActive();
    }

    @Override
    public Subscription renewSubscription(Long userId, double totalCost, int installmentCount) {
        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("No subscription found for userId: " + userId));

        // Si l'abonnement a expiré, on le renouvelle
        if (LocalDate.now().isAfter(subscription.getEndDate()) || !subscription.isActive()) {
            subscription.setPaidAmount(0.0); // Remet à zéro les paiements
            subscription.setInstallmentCount(installmentCount); // Nombre de tranches
            subscription.setStartDate(LocalDate.now()); // Date de début du renouvellement
            subscription.setEndDate(LocalDate.now().plusMonths(installmentCount)); // Nouvelle date de fin
            subscription.setActive(false); // Abonnement réactivé
        } else {
            throw new RuntimeException("The subscription is still active and cannot be renewed.");
        }

        return subscriptionRepository.save(subscription);
    }

    //////
    public Subscription processPayment(Long subscriptionId, int installmentCount) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        double totalCostByMonth = subscription.getSubscriptionPlan().getTotalCost();
        double paidAmount = totalCostByMonth * installmentCount;

      

        // if (installmentCount > 0) {
        subscription.setPaidAmount(paidAmount);
        subscription.setStatus(PaymentCallStatusEnum.PAID);
        subscription.setDateInvoice(LocalDate.now());
        subscription.setInstallmentCount(installmentCount);
        subscription.setActive(true);
        // }

        /*
         * else {
         * subscription.setStatus(PaymentCallStatusEnum.PENDING);
         * }
         */

        Subscription saved = subscriptionRepository.save(subscription);
      
        return saved;
    }

    public void renewSubscription(Subscription oldSubscription) {
        Subscription newSubscription = new Subscription();
        newSubscription.setUser(oldSubscription.getUser());
        newSubscription.setSubscriptionPlan(oldSubscription.getSubscriptionPlan());
        newSubscription.setInstallmentCount(oldSubscription.getInstallmentCount());

        // Calcul des nouvelles dates
        LocalDate newStartDate = oldSubscription.getEndDate().plusDays(1);
        LocalDate newEndDate = newStartDate.plusMonths(oldSubscription.getInstallmentCount());
        newSubscription.setStartDate(newStartDate);
        newSubscription.setEndDate(newEndDate);

        newSubscription.setActive(false);
        newSubscription.setPaidAmount(0.0);
        newSubscription.setStatus(PaymentCallStatusEnum.PENDING);

        subscriptionRepository.save(newSubscription);
    }

    public Page<Subscription> getUserSubscriptionsOrderedByEndDate(Long userId, Pageable pageable) {
        return subscriptionRepository.findByUserIdOrderByEndDateDesc(userId, pageable);
    }

    /// admin
    public Page<Subscription> getSubscriptions(Long userId, PaymentCallStatusEnum status, Pageable pageable) {

        return subscriptionRepository.findByUserIdAndStatusOrderByEndDateDesc(userId, status, pageable);
    }

    public boolean hasActiveSubscription(Long userId) {
        return userRepository.hasActiveSubscription(userId);
    }

    @Override
    public boolean hasActivePaidSubscription(Long userId) {
        return subscriptionRepository.existsActivePaidSubscription(userId, PaymentCallStatusEnum.PAID);
    }

    @Override
    public boolean isSubscriptionPaid(Long subscriptionId) {
        Optional<Subscription> optional = subscriptionRepository.findById(subscriptionId);
        if (optional.isEmpty()) {
            return false;
        }

        Subscription subscription = optional.get();
        return subscription.getStatus() == PaymentCallStatusEnum.PAID;
    }
    
    
    @Override
public Map<String, Object> getRevenueByMonth(Integer year) {

    int targetYear = (year != null)
            ? year
            : LocalDate.now().getYear();

    List<Object[]> results =
            subscriptionRepository.sumRevenueGroupedByMonth(targetYear);

    Map<Integer, Double> revenueByMonth = new HashMap<>();
    double totalRevenue = 0;

    for (Object[] row : results) {
        int month = ((Number) row[0]).intValue();
        double amount = ((Number) row[1]).doubleValue();
        revenueByMonth.put(month, amount);
        totalRevenue += amount;
    }

    String[] months = {
            "Jan", "Fév", "Mar", "Avr", "Mai", "Juin",
            "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"
    };

    List<Map<String, Object>> monthlyRevenue = new ArrayList<>();

    for (int i = 1; i <= 12; i++) {
        Map<String, Object> item = new HashMap<>();
        item.put("name", months[i - 1]);
        item.put("value", revenueByMonth.getOrDefault(i, 0.0));
        monthlyRevenue.add(item);
    }

    Map<String, Object> response = new HashMap<>();
    response.put("year", targetYear);
    response.put("totalRevenue", totalRevenue);
    response.put("monthlyRevenue", monthlyRevenue);

    return response;
}

    
    
}
