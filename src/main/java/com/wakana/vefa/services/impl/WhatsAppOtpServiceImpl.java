package com.wakana.vefa.services.impl;

import com.wakana.vefa.services.WhatsAppOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@RequiredArgsConstructor
public class WhatsAppOtpServiceImpl implements WhatsAppOtpService {

        private static final String GRAPH_VERSION = "v25.0";
        private static final String PHONE_NUMBER_ID = "996688440202981";
        private static final String TEMPLATE_NAME = "otp_code";
        private static final String TEMPLATE_LANGUAGE = "en";
        private static final String TEST_RECIPIENT = "221774262278";

            private final HttpClient httpClient = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build();

    @Override
    public void sendOtp(String phoneNumber, String otp) {
        String accessToken = System.getenv("WHATSAPP_ACCESS_TOKEN");
        if (accessToken == null || accessToken.isBlank()) {
            throw new IllegalStateException("WHATSAPP_ACCESS_TOKEN est obligatoire.");
        }

        String body = "{"
                + "\"messaging_product\":\"whatsapp\","
                + "\"to\":\"" + escape(TEST_RECIPIENT) + "\","
                + "\"type\":\"template\","
                + "\"template\":{"
                + "\"name\":\"" + TEMPLATE_NAME + "\","
                + "\"language\":{\"code\":\"" + TEMPLATE_LANGUAGE + "\"},"
                + "\"components\":["
                + "{\"type\":\"body\",\"parameters\":[{\"type\":\"text\",\"text\":\""
                + escape(otp) + "\"}]},"
                + "{\"type\":\"button\",\"sub_type\":\"url\",\"index\":\"0\","
                + "\"parameters\":[{\"type\":\"text\",\"text\":\"" + escape(otp) + "\"}]}"
                + "]}"
                + "}";

        String endpoint = "https://graph.facebook.com/" + GRAPH_VERSION + "/"
                + PHONE_NUMBER_ID + "/messages";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofSeconds(15))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Erreur WhatsApp API (" + response.statusCode() + ")");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Impossible de joindre l'API WhatsApp.", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Envoi WhatsApp interrompu.", e);
        }
    }

    private static String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Le numéro WhatsApp est obligatoire.");
        }

        String normalized = phoneNumber.replaceAll("[^0-9]", "");
                if (normalized.startsWith("00")) {
                        normalized = normalized.substring(2);
                }
                if (normalized.length() == 9) {
                        normalized = "221" + normalized;
                }
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Le numéro WhatsApp est invalide.");
        }
        return normalized;
    }
}
