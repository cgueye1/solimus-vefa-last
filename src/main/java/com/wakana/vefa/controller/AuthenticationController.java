
package com.wakana.vefa.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.wakana.vefa.dto.ChangePasswordRequest;
import com.wakana.vefa.dto.JwtAuthenticationResponse;
import com.wakana.vefa.dto.RefreshTokenRequest;
import com.wakana.vefa.dto.SignUpRequest;
import com.wakana.vefa.dto.SigninRequest;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.AuthenticationService;
import java.util.Collections;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    @Autowired
    private UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationService authenticationService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest signUpRequest) {

        User savedUser = authenticationService.signUp(signUpRequest);
        return ResponseEntity.ok(savedUser);

    }

    @PostMapping("/reservataire/{id}")
    public ResponseEntity<?> saveReservataire(@RequestBody SignUpRequest signUpRequest, @PathVariable Long id) {
        try {
            User savedUser = authenticationService.saveReservataire(signUpRequest, id);
            return ResponseEntity.ok(savedUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // JwtAuthenticationResponse
    @PostMapping("/signin")

    public ResponseEntity<?> signin(@RequestBody SigninRequest signInRequest) {

        return ResponseEntity.ok(authenticationService.signin(signInRequest));

    }

    @PutMapping("/password/change/{id}")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest,
            @PathVariable Long id, Authentication authentication) {
        try {
            changePasswordRequest.setEmail(authentication.getName());
            User updatedUser = authenticationService.changePassword(changePasswordRequest);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating password");
        }
    }

    @PostMapping("/password/reset")

    public ResponseEntity<?> resetPassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
       /*  String errorMessage = "Le numero de telephone est incorrecte";
        User user = userRepository.findByTelephone(changePasswordRequest.getEmail())
                .orElseGet(() -> userRepository.findByEmail(changePasswordRequest.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid email or password")));

        if (null == user) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(Collections.singletonMap("error", errorMessage));
        }*/

        authenticationService.resetPassword(changePasswordRequest);

        return ResponseEntity
                .ok("Votre nouveau mot de passe est envoyé sur l'adresse mail :" + changePasswordRequest.getEmail());

    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtAuthenticationResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authenticationService.refreshToken(refreshTokenRequest));

    }

}
