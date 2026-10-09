package com.wakana.vefa.services;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wakana.vefa.dto.PaymentHistoryRequest;
import com.wakana.vefa.dto.response.PaymentHistoryResponse;

public interface PaymentHistoryService {

    void createHistory(PaymentHistoryRequest request);

    void markAsPaid(Long userId, Long planId);

    Page<PaymentHistoryResponse> list(Pageable pageable, String keyword);
}
