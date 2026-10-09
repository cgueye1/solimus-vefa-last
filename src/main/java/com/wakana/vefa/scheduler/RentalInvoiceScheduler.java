package com.wakana.vefa.scheduler;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.RentalInvoice;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.RentalInvoiceRepository;

import lombok.RequiredArgsConstructor;

// Scheduler pour générer les factures chaque fin de mois
@Component
@EnableScheduling
@RequiredArgsConstructor
public class RentalInvoiceScheduler {

    private final RealEstatePropertyRepository propertyRepository;
    private final RentalInvoiceRepository invoiceRepository;

    @Scheduled(cron = "0 0 0 L * ?") // Exécution à minuit le dernier jour de chaque mois
    public void generateInvoices() {
        // Récupérer les propriétés en location avec rentalDate et recipient non null
        List<RealEstateProperty> rentalProperties = propertyRepository.findByRentalTrueAndRentalDateNotNullAndRecipientNotNull();

        for (RealEstateProperty property : rentalProperties) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime rentalStartDate = property.getRentalDate();

            double monthlyRent = property.getPrice();
            double amountToPay;

            // Calculer le montant pour le mois partiel si la location a commencé ce mois-ci
            if (rentalStartDate.getYear() == now.getYear() && rentalStartDate.getMonth() == now.getMonth()) {
                int daysInMonth = now.getMonth().length(now.toLocalDate().isLeapYear());
                int rentedDays = daysInMonth - rentalStartDate.getDayOfMonth() + 1;
                amountToPay = (monthlyRent / daysInMonth) * rentedDays;
            } else {
                amountToPay = monthlyRent;
            }

            RentalInvoice invoice = new RentalInvoice();
            invoice.setProperty(property);
            invoice.setAmount(amountToPay);
            invoice.setDueDate(now.plusMonths(1).withDayOfMonth(1)); // Échéance au 1er du mois suivant
            invoice.setPaid(false);

            invoiceRepository.save(invoice);
        }
    }
}
