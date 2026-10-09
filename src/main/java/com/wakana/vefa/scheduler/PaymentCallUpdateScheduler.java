package com.wakana.vefa.scheduler;



import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.model.PaymentCall;
import com.wakana.vefa.repository.PaymentCallRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCallUpdateScheduler {

    private final PaymentCallRepository paymentCallRepository;

    /**
     * Démarre dès le lancement de l'application (initialDelay = 0)
     * puis se répète chaque 24h (fixedRate = 86400000 ms)
     */
    @Scheduled(initialDelay = 0, fixedRate = 86_400_000)
    @Transactional
    public void markOverduePaymentCalls() {
        log.info("[Scheduler] Vérification des appels de fonds en retard...");

        LocalDate today = LocalDate.now();

        // On récupère uniquement les PENDING (les PAID ne doivent jamais passer OVERDUE)
        List<PaymentCall> pendingCalls = paymentCallRepository
                .findByStatus(PaymentCallStatusEnum.PENDING);

        int count = 0;

        for (PaymentCall pc : pendingCalls) {
            LocalDate expectedDate = parseDate(pc.getExpectedDate());
            if (expectedDate != null && expectedDate.isBefore(today)) {
                pc.setStatus(PaymentCallStatusEnum.OVERDUE);
                count++;
            }
        }

        if (count > 0) {
            paymentCallRepository.saveAll(pendingCalls);
            log.info("[Scheduler] {} appel(s) de fonds marqué(s) OVERDUE.", count);
        } else {
            log.info("[Scheduler] Aucun appel de fonds en retard détecté.");
        }
    }

    /**
     * Parse la date depuis String.
     * Supporte les formats : yyyy-MM-dd, dd/MM/yyyy, dd-MM-yyyy
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;

        List<DateTimeFormatter> formatters = List.of(
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy")
        );

        for (DateTimeFormatter fmt : formatters) {
            try {
                return LocalDate.parse(dateStr.trim(), fmt);
            } catch (DateTimeParseException ignored) {}
        }

        log.warn("[Scheduler] Format de date non reconnu pour : '{}'", dateStr);
        return null;
    }
}