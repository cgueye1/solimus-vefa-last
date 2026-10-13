package com.wakana.vefa.services.impl;

import com.wakana.vefa.services.WhatsAppOtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WhatsAppOtpServiceImpl implements WhatsAppOtpService {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${whatsapp.graph.version}")
    private String graphVersion;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.otp.template}")
    private String templateName;

    @Value("${whatsapp.otp.language}")
    private String templateLanguage;

    @Value("${whatsapp.otp.button-parameter}")
    private String buttonParameter;

        @Value("${whatsapp.test-mode:false}")
        private boolean testMode;

        @Value("${whatsapp.test-phone-number:}")
        private String testPhoneNumber;

    @Override
    public void sendOtp(String phoneNumber, String otp) {
        String recipient = testMode
                ? normalizePhoneNumber(testPhoneNumber)
                : normalizePhoneNumber(phoneNumber);
        String endpoint = "https://graph.facebook.com/" + graphVersion + "/"
                + phoneNumberId + "/messages";

        Map<String, Object> requestBody = Map.of(
                "messaging_product", "whatsapp",
                "to", recipient,
                "type", "template",
                "template", Map.of(
                        "name", templateName,
                        "language", Map.of("code", templateLanguage),
                        "components", List.of(
                                Map.of(
                                        "type", "body",
                                        "parameters", List.of(Map.of(
                                                "type", "text",
                                                "text", otp))),
                                Map.of(
                                        "type", "button",
                                        "sub_type", "url",
                                        "index", "0",
                                        "parameters", List.of(Map.of(
                                                "type", "text",
                                                "text", buttonParameter))))));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity(endpoint,
                new HttpEntity<>(requestBody, headers),
                String.class);
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
