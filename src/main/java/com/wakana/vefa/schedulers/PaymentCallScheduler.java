package com.wakana.vefa.schedulers;

import com.wakana.vefa.model.PaymentCall;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.PaymentCallRepository;
import com.wakana.vefa.services.EmailService;
import com.wakana.vefa.services.NotificationsService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.NumberFormat;

@Component
@RequiredArgsConstructor
public class PaymentCallScheduler {

    private final PaymentCallRepository paymentCallRepository;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    @Autowired
    private EmailService emailService;

    private final NotificationsService notificationsService;

    @Transactional
    @Scheduled(fixedRate = 86400000) // Check every 24 hours
    public void checkPaymentCallDates() {
        // Fetch payment calls that are due in the next 2 days or are overdue
        List<PaymentCall> paymentCalls = paymentCallRepository.findDueOrOverdueCalls();

        for (PaymentCall paymentCall : paymentCalls) {
            try {
                Date expectedDate = dateFormat.parse(paymentCall.getExpectedDate());
                long diffInMillies = expectedDate.getTime() - System.currentTimeMillis();
                long daysUntilDue = diffInMillies / (1000 * 60 * 60 * 24);

                if (paymentCall.getRealEstateProperty() != null &&
                        paymentCall.getRealEstateProperty().getRecipient() != null &&
                        paymentCall.getRealEstateProperty().getRecipient().getEmail() != null) {

                    String recipientEmail = paymentCall.getRealEstateProperty().getRecipient().getEmail();
                    User recipient = paymentCall.getRealEstateProperty().getRecipient();

                    if (daysUntilDue == 2 || daysUntilDue == 0) {

                        String message;
                        if (daysUntilDue >= 0) {
                            message = "Votre appel de fonds est dû dans " + daysUntilDue + " jour(s).";
                        } else {
                            message = "Votre appel de fonds était dû il y a " + Math.abs(daysUntilDue) + " jour(s).";
                        }
                        System.out.println("Alert: Payment call '" + paymentCall.getAmount() + paymentCall.getLabel() +
                                "' is due in " + daysUntilDue + " days!");
                        notificationsService.sendOnlyFCM(
                                paymentCall.getLabel(),
                                message,
                                recipient);

                        /*
                         * emailService.sendHtmlMessage(
                         * recipientEmail,
                         * getSubject((int) daysUntilDue),
                         * getHtml(paymentCall, (int) daysUntilDue));
                         */

                    } else if (daysUntilDue < 0 && (Math.abs(daysUntilDue) % 2 != 0)) {
                        System.out.println("Alert: Payment call '" + paymentCall.getLabel() +
                                "' is overdue by   recipientEmail" + paymentCall.getAmount() + Math.abs(daysUntilDue)
                                + " days!");
                        String message;
                        if (daysUntilDue >= 0) {
                            message = "Votre appel de fonds est dû dans " + daysUntilDue + " jour(s).";
                        } else {
                            message = "Votre appel de fonds était dû il y a " + Math.abs(daysUntilDue) + " jour(s).";
                        }
                        notificationsService.sendOnlyFCM(
                                paymentCall.getLabel(),
                                message,
                                recipient);

                        /*
                         * emailService.sendHtmlMessage(
                         * recipientEmail,
                         * getSubject((int) daysUntilDue),
                         * getHtml(paymentCall, (int) daysUntilDue));
                         */
                    }
                } else {
                    System.out.println(
                            "Skipping payment call '" + paymentCall.getLabel() + "' due to missing recipient email.");
                }

            } catch (ParseException e) {
                System.err.println("Error parsing date for payment call: " + paymentCall.getLabel());
            }
        }
    }

    private String getSubject(int daysUntilDue) {
        if (daysUntilDue == 2 || daysUntilDue == 0) {
            return "📅 Rappel : Votre appel de fonds est dû dans " + daysUntilDue + " jour(s)";
        } else {
            return "⏳ Alerte : Votre appel de fonds est en retard de " + Math.abs(daysUntilDue) + " jours";
        }
    }

    private String getHtml(PaymentCall paymentCall, int daysUntilDue) {
        String statusMessage;
        String alertColor;

        if (daysUntilDue == 2 || daysUntilDue == 0) {
            statusMessage = "Votre appel de fonds est dû dans <b>" + daysUntilDue
                    + " jour(s)</b>. Veuillez effectuer le paiement à temps.";
            alertColor = "#f39c12"; // Jaune
        } else {
            statusMessage = "Votre appel de fonds est <b>en retard de " + Math.abs(daysUntilDue)
                    + " jours</b>. Veuillez régulariser votre paiement immédiatement.";
            alertColor = "#e74c3c"; // Rouge
        }

        // Formatage du montant avec séparateur de milliers
        NumberFormat formatter = NumberFormat.getInstance(Locale.FRANCE);
        String formattedAmount = formatter.format(paymentCall.getAmount());

        return "<html>" +
                "<head>" +
                "<style>" +
                "  body { font-family: Arial, sans-serif; background-color: #f4f4f4; padding: 20px; }" +
                "  .container { background: #fff; padding: 20px; border-radius: 8px; box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1); }"
                +
                "  .header { background: " + alertColor
                + "; color: white; padding: 10px; font-size: 18px; font-weight: bold; text-align: center; }" +
                "  .content { padding: 20px; font-size: 16px; color: #333; }" +
                "  .footer { text-align: center; margin-top: 20px; font-size: 14px; color: #777; }" +
                "  .button { background: #2ecc71; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "  <div class='container'>" +
                "    <div class='header'>" + (daysUntilDue >= 0 ? "📅 Rappel de paiement" : "⏳ Alerte de retard")
                + "</div>" +
                "    <div class='content'>" +
                "      <p>Bonjour,</p>" +
                "      <p>" + statusMessage + "</p>" +
                "      <p><b>Titre :</b> " + paymentCall.getLabel() +
                "      <p><b>Montant :</b> " + formattedAmount + " CFA</p>" +
                "      <p><b>Date d'échéance :</b> " + paymentCall.getExpectedDate() + "</p>" +
                "      <p><b>Propriété concernée :</b> " + paymentCall.getRealEstateProperty().getName() + "</p>" +
                "      <p><b>Propriété parent :</b> "
                + paymentCall.getRealEstateProperty().getParentProperty().getName() + "</p>" +
                "      <p><b>Adresse :</b> " + paymentCall.getRealEstateProperty().getParentProperty().getAddress()
                + "</p>" +
                "      <p style='text-align:center;'>" +
                "        <a href='https://solimus.sn/#/auth/login' class='button'>Effectuer mon paiement</a>" +
                "      </p>" +
                "    </div>" +
                "    <div class='footer'>Ceci est un message automatique. Merci de ne pas répondre.</div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

}
