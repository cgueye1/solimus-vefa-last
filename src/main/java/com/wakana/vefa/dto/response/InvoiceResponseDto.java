package com.wakana.vefa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.format.DateTimeFormatter;
import com.wakana.vefa.model.Invoice;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDto {
    private Long id;
    private String invoiceNumber;
    private double amount;
    private String createdAt;
    private boolean paid;
    private String paymentMethod;
    private String planLabel;
    private String userName;

    public static InvoiceResponseDto fromEntity(Invoice invoice) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        return new InvoiceResponseDto(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getAmount(),
                invoice.getCreatedAt().format(formatter),
                invoice.isPaid(),
                invoice.getPaymentMethod(),
                invoice.getSubscription() != null ? invoice.getSubscription().getSubscriptionPlans().getLabel() : null,
                invoice.getUser() != null ? invoice.getUser().getPrenom() + " " + invoice.getUser().getNom() : null);
    }
}
