package com.wakana.vefa.services;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wakana.vefa.dto.response.InvoiceResponseDto;
import com.wakana.vefa.dto.response.MonthlyRevenueDTO;
import com.wakana.vefa.dto.response.MonthlyRevenueResponseDTO;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.Invoice;
import com.wakana.vefa.model.Subscriptions;
import com.wakana.vefa.model.User;

public interface InvoiceService {
    Invoice generateInvoice(User user, Subscriptions subscription, double amount, String paymentMethod);

    Page<Invoice> getInvoicesByUser(Long userId, Pageable pageable);

    MonthlyRevenueResponseDTO getMonthlyRevenueEvolution(Integer year);

    Page<InvoiceResponseDto> getInvoices(Pageable pageable);
    
    
     Page<Invoice> getInvoicesByUserAndPlan(
            Long userId,
            ProfilEnum planName,
            Pageable pageable
    );
}
