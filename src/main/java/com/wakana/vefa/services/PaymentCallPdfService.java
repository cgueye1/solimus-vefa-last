package com.wakana.vefa.services;

import com.wakana.vefa.model.PaymentCall;

public interface PaymentCallPdfService {

    byte[] generatePdf(PaymentCall paymentCall);

    byte[] generateReceiptPdf(PaymentCall paymentCall);

    void sendPaymentCallByEmail(PaymentCall paymentCall);

}