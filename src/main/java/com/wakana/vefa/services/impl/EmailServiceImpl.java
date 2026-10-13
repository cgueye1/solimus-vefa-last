package com.wakana.vefa.services.impl;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.time.format.DateTimeFormatter;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender emailSender;

    public void sendSimpleMessage(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("support@solimus.sn");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        emailSender.send(message);
    }

    public void sendHtmlMessage(String to, String subject, String html, List<String> ccEmails) {
        MimeMessage message = emailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("support@solimus.sn");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);
            if (ccEmails != null && !ccEmails.isEmpty()) {
                helper.setCc(ccEmails.toArray(new String[0]));
            }
            emailSender.send(message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Impossible de préparer l'e-mail.", e);
        }

    }

    public void sendHtmlMessageWithAttachment(
            String to,
            String subject,
            String html,
            byte[] attachment,
            String fileName,
            String contentType,
            List<String> ccEmails) {

        MimeMessage message = emailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom("support@solimus.sn");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            if (ccEmails != null && !ccEmails.isEmpty()) {
                helper.setCc(ccEmails.toArray(new String[0]));
            }

            helper.addAttachment(
                    fileName,
                    new ByteArrayResource(attachment),
                    contentType);

            emailSender.send(message);

        } catch (MessagingException e) {
            throw new RuntimeException("Erreur envoi email avec pièce jointe", e);
        }
    }

    public void sendMeetingInvite(
            List<String> to,
            List<String> cc,
            List<String> bcc,
            String subject,
            String description,
            LocalDateTime startTime,
            LocalDateTime endTime,
            String location) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            // Format des dates pour le fichier ICS
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
            String startDate = startTime.format(dtf);
            String endDate = endTime.format(dtf);

            // Contenu ICS (iCalendar)
            String icsContent = "BEGIN:VCALENDAR\n" +
                    "PRODID:-//MyApp//Meeting Invite//EN\n" +
                    "VERSION:2.0\n" +
                    "CALSCALE:GREGORIAN\n" +
                    "METHOD:REQUEST\n" +
                    "BEGIN:VEVENT\n" +
                    "DTSTART:" + startDate + "\n" +
                    "DTEND:" + endDate + "\n" +
                    "DTSTAMP:" + LocalDateTime.now().format(dtf) + "\n" +
                    "UID:" + java.util.UUID.randomUUID() + "\n" +
                    "SUMMARY:" + subject + "\n" +
                    "DESCRIPTION:" + description + "\n" +
                    "LOCATION:" + location + "\n" +
                    "STATUS:CONFIRMED\n" +
                    "SEQUENCE:0\n" +
                    "BEGIN:VALARM\n" +
                    "ORGANIZER:mailto:" + "SOlimus" + "\n" + // Ajout de l'organisateur
                    "TRIGGER:-PT15M\n" +
                    "DESCRIPTION:Reminder\n" +
                    "ACTION:DISPLAY\n" +
                    "END:VALARM\n" +
                    "END:VEVENT\n" +
                    "END:VCALENDAR";

            // Ajout des destinataires
            helper.setTo(to.toArray(new String[0])); // Destinataires principaux
            if (cc != null && !cc.isEmpty()) {
                helper.setCc(cc.toArray(new String[0])); // Copie
            }
            if (bcc != null && !bcc.isEmpty()) {
                helper.setBcc(bcc.toArray(new String[0])); // Copie cachée
            }

            helper.setSubject(subject);
            helper.setText(description, true);
            helper.setFrom("support@solimus.sn");

            // Ajout de la pièce jointe .ics
            helper.addAttachment("invitation.ics",
                    new org.springframework.core.io.ByteArrayResource(icsContent.getBytes(StandardCharsets.UTF_8)));

            // Envoi du mail
            emailSender.send(message);
            System.out.println("Invitation envoyée avec succès à " + to);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur lors de l'envoi de l'invitation.");
        }
    }

    public String getPropertyHTML(RealEstateProperty property, String headerMessage, String message,
            String alertColor) {
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
                "  .button { background:rgb(209, 209, 209); color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px; font-weight: bold; display: inline-block; }"
                +
                "</style>" +
                "</head>" +
                "<body>" +
                "  <div class='container'>" +
                "    <div class='header'>" + (headerMessage)
                + "</div>" +
                "    <div class='content'>" +
                "      <p>Bonjour,</p>" +
                "      <p>" + message + "</p>" +
                "      <p><b>Lot concerné :</b> " + property.getName() + "</p>" +
                "      <p><b>Propriété concernée :</b> " + (property.getParentProperty() != null
                        ? property.getParentProperty().getName()
                        : property.getName())
                + "</p>" +
                "      <p> cordialement" + "</p>" +

                "      <p style='text-align:center;'>" +
                "        <a href='https://solimus.sn/#/auth/login' class='button'>Voir les details</a>" +
                "      </p>" +
                "    </div>" +
                "    <div class='footer'></div>" +
                "  </div>" +
                "</body>" +
                "</html>";
    }

}
