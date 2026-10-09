package com.wakana.vefa.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.wakana.vefa.dto.PaymentCallRequest;
import com.wakana.vefa.dto.response.CallForChargeKpiResponse;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.model.CallForCharge;
import com.wakana.vefa.model.PaymentCall;

public interface CallForChargeService {

        public Page<CallForCharge> getPendingCallsByOwner(Long ownerId, Pageable pageable);

        public Page<CallForCharge> getPaidCallsByOwner(Long ownerId, Pageable pageable);

        public Page<CallForCharge> getCallsByOwnerAndProperty(Long ownerId, Long propertyId, Pageable pageable);

        public List<CallForCharge> getEarliestPendingCallsByOwner(Long ownerId);

        public double getPendingFeesForOwner(Long ownerId);

        public Page<CallForCharge> getPendingCallsByOwnerAndProprety(Long ownerId, Long pId, Pageable pageable);

        public Page<CallForCharge> getPaidCallsByOwnerAndProprety(Long ownerId, Long pId, Pageable pageable);

        CallForChargeKpiResponse getKpiByPromoter(Long promoterId);

        CallForChargeKpiResponse getKpiByParentProperty(Long propertyId);

        CallForCharge updateStatus(Long callId, PaymentCallStatusEnum newStatus);

        double getTotalPaidByOwner(Long ownerId);

        CallForCharge addFile(Long id, PaymentCallRequest paymentCallRequest);

        CallForCharge updateStatus(Long callForChargeId, String status);

}
