package com.wakana.vefa.services.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.model.PaymentCall;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.PaymentCallRepository;
import com.wakana.vefa.services.EmailService;
import com.wakana.vefa.services.PaymentCallPdfService;
import com.wakana.vefa.util.CurrencyFormatter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PaymentCallPdfServiceImpl implements PaymentCallPdfService {

        private final TemplateEngine templateEngine;
        private final PaymentCallRepository paymentCallRepository;
        private final EmailService emailService;

        @Override
        @Transactional(readOnly = true)
        public byte[] generatePdf(PaymentCall paymentCall) {
                return renderPdf(paymentCall, false);
        }

        @Override
        @Transactional(readOnly = true)
        public byte[] generateReceiptPdf(PaymentCall paymentCall) {
                PaymentCall persistedPaymentCall = paymentCallRepository.findById(paymentCall.getId())
                                .orElseThrow(() -> new IllegalArgumentException("Appel de fonds introuvable"));
                if (persistedPaymentCall.getStatus() != PaymentCallStatusEnum.PAID) {
                        throw new IllegalStateException("Une quittance est disponible uniquement après paiement");
                }
                return renderPdf(persistedPaymentCall, true);
        }

        private byte[] renderPdf(PaymentCall paymentCall, boolean receipt) {

                paymentCall = paymentCallRepository.findById(paymentCall.getId())
                                .orElseThrow(() -> new IllegalArgumentException("Appel de fonds introuvable"));

                RealEstateProperty property = paymentCall.getRealEstateProperty();
                if (property == null) {
                        throw new IllegalArgumentException("Le bien associé à cet appel de fonds est introuvable");
                }
                User promoter = property != null ? property.getPromoter() : null;
                User buyer = property != null ? property.getRecipient() : null;
                String program = property != null && property.getParentProperty() != null
                                ? property.getParentProperty().getName()
                                : property != null ? property.getName() : "Programme immobilier";
                var company = promoter != null ? promoter.getCompany() : null;

                // Appels de fonds déjà payés
                List<PaymentCall> paidCalls = paymentCallRepository
                                .findByRealEstatePropertyIdAndStatus(
                                                property.getId(),
                                                PaymentCallStatusEnum.PAID)
                                .stream()
                                .filter(pc -> pc.getPercentage() > 0)
                                .toList();

                double paidAmountValue = paidCalls.stream()
                                .mapToDouble(call -> call.getAmount())
                                .sum();

                double paidPercentage = paidCalls.stream()
                                .mapToDouble(call -> call.getPercentage())
                                .sum();

                double totalPrice = property.getPrice();
                double remainingAmount = totalPrice - paidAmountValue;

                Context context = new Context();
                context.setVariable("paymentCall", paymentCall);
                context.setVariable("property", property);
                context.setVariable("promoter", promoter);
                context.setVariable("company", company);
                context.setVariable("buyer", buyer);
                context.setVariable("program", program);
                context.setVariable("documentNumber", (receipt ? "QUI-" : "APF-") + paymentCall.getId());
                context.setVariable("generatedDate", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                context.setVariable("isReceipt", receipt);
                context.setVariable("documentTitle", receipt
                                ? (property.isRental() ? "QUITTANCE DE LOYER" : "RECU DE PAIEMENT")
                                : (property.isRental() ? "AVIS D'ECHEANCE" : "APPEL DE FONDS"));
                context.setVariable("documentSubtitle", receipt
                                ? "Justificatif de paiement"
                                : (property.isRental() ? "Document de paiement locatif" : "Vente en l'Etat Futur d'Achevement (VEFA)"));
                context.setVariable("detailTitle", receipt ? "DETAIL DU PAIEMENT" : "DETAIL DE L'APPEL DE FONDS");
                context.setVariable("amountLabel", receipt ? "MONTANT REGLE" : "MONTANT A PAYER");
                context.setVariable("dateLabel", receipt ? "Date de paiement : " : "Date limite de paiement : ");
                context.setVariable("documentDate", receipt
                                ? LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                : paymentCall.getExpectedDate());
                context.setVariable("footerText", receipt
                                ? "Ce document certifie le paiement de l'appel de fonds indique ci-dessus."
                                : "Ce document fait office d'appel de fonds conformément au contrat de réservation VEFA");
                context.setVariable("isRental", property.isRental());
                context.setVariable("statusText", receipt ? "PAYE" : paymentCall.getStatus() == PaymentCallStatusEnum.PAID ? "PAYE" : "EN ATTENTE");
                context.setVariable("propertyNumber", property != null ? property.getNumber() : "Non renseigné");
                context.setVariable("promoterName", fullName(promoter));
                context.setVariable("promoterAddress", value(promoter != null ? promoter.getAdress() : null));
                context.setVariable("promoterPhone", value(promoter != null ? promoter.getTelephone() : null));
                context.setVariable("promoterEmail", value(promoter != null ? promoter.getEmail() : null));
                context.setVariable("buyerName", fullName(buyer));
                context.setVariable("buyerAddress", value(buyer != null ? buyer.getAdress() : null));
                context.setVariable("buyerPhone", value(buyer != null ? buyer.getTelephone() : null));
                context.setVariable("buyerEmail", value(buyer != null ? buyer.getEmail() : null));
                context.setVariable("loyer", CurrencyFormatter.formatCFA(
                                paymentCall.getAmount() - paymentCall.getTom()));
                context.setVariable("tom", CurrencyFormatter.formatCFA(paymentCall.getTom()));
                context.setVariable("tva", paymentCall.getTva());

                // Montants formatés en CFA
                context.setVariable("propertyPrice", CurrencyFormatter.formatCFA(totalPrice));
                context.setVariable("paidAmount", CurrencyFormatter.formatCFA(paidAmountValue));
                context.setVariable("remainingAmount", CurrencyFormatter.formatCFA(remainingAmount));
                context.setVariable("currentAmount", CurrencyFormatter.formatCFA(paymentCall.getAmount()));

                context.setVariable("paidPercentage", paidPercentage);
                context.setVariable("currentPercentage", paymentCall.getPercentage());

                String html = templateEngine.process("payment-call", context);

                try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                        PdfRendererBuilder builder = new PdfRendererBuilder();
                        builder.withHtmlContent(html, null);
                        builder.toStream(outputStream);
                        builder.run();

                        return outputStream.toByteArray();

                } catch (Exception e) {
                        throw new RuntimeException("Erreur génération PDF appel de fonds", e);
                }
        }

        private String value(String value) {
                return value == null || value.isBlank() ? "Non renseigné" : value;
        }

        private String fullName(User user) {
                if (user == null) {
                        return "Non renseigné";
                }
                String name = ((user.getPrenom() == null ? "" : user.getPrenom()) + " "
                                + (user.getNom() == null ? "" : user.getNom())).trim();
                return value(name);
        }

        @Override
        public void sendPaymentCallByEmail(PaymentCall paymentCall) {

                byte[] pdf = generatePdf(paymentCall);

                String subject = "Appel de fonds – " + paymentCall.getLabel();

                String html = """
                                <p>Bonjour,</p>
                                <p>Veuillez trouver en pièce jointe votre appel de fonds.</p>
                                <p>Cordialement.</p>
                                """;

                emailService.sendHtmlMessageWithAttachment(
                                paymentCall.getRealEstateProperty().getRecipient().getEmail(),

                                subject,
                                html,
                                pdf,
                                "appel-fonds-" + paymentCall.getId() + ".pdf",
                                "application/pdf",
                                List.of(paymentCall.getRealEstateProperty().getPromoter().getEmail())

                );
        }
}