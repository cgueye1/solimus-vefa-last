package com.wakana.vefa.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wakana.vefa.dto.PaymentHistoryRequest;
import com.wakana.vefa.dto.response.PaymentHistoryResponse;
import com.wakana.vefa.model.PaymentHistory;
import com.wakana.vefa.model.SubscriptionPlans;
import com.wakana.vefa.model.Subscriptions;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.PaymentHistoryRepository;
import com.wakana.vefa.repository.SubscriptionsPlanRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.InvoiceService;
import com.wakana.vefa.services.PaymentHistoryService;
import com.wakana.vefa.services.SubscriptionServices;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentHistoryServiceImpl implements PaymentHistoryService {

    private final PaymentHistoryRepository repository;
    private final UserRepository userRepository;
    private final SubscriptionsPlanRepository planRepository;
    private final SubscriptionServices subscriptionService;
    private final InvoiceService invoiceService;

    @Override
    public void createHistory(PaymentHistoryRequest request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        SubscriptionPlans plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        PaymentHistory history = new PaymentHistory();
        history.setUser(user);
        history.setPlan(plan);
        history.setAmount(request.getAmount());
        history.setInstallmentCount(request.getInstallmentCount());
        history.setNumber(request.getNumber());
        history.setPaid(false);

        repository.save(history);
    }

    @Override
    public void markAsPaid(Long userId, Long planId) {

        PaymentHistory history = repository.findLastByUserAndPlan(userId, planId)
                .orElseThrow(() -> new RuntimeException("No payment history found"));

        Subscriptions subscription = subscriptionService.createSubscription(history.getUser(), planId,
                history.getInstallmentCount());
        if (subscription != null) {
            invoiceService.generateInvoice(
                    history.getUser(),
                    subscription,
                    subscription.getPaidAmount(),
                    history.getNumber());

            history.setPaid(true);
            repository.save(history);
        }
    }

    @Override
    public Page<PaymentHistoryResponse> list(Pageable pageable, String keyword) {

        return repository.search(pageable, keyword)
                .map(p -> {
                    PaymentHistoryResponse dto = new PaymentHistoryResponse();
                    dto.setNom(p.getUser().getNom());
                    dto.setPrenom(p.getUser().getPrenom());
                    dto.setPlanLabel(p.getPlan().getLabel());
                    dto.setAmount(p.getAmount());
                    dto.setPaid(p.isPaid());
                    dto.setDate(p.getCreatedAt());
                    dto.setNumber(p.getNumber());
                    dto.setUserId(p.getUser().getId());
                    dto.setPlanId(p.getPlan().getId());
                    return dto;
                });
    }
}
