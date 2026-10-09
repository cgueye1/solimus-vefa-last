package com.wakana.vefa.services.impl;

import com.wakana.vefa.dto.PaymentCallRequest;
import com.wakana.vefa.dto.response.CallForChargeKpiResponse;
import com.wakana.vefa.dto.response.PaymentCallKpiDto;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.PaymentCall;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.repository.PaymentCallRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.services.LamSmsService;
import com.wakana.vefa.services.NotificationsService;
import com.wakana.vefa.services.PaymentCallPdfService;
import com.wakana.vefa.services.PaymentCallService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PaymentCallServiceImpl implements PaymentCallService {

        private final PaymentCallRepository paymentCallRepository;
        private final RealEstatePropertyRepository realEstatePropertyRepository;

        private final NotificationsService notificationsService;

        private final LamSmsService lamSmsService;

        private final PaymentCallPdfService paymentCallPdfService;

        @Override
        public List<PaymentCall> getPaymentCallsForProperty(Long propertyId) {
                return paymentCallRepository.findByRealEstatePropertyIdOrderByCreatedAtDesc(propertyId);
        }

        @Override
        public PaymentCall createPaymentCall(PaymentCallRequest request) {

                RealEstateProperty property = realEstatePropertyRepository
                                .findById(request.getRealEstatePropertyId())
                                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

                // Calcul de la somme des montants et pourcentages existants (tous appels >0)
                List<PaymentCall> existingCalls = paymentCallRepository
                                .findByRealEstatePropertyId(property.getId())
                                .stream()
                                .filter(pc -> pc.getPercentage() > 0)
                                .toList();

                double totalAmount = existingCalls.stream()
                                .mapToDouble(PaymentCall::getAmount)
                                .sum() + request.getAmount(); // inclure le nouvel appel

                double totalPercentage = existingCalls.stream()
                                .mapToDouble(PaymentCall::getPercentage)
                                .sum() + request.getPercentage();

                if (totalAmount > property.getPrice()) {
                        throw new IllegalArgumentException(
                                        "Le montant total des appels de fonds dépasse le prix du bien ("
                                                        + property.getPrice() + " CFA).");
                }

                if (totalPercentage > 100) {
                        throw new IllegalArgumentException("La somme des pourcentages dépasse 100%.");
                }

                // Création du nouvel appel
                PaymentCall paymentCall = new PaymentCall();
                paymentCall.setLabel(request.getLabel());
                paymentCall.setPercentage(request.getPercentage());
                paymentCall.setExpectedDate(request.getExpectedDate());
                paymentCall.setStatus(PaymentCallStatusEnum.PENDING);
                paymentCall.setRealEstateProperty(property);
                paymentCall.setCreatedAt(System.currentTimeMillis());
                paymentCall.setUpdatedAt(System.currentTimeMillis());
                paymentCall.setBaseAmount(request.getAmount());
                paymentCall.setRef(request.getRef());
                paymentCall.setTom(request.getTom());
                paymentCall.setTva(request.getTva());

                // Calcul montant final avec TVA/TOM
                double finalAmount = calculateFinalAmount(
                                request.getAmount(),
                                request.getTva(),
                                request.getTom());
                paymentCall.setAmount(finalAmount);

                // Notification si destinataire défini
                if (property.getRecipient() != null) {
                        notificationsService.sendReservationNotif(
                                        property.getName(),
                                        request.getLabel(),
                                        property,
                                        property.getRecipient());

                        String message = request.getLabel() + "\n"
                                        + "Montant : " + finalAmount + " CFA\n"
                                        + "Date prévue : " + request.getExpectedDate();

                        // lamSmsService.sendSms(property.getRecipient().getTelephone(), message);
                }

                PaymentCall savedPaymentCall = paymentCallRepository.save(paymentCall);

                paymentCallPdfService.sendPaymentCallByEmail(savedPaymentCall);

                return savedPaymentCall;
        }

        @Override
        public Optional<PaymentCall> updatePaymentCall(Long id, PaymentCallRequest request) {

                return paymentCallRepository.findById(id).map(existing -> {

                        RealEstateProperty property = realEstatePropertyRepository
                                        .findById(request.getRealEstatePropertyId())
                                        .orElseThrow(() -> new IllegalArgumentException("Property not found"));

                        // Calcul des totaux en incluant le nouvel appel mis à jour
                        List<PaymentCall> otherCalls = paymentCallRepository
                                        .findByRealEstatePropertyId(property.getId())
                                        .stream()
                                        .filter(pc -> pc.getPercentage() > 0 && !pc.getId().equals(existing.getId()))
                                        .toList();

                        double totalAmount = otherCalls.stream()
                                        .mapToDouble(PaymentCall::getAmount)
                                        .sum() + request.getAmount();

                        double totalPercentage = otherCalls.stream()
                                        .mapToDouble(PaymentCall::getPercentage)
                                        .sum() + request.getPercentage();

                        if (totalAmount > property.getPrice()) {
                                throw new IllegalArgumentException(
                                                "Le montant total des appels de fonds dépasse le prix du bien ("
                                                                + property.getPrice() + " CFA).");
                        }

                        if (totalPercentage > 100) {
                                throw new IllegalArgumentException("La somme des pourcentages dépasse 100%.");
                        }

                        // Mise à jour
                        existing.setLabel(request.getLabel());
                        existing.setPercentage(request.getPercentage());
                        existing.setExpectedDate(request.getExpectedDate());
                        existing.setStatus(PaymentCallStatusEnum.valueOf(request.getStatus()));
                        existing.setUpdatedAt(System.currentTimeMillis());
                        existing.setTom(request.getTom());
                        existing.setTva(request.getTva());
                        existing.setBaseAmount(request.getAmount());
                        existing.setRef(request.getRef());

                        double finalAmount = calculateFinalAmount(
                                        request.getAmount(),
                                        request.getTva(),
                                        request.getTom());
                        existing.setAmount(finalAmount);

                        return paymentCallRepository.save(existing);
                });
        }

        public Map<String, Object> simulatePayments(Long propertyId) {

                // Vérifier que le bien existe
                RealEstateProperty property = realEstatePropertyRepository.findById(propertyId)
                                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

                // Récupérer les appels de fonds en retard
                List<PaymentCall> overduePayments = paymentCallRepository
                                .findOverdueByRealEstatePropertyId(propertyId);

                // Garder uniquement ceux qui ne sont pas PAYÉS
                List<PaymentCall> unpaidPayments = overduePayments.stream()
                                .filter(payment -> payment.getStatus() != PaymentCallStatusEnum.PAID)
                                .toList();

                // Calcul du montant total
                double totalAmount = unpaidPayments.stream()
                                .mapToDouble(PaymentCall::getAmount)
                                .sum();

                // Construire la réponse
                Map<String, Object> result = new HashMap<>();
                result.put("overduePayments", unpaidPayments);
                result.put("totalAmount", totalAmount);

                return result;
        }

        @Override
        public void deletePaymentCall(Long id) {
                paymentCallRepository.deleteById(id);
        }

        @Override
        public PaymentCall addFile(Long id, PaymentCallRequest paymentCallRequest) {
                PaymentCall paymentCall = paymentCallRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));

                paymentCall.setFile(paymentCallRequest.getFileName());

                return paymentCallRepository.save(paymentCall);
        }

        public CallForChargeKpiResponse getKpiByPromoter(Long promoterId, String planName) {
                // Convertir le plan en enum
                ProfilEnum planEnum = ProfilEnum.valueOf(planName.toUpperCase());

                // Total
                double total = Optional.ofNullable(
                                paymentCallRepository.getTotalAmountByPromoterAndPlan(promoterId, planEnum))
                                .orElse(0.0);

                // Payé
                double paid = Optional.ofNullable(
                                paymentCallRepository.getPaidAmountByPromoterAndPlan(promoterId, planEnum)).orElse(0.0);

                // Non payé
                double unpaid = Optional.ofNullable(
                                paymentCallRepository.getUnpaidAmountByPromoterAndPlan(promoterId, planEnum))
                                .orElse(0.0);

                return new CallForChargeKpiResponse(total, paid, unpaid);
        }

        public CallForChargeKpiResponse getKpiByParentProperty(Long propertyId) {
                double total = Optional.ofNullable(paymentCallRepository.getTotalAmountByParentProperty(propertyId))
                                .orElse(0.0);
                double paid = Optional.ofNullable(paymentCallRepository.getPaidAmountByParentProperty(propertyId))
                                .orElse(0.0);
                double unpaid = Optional.ofNullable(paymentCallRepository.getUnpaidAmountByParentProperty(propertyId))
                                .orElse(0.0);
                return new CallForChargeKpiResponse(total, paid, unpaid);
        }

        @Override
        public PaymentCall updateStatus(Long paymentCallId, String status) {

                PaymentCall paymentCall = paymentCallRepository.findById(paymentCallId)
                                .orElseThrow(() -> new RuntimeException("PaymentCall introuvable"));

                PaymentCallStatusEnum statusEnum;

                try {
                        statusEnum = PaymentCallStatusEnum.valueOf(status.toUpperCase());
                } catch (IllegalArgumentException e) {
                        throw new RuntimeException(
                                        "Statut invalide. Valeurs possibles : " +
                                                        Arrays.toString(PaymentCallStatusEnum.values()));
                }

                paymentCall.setStatus(statusEnum);
                paymentCall.setUpdatedAt(System.currentTimeMillis());

                return paymentCallRepository.save(paymentCall);
        }

        private double calculateFinalAmount(double baseAmount, Double tva, Double tom) {
                double finalAmount = baseAmount;

                if (tva != null && tva > 0) {
                        finalAmount += baseAmount * tva / 100;
                }

                if (tom != null && tom > 0) {
                        finalAmount += tom;
                }

                return finalAmount;
        }

        @Override
        public PaymentCallKpiDto getPaymentCallKpis(Long propertyId) {

                RealEstateProperty property = realEstatePropertyRepository.findById(propertyId)
                                .orElseThrow(() -> new IllegalArgumentException("Property not found"));

                double totalPrice = property.getPrice();

                List<PaymentCall> calls = paymentCallRepository.findByRealEstatePropertyId(propertyId)
                                .stream()
                                .filter(pc -> pc.getPercentage() > 0)
                                .toList();

                // Montant déjà payé
                double paidAmount = calls.stream()
                                .filter(pc -> pc.getStatus() == PaymentCallStatusEnum.PAID)
                                .mapToDouble(PaymentCall::getAmount)
                                .sum();

                // Pourcentage payé
                double totalPercentage = calls.stream()
                                .filter(pc -> pc.getStatus() == PaymentCallStatusEnum.PAID)
                                .mapToDouble(PaymentCall::getPercentage)
                                .sum();

                double remainingAmount = totalPrice - paidAmount;
                double remainingPercentage = 100 - totalPercentage;

                // Limiter à 1 chiffre après la virgule
                totalPercentage = Math.round(totalPercentage * 10.0) / 10.0;
                remainingPercentage = Math.round(remainingPercentage * 10.0) / 10.0;

                return new PaymentCallKpiDto(
                                totalPrice,
                                paidAmount,
                                remainingAmount,
                                remainingPercentage);
        }
}
