package com.wakana.vefa.scheduler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.model.PaymentCall;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.repository.PaymentCallRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeneratePaymentCallScheduler {

    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final PaymentCallRepository paymentCallRepository;

    // Tous les mois le 1er à 02h du matin
    @Scheduled(cron = "0 0 2 5 * ?")
    @Transactional
    public void generateMonthlyPaymentCalls() {

        log.info("🚀 Démarrage du scheduler PaymentCall mensuel");

        List<RealEstateProperty> properties = realEstatePropertyRepository.findByRentalTrueAndRecipientIsNotNull();

        for (RealEstateProperty property : properties) {

            Optional<PaymentCall> lastCallOpt = paymentCallRepository
                    .findTopByRealEstatePropertyOrderByCreatedAtDesc(property);

            if (lastCallOpt.isEmpty()) {
                log.warn("⚠️ Aucun PaymentCall trouvé pour le bien {}", property.getId());
                continue;
            }

            PaymentCall lastCall = lastCallOpt.get();

            PaymentCall newCall = new PaymentCall();
            newCall.setLabel(lastCall.getLabel());
            newCall.setPercentage(lastCall.getPercentage());
            newCall.setAmount(lastCall.getAmount());
            newCall.setTom(lastCall.getTom());
            newCall.setTva(lastCall.getTva());
            newCall.setBaseAmount(lastCall.getBaseAmount());

            newCall.setExpectedDate(addOneMonth(lastCall.getExpectedDate()));
            newCall.setStatus(PaymentCallStatusEnum.PENDING);
            newCall.setRealEstateProperty(property);
            newCall.setCreatedAt(System.currentTimeMillis());
            newCall.setUpdatedAt(System.currentTimeMillis());

            paymentCallRepository.save(newCall);

            log.info("✅ PaymentCall généré pour le bien {} | Date {}",
                    property.getId(),
                    newCall.getExpectedDate());
        }

        log.info("🏁 Fin du scheduler PaymentCall mensuel");
    }

    private String addOneMonth(String expectedDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

        LocalDate date = LocalDate.parse(expectedDate, formatter);
        LocalDate nextMonth = date.plusMonths(1);

        return nextMonth.format(formatter);
    }

}
