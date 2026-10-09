package com.wakana.vefa.services.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.wakana.vefa.dto.response.ChartValueDTO;
import com.wakana.vefa.dto.response.InvoiceResponseDto;
import com.wakana.vefa.dto.response.MonthlyRevenueDTO;
import com.wakana.vefa.dto.response.MonthlyRevenueResponseDTO;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.Invoice;
import com.wakana.vefa.model.Subscriptions;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.InvoiceRepository;
import com.wakana.vefa.services.InvoiceService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;

    @Override
    @Transactional
    public Invoice generateInvoice(User user, Subscriptions subscription, double amount, String paymentMethod) {

        Optional<Invoice> existingInvoice = invoiceRepository
                .findTopBySubscription_IdOrderByIdDesc(subscription.getId());

        // éviter génération multiple dans un court délai (ex: 10 secondes)
        if (existingInvoice.isPresent()) {
            Invoice last = existingInvoice.get();

            if (last.getCreatedAt() != null &&
                    last.getCreatedAt().isAfter(LocalDateTime.now().minusSeconds(10))) {
                return last;
            }
        }

        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        invoice.setUser(user);
        invoice.setSubscription(subscription);
        invoice.setAmount(amount);
        invoice.setPaid(true);
        invoice.setPaymentMethod(paymentMethod);
        invoice.setCreatedAt(LocalDateTime.now());

        return invoiceRepository.save(invoice);
    }

    @Override
    public Page<Invoice> getInvoicesByUser(Long userId, Pageable pageable) {
        return invoiceRepository.findByUserId(userId, pageable);
    }

    @Override
    public MonthlyRevenueResponseDTO getMonthlyRevenueEvolution(Integer year) {

        int targetYear = (year != null) ? year : LocalDate.now().getYear();

        List<Object[]> results = invoiceRepository.getMonthlyRevenues(targetYear);

        Map<Integer, Double> map = new HashMap<>();
        double totalRevenue = 0;

        for (Object[] row : results) {
            Integer month = (Integer) row[0];
            Double total = (Double) row[1];
            map.put(month, total);
            totalRevenue += total;
        }

        String[] months = {
                "Jan", "Fév", "Mar", "Avr", "Mai", "Juin",
                "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"
        };

        List<ChartValueDTO> monthlyRevenue = new ArrayList<>();

        for (int i = 1; i <= 12; i++) {
            monthlyRevenue.add(
                    new ChartValueDTO(
                            months[i - 1],
                            map.getOrDefault(i, 0.0)));
        }

        return new MonthlyRevenueResponseDTO(
                targetYear,
                totalRevenue,
                monthlyRevenue);
    }

    @Override
    public Page<InvoiceResponseDto> getInvoices(Pageable pageable) {
        return invoiceRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(InvoiceResponseDto::fromEntity); // utilise ton DTO avec formatage et labels
    }

    @Override
    public Page<Invoice> getInvoicesByUserAndPlan(
            Long userId,
            ProfilEnum planName,
            Pageable pageable) {
        return invoiceRepository
                .findBySubscription_User_IdAndSubscription_SubscriptionPlans_Name(
                        userId,
                        planName,
                        pageable);
    }
}
