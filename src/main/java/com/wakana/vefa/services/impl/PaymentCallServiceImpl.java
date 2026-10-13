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
import com.wakana.vefa.services.NotificationsService;
import com.wakana.vefa.services.EmailService;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Service
@RequiredArgsConstructor
public class PaymentCallServiceImpl implements PaymentCallService {

        private final PaymentCallRepository paymentCallRepository;
        private final RealEstatePropertyRepository realEstatePropertyRepository;

        private final NotificationsService notificationsService;
        private final EmailService emailService;


        private final PaymentCallPdfService paymentCallPdfService;

        @Override
        public List<PaymentCall> getPaymentCallsForProperty(Long propertyId) {
                return withOverdueFlag(paymentCallRepository.findByRealEstatePropertyIdOrderByCreatedAtDesc(propertyId));
        }

        @Override
        public List<PaymentCall> getPaymentCallsForRecipient(Long recipientId) {
                return withOverdueFlag(paymentCallRepository.findByRealEstatePropertyRecipientIdOrderByExpectedDateAsc(recipientId));
        }

        @Override
        @Transactional(readOnly = true)
        public Optional<PaymentCall> getPaymentCall(Long id) {
                return paymentCallRepository.findById(id);
        }

        private List<PaymentCall> withOverdueFlag(List<PaymentCall> calls) {
                calls.forEach(call -> call.setOverdue(isOverdue(call)));
                return calls;
        }

        private boolean isOverdue(PaymentCall call) {
                if (call.getStatus() == PaymentCallStatusEnum.OVERDUE) {
                        return true;
                }
                if (call.getStatus() != PaymentCallStatusEnum.PENDING || call.getExpectedDate() == null) {
                        return false;
                }
                for (DateTimeFormatter formatter : List.of(
                                DateTimeFormatter.ISO_LOCAL_DATE,
                                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                                DateTimeFormatter.ofPattern("dd/MM/yyyy"))) {
                        try {
                                return LocalDate.parse(call.getExpectedDate().trim(), formatter).isBefore(LocalDate.now());
                        } catch (DateTimeParseException ignored) {
                                // Try the next legacy date format.
                        }
                }
                return false;
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

                double finalAmount = calculateFinalAmount(
                                request.getAmount(), request.getTva(), request.getTom());

                double totalAmount = existingCalls.stream()
                                .mapToDouble(PaymentCall::getAmount)
                                .sum() + finalAmount; // inclure le nouvel appel avec taxes

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
                if (!property.isRental() && totalPercentage >= 100
                                && Math.abs(totalAmount - property.getPrice()) > 0.01) {
                        throw new IllegalArgumentException(
                                        "Lorsque les appels atteignent 100%, leur total doit être exactement égal au prix du lot.");
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

                        double finalAmount = calculateFinalAmount(
                                        request.getAmount(), request.getTva(), request.getTom());
                        double totalAmount = otherCalls.stream()
                                        .mapToDouble(PaymentCall::getAmount)
                                        .sum() + finalAmount;

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
                        if (!property.isRental() && totalPercentage >= 100
                                        && Math.abs(totalAmount - property.getPrice()) > 0.01) {
                                throw new IllegalArgumentException(
                                                "Lorsque les appels atteignent 100%, leur total doit être exactement égal au prix du lot.");
                        }

                        // Mise à jour
                        existing.setLabel(request.getLabel());
                        existing.setPercentage(request.getPercentage());
                        existing.setExpectedDate(request.getExpectedDate());
                        String requestedStatus = request.getStatus();
                        existing.setStatus(requestedStatus == null || requestedStatus.isBlank()
                                        ? existing.getStatus()
                                        : PaymentCallStatusEnum.valueOf(requestedStatus.toUpperCase()));
                        existing.setUpdatedAt(System.currentTimeMillis());
                        existing.setTom(request.getTom());
                        existing.setTva(request.getTva());
                        existing.setBaseAmount(request.getAmount());
                        existing.setRef(request.getRef());

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

                PaymentCallStatusEnum previousStatus = paymentCall.getStatus();
                paymentCall.setStatus(statusEnum);
                paymentCall.setUpdatedAt(System.currentTimeMillis());

                PaymentCall savedPaymentCall = paymentCallRepository.save(paymentCall);
                if (previousStatus != PaymentCallStatusEnum.PAID && statusEnum == PaymentCallStatusEnum.PAID) {
                        notifyRecipientOfPayment(savedPaymentCall);
                }

                return savedPaymentCall;
        }

        private void notifyRecipientOfPayment(PaymentCall paymentCall) {
                RealEstateProperty property = paymentCall.getRealEstateProperty();
                if (property == null || property.getRecipient() == null
                                || property.getRecipient().getEmail() == null
                                || property.getRecipient().getEmail().isBlank()) {
                        return;
                }

                String recipientName = property.getRecipient().getPrenom() + " " + property.getRecipient().getNom();
                String propertyName = property.getName() == null ? "votre lot" : property.getName();
                String html = "<p>Bonjour " + recipientName + ",</p>"
                                + "<p>Nous vous confirmons que votre appel de fonds concernant <strong>"
                                + propertyName + "</strong> a été marqué comme payé.</p>"
                                + "<p>Montant payé : <strong>"
                                + String.format("%,.0f FCFA", paymentCall.getAmount()) + "</strong></p>"
                                + "<p>Merci pour votre confiance.</p>";

                try {
                        emailService.sendHtmlMessage(
                                        property.getRecipient().getEmail(),
                                        "Confirmation de paiement de votre appel de fonds",
                                        html,
                                        null);
                } catch (RuntimeException exception) {
                        System.err.println("Impossible d'envoyer la confirmation de paiement : "
                                        + exception.getMessage());
                }
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
