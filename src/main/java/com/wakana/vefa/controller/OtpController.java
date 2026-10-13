
package com.wakana.vefa.controller;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import com.wakana.vefa.dto.OTPRequest;
import com.wakana.vefa.dto.OTPRequest1;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.EmailService;
import com.wakana.vefa.services.WhatsAppOtpService;
import com.wakana.vefa.util.OTPUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequestMapping("/api/otp")
@RestController
@RequiredArgsConstructor
@Slf4j
@Tag(name = " SMS")
public class OtpController {

    private final OTPUtil otpUtil;

    private final WhatsAppOtpService whatsAppOtpService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @PostMapping("/send")
    ResponseEntity<?> generateAndSendOTP(@RequestBody OTPRequest1 oTPRequests) throws Exception {
        String otp = otpUtil.generateOTP();
        String phoneNumber = oTPRequests.getPhoneNumber();
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        String message = otp + " est votre code de validation.";
        boolean whatsappSent = false;
        boolean emailSent = false;

        try {
            whatsAppOtpService.sendOtp(normalizedPhoneNumber, otp);
            whatsappSent = true;
        } catch (Exception e) {
            log.warn("Échec d'envoi OTP par WhatsApp pour {}: {}", phoneNumber, e.getMessage());
        }

        Optional<User> user = userRepository.findByTelephone(phoneNumber)
            .or(() -> userRepository.findByTelephone(normalizedPhoneNumber));
        if (user.isPresent() && user.get().getEmail() != null && !user.get().getEmail().isBlank()) {
            try {
                emailService.sendHtmlMessage(user.get().getEmail(), "Code de validation Solimus", message, null);
                emailSent = true;
            } catch (Exception e) {
                log.warn("Échec d'envoi OTP par e-mail pour {}: {}", phoneNumber, e.getMessage());
            }
        }

        if (whatsappSent || emailSent) {
            otpUtil.storeOTP(normalizedPhoneNumber, otp);
            String channels = whatsappSent && emailSent ? "WhatsApp et e-mail"
                : whatsappSent ? "WhatsApp" : "e-mail";
            return ResponseEntity.ok(Collections.singletonMap(
                    "message", "Votre code a été envoyé par " + channels + "."));
        }

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Collections.singletonMap("error", "Impossible d'envoyer le code de validation."));
    }

    private String normalizePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Le numéro de téléphone est obligatoire.");
        }

        String normalized = phoneNumber.replaceAll("[^0-9]", "");
        if (normalized.startsWith("00")) {
            normalized = normalized.substring(2);
        }
        if (normalized.length() == 9) {
            normalized = "221" + normalized;
        }
        if (normalized.length() < 10) {
            throw new IllegalArgumentException("Le numéro de téléphone est invalide.");
        }
        return normalized;
    }

    @PostMapping("/validate")
    public boolean validateOTP(@RequestBody OTPRequest oTPRequests) {
        if (otpUtil.validateOTP(oTPRequests.getPhoneNumber(), oTPRequests.getOtp())) {
            return true;
        } else {
            return false;
        }
    }

    /*
     * @GetMapping("/send")
     * public ResponseEntity<?> getAll() throws Exception {
     * 
     * otpUtil.sendSms(
     * "OTP", "2217785386140", "Salam RAS ");
     * 
     * return ResponseEntity.ok("sended");
     * 
     * }
     */

}
