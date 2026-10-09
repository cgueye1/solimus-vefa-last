
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
import com.wakana.vefa.services.LamSmsService;
import com.wakana.vefa.util.OTPUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RequestMapping("/api/otp")
@RestController
@RequiredArgsConstructor
@Tag(name = " SMS")
public class OtpController {

    private final OTPUtil otpUtil;

    private final LamSmsService lamSmsService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @PostMapping("/send")
    ResponseEntity<?> generateAndSendOTP(@RequestBody OTPRequest1 oTPRequests) throws Exception {
        String otp = otpUtil.generateOTP();

        try {

            String message = otp + " est votre code de validation.";
            lamSmsService.sendSms(oTPRequests.getPhoneNumber(), message);
            otpUtil.storeOTP(oTPRequests.getPhoneNumber(), otp);

            Optional<User> user = userRepository.findByTelephone(oTPRequests.getPhoneNumber());

            if (user.isPresent()) {
                emailService.sendHtmlMessage(user.get().getEmail(), "Otp ", message,
                        null);
            }

            return ResponseEntity.ok(Collections.singletonMap(
                    "message",
                    "Votre code OTP a été envoyé par SMS ou par e-mail. Veuillez vérifier vos messages pour obtenir le code de validation."));

        } catch (UsernameNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Utilisateur non trouvé"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Échec d'envoi: " + e.getMessage()));
        }
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
