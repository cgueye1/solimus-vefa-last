
package com.wakana.vefa.controller;

import java.util.Collections;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.wakana.vefa.dto.OTPRequest;
import com.wakana.vefa.dto.OTPRequest1;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.EmailService;
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

    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${otp.test-email:}")
    private String testEmail;

    @PostMapping("/send")
    ResponseEntity<?> generateAndSendOTP(@RequestBody OTPRequest1 oTPRequests) {
        String otp = otpUtil.generateOTP();
        String phoneNumber = oTPRequests.getPhoneNumber();
        String normalizedPhoneNumber = normalizePhoneNumber(phoneNumber);
        Optional<User> user = findAuthenticatedUser()
            .or(() -> Optional.ofNullable(oTPRequests.getSignerId()).flatMap(userRepository::findById))
            .or(() -> userRepository.findByTelephone(phoneNumber))
            .or(() -> userRepository.findByTelephone(normalizedPhoneNumber));

        if (user.isEmpty() || user.get().getEmail() == null || user.get().getEmail().isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Collections.singletonMap("error", "Aucune adresse e-mail n'est associée à cet utilisateur."));
        }

        try {
            String message = otp + " est votre code de validation.";
            String userEmail = user.get().getEmail().trim();
            emailService.sendHtmlMessage(userEmail, "Code de validation Solimus", message, null);

            if (testEmail != null && !testEmail.isBlank()
                    && !testEmail.trim().equalsIgnoreCase(userEmail)) {
                try {
                    emailService.sendHtmlMessage(testEmail.trim(), "Code de validation Solimus", message, null);
                } catch (Exception e) {
                    log.warn("Échec d'envoi OTP vers l'e-mail de test: {}", e.getMessage());
                }
            }

            otpUtil.storeOTP(normalizedPhoneNumber, otp);
            return ResponseEntity.ok(Collections.singletonMap(
                    "message", "Votre code a été envoyé par e-mail."));
        } catch (Exception e) {
            log.warn("Échec d'envoi OTP par e-mail pour l'utilisateur {}: {}", user.get().getId(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Collections.singletonMap("error", "Impossible d'envoyer le code par e-mail."));
        }
    }

    private Optional<User> findAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByEmailOrTelephone(authentication.getName());
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
        if (otpUtil.validateOTP(normalizePhoneNumber(oTPRequests.getPhoneNumber()), oTPRequests.getOtp())) {
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
