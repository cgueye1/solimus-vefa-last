package com.wakana.vefa.services.impl;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.wakana.vefa.services.LamSmsService;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LamSmsServiceImpl implements LamSmsService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${lam.sms.url}")
    private String smsUrl;

    @Value("${lam.sms.accountid}")
    private String accountId;

    @Value("${lam.sms.password}")
    private String password;

    @Value("${lam.sms.sender}")
    private String sender;

    @Override
    public void sendSms(String phoneNumber, String message) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> requestBody = Map.of(
                "accountid", accountId,
                "password", password,
                "sender", sender,
                "text", message,
                "to", phoneNumber
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(smsUrl, request, String.class);
            System.out.println("SMS envoyé: " + response.getBody());
        } catch (Exception e) {
            System.err.println("Erreur lors de l’envoi du SMS: " + e.getMessage());
        }
    }
}
