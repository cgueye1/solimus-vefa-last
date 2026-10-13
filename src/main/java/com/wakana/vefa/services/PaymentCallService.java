package com.wakana.vefa.services;

import com.wakana.vefa.dto.PaymentCallRequest;
import com.wakana.vefa.dto.response.CallForChargeKpiResponse;
import com.wakana.vefa.dto.response.PaymentCallKpiDto;
import com.wakana.vefa.model.PaymentCall;
import java.util.List;
import java.util.Optional;
import java.util.Map;

public interface PaymentCallService {

    List<PaymentCall> getPaymentCallsForProperty(Long propertyId);

    List<PaymentCall> getPaymentCallsForRecipient(Long recipientId);

    Optional<PaymentCall> getPaymentCall(Long id);

    PaymentCall createPaymentCall(PaymentCallRequest paymentCallRequest);

    Optional<PaymentCall> updatePaymentCall(Long id, PaymentCallRequest paymentCallRequest);

    PaymentCall addFile(Long id, PaymentCallRequest paymentCallRequest);

    void deletePaymentCall(Long id);

    Map<String, Object> simulatePayments(Long propertyId);

    CallForChargeKpiResponse getKpiByPromoter(Long promoterId, String planName);

    CallForChargeKpiResponse getKpiByParentProperty(Long propertyId);

    PaymentCall updateStatus(Long paymentCallId, String status);

    PaymentCallKpiDto getPaymentCallKpis(Long propertyId);

}
