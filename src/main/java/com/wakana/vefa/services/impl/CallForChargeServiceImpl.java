package com.wakana.vefa.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import com.wakana.vefa.dto.PaymentCallRequest;
import com.wakana.vefa.dto.response.CallForChargeKpiResponse;
import com.wakana.vefa.enums.PaymentCallStatusEnum;

import com.wakana.vefa.model.CallForCharge;
import com.wakana.vefa.model.PaymentCall;
import com.wakana.vefa.repository.CallForChargeRepository;

import com.wakana.vefa.services.CallForChargeService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CallForChargeServiceImpl implements CallForChargeService {
    @Autowired
    private CallForChargeRepository callForChargeRepository;

    public Page<CallForCharge> getPendingCallsByOwner(Long ownerId, Pageable pageable) {
        return callForChargeRepository.findByOwnerIdAndStatusOrderByExpectedDateAsc(ownerId,
                PaymentCallStatusEnum.PENDING, pageable);
    }

    public Page<CallForCharge> getPaidCallsByOwner(Long ownerId, Pageable pageable) {
        return callForChargeRepository.findByOwnerIdAndStatus(ownerId, PaymentCallStatusEnum.PAID, pageable);
    }

    public Page<CallForCharge> getCallsByOwnerAndProperty(Long ownerId, Long propertyId, Pageable pageable) {
        return callForChargeRepository.findByOwnerIdAndRealEstatePropertyId(ownerId, propertyId, pageable);
    }

    public List<CallForCharge> getEarliestPendingCallsByOwner(Long ownerId) {
        return callForChargeRepository.findEarliestPendingCallsByOwner(ownerId, PaymentCallStatusEnum.PENDING.name());
    }

    public double getPendingFeesForOwner(Long ownerId) {
        return callForChargeRepository.getTotalPendingFeesByOwnerId(ownerId);
    }

    @Override
    public Page<CallForCharge> getPendingCallsByOwnerAndProprety(Long ownerId, Long pId, Pageable pageable) {
        return callForChargeRepository.findByOwnerIdAndStatusAndRealEstatePropertyId(ownerId,
                PaymentCallStatusEnum.PENDING, pId, pageable);
    }

    @Override
    public Page<CallForCharge> getPaidCallsByOwnerAndProprety(Long ownerId, Long pId, Pageable pageable) {
        return callForChargeRepository.findByOwnerIdAndStatusAndRealEstatePropertyId(ownerId,
                PaymentCallStatusEnum.PAID, pId, pageable);

    }

    public CallForChargeKpiResponse getKpiByPromoter(Long promoterId) {
        double total = Optional.ofNullable(callForChargeRepository.getTotalFeeByPromoter(promoterId)).orElse(0.0);
        double paid = Optional.ofNullable(callForChargeRepository.getPaidFeeByPromoter(promoterId)).orElse(0.0);
        double unpaid = Optional.ofNullable(callForChargeRepository.getUnpaidFeeByPromoter(promoterId)).orElse(0.0);
        return new CallForChargeKpiResponse(total, paid, unpaid);
    }

    public CallForChargeKpiResponse getKpiByParentProperty(Long propertyId) {
        double total = Optional.ofNullable(callForChargeRepository.getTotalFeeByParentProperty(propertyId)).orElse(0.0);
        double paid = Optional.ofNullable(callForChargeRepository.getPaidFeeByParentProperty(propertyId)).orElse(0.0);
        double unpaid = Optional.ofNullable(callForChargeRepository.getUnpaidFeeByParentProperty(propertyId))
                .orElse(0.0);
        return new CallForChargeKpiResponse(total, paid, unpaid);
    }

    @Override
    public CallForCharge updateStatus(Long callId, PaymentCallStatusEnum newStatus) {
        CallForCharge call = callForChargeRepository.findById(callId)
                .orElseThrow(() -> new RuntimeException("CallForCharge not found with id " + callId));

        call.setStatus(newStatus);

        if (newStatus == PaymentCallStatusEnum.PAID) {
            call.setPaymentDate(System.currentTimeMillis()); // date actuelle en timestamp
        } else {
            call.setPaymentDate(null); // reset si autre status
        }

        call.setUpdatedAt(System.currentTimeMillis());

        return callForChargeRepository.save(call);
    }

    public double getTotalPaidByOwner(Long ownerId) {
        return callForChargeRepository.sumPaidByOwner(ownerId);
    }

    @Override
    public CallForCharge addFile(Long id, PaymentCallRequest paymentCallRequest) {
        CallForCharge paymentCall = callForChargeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));

        paymentCall.setFile(paymentCallRequest.getFileName());

        return callForChargeRepository.save(paymentCall);
    }

    @Override
    public CallForCharge updateStatus(Long callForChargeId, String status) {

        CallForCharge callForCharge = callForChargeRepository.findById(callForChargeId)
                .orElseThrow(() -> new RuntimeException("CallForCharge introuvable"));

        PaymentCallStatusEnum statusEnum;

        try {
            statusEnum = PaymentCallStatusEnum.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Statut invalide. Valeurs possibles : " +
                            Arrays.toString(PaymentCallStatusEnum.values()));
        }

        // Optionnel : règle métier
        if (callForCharge.getStatus() == PaymentCallStatusEnum.PAID) {
            throw new RuntimeException("Un appel de charges déjà payé ne peut pas être modifié");
        }

        callForCharge.setStatus(statusEnum);
        callForCharge.setUpdatedAt(System.currentTimeMillis());

        // Si payé → renseigner la date de paiement
        if (statusEnum == PaymentCallStatusEnum.PAID) {
            callForCharge.setPaymentDate(System.currentTimeMillis());
        }

        return callForChargeRepository.save(callForCharge);
    }

}
