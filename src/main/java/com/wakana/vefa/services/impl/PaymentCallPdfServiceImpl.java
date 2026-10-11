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
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PaymentCallPdfServiceImpl implements PaymentCallPdfService {

        private final TemplateEngine templateEngine;
        private final PaymentCallRepository paymentCallRepository;
        private final EmailService emailService;

        @Override
        public byte[] generatePdf(PaymentCall paymentCall) {

                RealEstateProperty property = paymentCall.getRealEstateProperty();
                User promoter = property.getPromoter();
                User buyer = property.getRecipient();
                String program = property.getParentProperty().getName();
                var company = promoter.getCompany();

                // Appels de fonds déjà payés
                List<PaymentCall> paidCalls = paymentCallRepository
                                .findByRealEstatePropertyIdAndStatus(
                                                property.getId(),
                                                PaymentCallStatusEnum.PAID)
                                .stream()
                                .filter(pc -> pc.getPercentage() > 0)
                                .toList();

                double paidAmountValue = paidCalls.stream()
                                .mapToDouble(PaymentCall::getAmount)
                                .sum();

                double paidPercentage = paidCalls.stream()
                                .mapToDouble(PaymentCall::getPercentage)
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