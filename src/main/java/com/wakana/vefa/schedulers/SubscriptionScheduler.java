package com.wakana.vefa.schedulers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.wakana.vefa.model.Subscription;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.repository.SubscriptionRepository;
import com.wakana.vefa.services.EmailService;
import com.wakana.vefa.services.NotificationsService;
import com.wakana.vefa.services.SubscriptionService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionScheduler {

    private final SubscriptionService subscriptionService;
    private final SubscriptionRepository subscriptionRepository;
    @Autowired
    private EmailService emailService;

    private final NotificationsService notificationsService;

    @Scheduled(fixedRate = 86400000) // Exécution chaque 24 heures
    public void checkAndRenewSubscriptions() {
        // Récupérer uniquement les abonnements actifs et payés
        List<Subscription> paidSubscriptions = subscriptionRepository
                .findByActiveTrueAndStatus(PaymentCallStatusEnum.PAID);

        for (Subscription subscription : paidSubscriptions) {
            LocalDate today = LocalDate.now();
            LocalDate renewalDate = subscription.getEndDate().minusDays(2);

            // Vérifier si on doit renouveler aujourd'hui et éviter les doublons
            if (!subscription.isRenewed() && !today.isBefore(renewalDate)) {
                subscription.setRenewed(true); // Marquer comme renouvelé
                subscriptionRepository.save(subscription);
                // Renouveler l'abonnement
                subscriptionService.renewSubscription(subscription);
                sendRenewalEmail(subscription);
                notificationsService.sendReservationNotif(
                        "Renouvellement de votre abonnement - Solimus",
                        "Nous vous informons que votre abonnement à Solimus arrive à expiration dans deux jours.",
                        null, subscription.getUser());

            }

            if ((today.isAfter(subscription.getEndDate()) || today.isEqual(subscription.getEndDate()))) {

                if (subscription.isRenewed()) {
                    subscription.setActive(false); // Désactiver l'abonnement
                    subscriptionRepository.save(subscription);
                }

            }

        }
    }

    @Transactional
    private void sendRenewalEmail(Subscription subscription) {
        String html = "<html>" +
                "<body>" +
                "<p>Bonjour " + subscription.getUser().getPrenom() + " " + subscription.getUser().getNom() + ",</p>" +
                "<p>Nous vous informons que votre abonnement à Solimus arrive à expiration dans deux jours.</p>" +
                "<p>Pour éviter toute interruption de service, nous vous invitons à procéder au renouvellement dès maintenant.</p>"
                +
                "<p>Veuillez effectuer votre paiement en cliquant sur le lien ci-dessous :</p>" +
                "<p><a href='https://solimus.sn/'>Effectuer mon paiement</a></p>" +
                "<p>Si vous avez déjà effectué le paiement, veuillez ignorer ce message.</p>" +
                "<p>Pour toute question ou assistance, n’hésitez pas à contacter notre support.</p>" +
                "<br>" +
                "<p>Cordialement,</p>" +
                "<p><strong>L’équipe Solimus</strong></p>" +
                "</body>" +
                "</html>";

        emailService.sendHtmlMessage(
                subscription.getUser().getEmail(),
                "Renouvellement de votre abonnement - Solimus",
                html,
                null);

    }

    /*
     * @Scheduled(cron = "0 0 0 * * ?") // Exécuter tous les jours à minuit
     * public void checkAndNotifySubscriptions() {
     * // Récupérer uniquement les abonnements actifs et payés
     * List<Subscription> paidSubscriptions = subscriptionRepository
     * .findByActiveTrueAndStatus(PaymentCallStatusEnum.PAID);
     * 
     * LocalDate today = LocalDate.now();
     * 
     * for (Subscription subscription : paidSubscriptions) {
     * LocalDate endDate = subscription.getEndDate();
     * 
     * // Vérifier si la date d'expiration est dans ≤ 2 jours (rappel)
     * if (!endDate.isBefore(today) && endDate.minusDays(2).isEqual(today)) {
     * // Envoyer un rappel à l'utilisateur
     * // notificationService.sendSubscriptionReminder(subscription.getUser());
     * }
     * 
     * // Si la date d'expiration est dans 2 jours, renouveler l'abonnement
     * if (endDate.minusDays(2).isEqual(today)) {
     * subscriptionService.renewSubscription(subscription);
     * }
     * 
     * // Si l'abonnement expire aujourd'hui, désactiver
     * if (endDate.isEqual(today)) {
     * subscription.setActive(false);
     * subscriptionRepository.save(subscription);
     * }
     * }
     * }
     */

}
