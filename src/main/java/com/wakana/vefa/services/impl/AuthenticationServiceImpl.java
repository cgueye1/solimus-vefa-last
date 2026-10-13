package com.wakana.vefa.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import com.wakana.vefa.exception.AccountLockedException;
import com.wakana.vefa.security.LoginAttemptService;
import com.wakana.vefa.security.PasswordValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wakana.vefa.dto.ChangePasswordRequest;
import com.wakana.vefa.dto.JwtAuthenticationResponse;
import com.wakana.vefa.dto.RefreshTokenRequest;
import com.wakana.vefa.dto.ReservationRequest;
import com.wakana.vefa.dto.SignUpRequest;
import com.wakana.vefa.dto.SigninRequest;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.AddressLot;
import com.wakana.vefa.model.Company;
import com.wakana.vefa.model.Reservation;
import com.wakana.vefa.model.SolimusJobs;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.SubscriptionPlanRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.AuthenticationService;
import com.wakana.vefa.services.CompanyService;
import com.wakana.vefa.services.EmailService;
import com.wakana.vefa.services.JWTService;
import com.wakana.vefa.services.OwnerPropertyAreaService;
import com.wakana.vefa.services.RealEstatePropertyService;
import com.wakana.vefa.services.ReservationService;
import com.wakana.vefa.services.SolimusJobsService;
import com.wakana.vefa.services.SubscriptionService;
import com.wakana.vefa.util.PasswordUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JWTService jwtService;
    private final CompanyService companyService;
    private final SubscriptionService subscriptionService;
    private final RealEstatePropertyRepository realEstatePropertyRepository;
    private final OwnerPropertyAreaService ownerPropertyAreaService;
    private final RealEstatePropertyService realEstatePropertyService;
    private final SolimusJobsService solimusJobsService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    // ── Nouveaux services sécurité ────────────────────────────────────────────
    private final PasswordValidator passwordValidator;
    private final LoginAttemptService loginAttemptService;

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private EmailService emailService;

    // ─────────────────────────────────────────────────────────────────────────
    // SIGN UP
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public User signUp(SignUpRequest signUpRequest) {

        // ── 1. Validation du mot de passe fort ────────────────────────────────
        passwordValidator.validate(signUpRequest.getPassword());

        // ── 2. Unicité email ──────────────────────────────────────────────────
        if (signUpRequest.getEmail() != null && !signUpRequest.getEmail().isBlank()) {
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                throw new IllegalArgumentException("Un utilisateur avec cet e-mail existe déjà.");
            }
        }

        // ── 3. Unicité téléphone ──────────────────────────────────────────────
        if (userRepository.findByTelephone(signUpRequest.getTelephone()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec ce numéro de téléphone existe déjà.");
        }

        // ── 4. Création et sauvegarde ─────────────────────────────────────────
        User user = User.fromSignUpRequest(signUpRequest);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        User savedUser = userRepository.save(user);
        userRepository.flush();

        log.info("[Auth] Nouveau compte créé : id={}, profil={}", savedUser.getId(), savedUser.getProfil());

        // ── 5. Assignation du job (PRESTATAIRE) ───────────────────────────────
        if (ProfilEnum.PRESTATAIRE.equals(savedUser.getProfil())) {
            SolimusJobs job = solimusJobsService.getJobById(signUpRequest.getJobId());
            savedUser.setJob(job);
            userRepository.save(savedUser);
        }

        // ── 6. Création de la société (profils pro) ───────────────────────────
        if (ProfilEnum.PROMOTEUR.equals(savedUser.getProfil())
                || ProfilEnum.NOTAIRE.equals(savedUser.getProfil())
                || ProfilEnum.AGENCY.equals(savedUser.getProfil())
                || ProfilEnum.BANK.equals(savedUser.getProfil())) {
            Company company = new Company();
            company.setName(signUpRequest.getCompagnyName());
            company.setOwner(savedUser);
            company.setLogo("");
            companyService.create(company);
        }

        // ── 7. Email de bienvenue (sans le mot de passe) ─────────────────────
        String html = buildWelcomeEmail(savedUser.getPrenom(), savedUser.getNom());
        safelySendEmail(signUpRequest.getEmail(), "Bienvenue sur Solimus", html);

        return savedUser;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAVE RÉSERVATAIRE
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public User saveReservataire(SignUpRequest signUpRequest, Long id) {

        // Validation du mot de passe fort uniquement pour un nouvel utilisateur
        boolean userExists = (signUpRequest.getEmail() != null && !signUpRequest.getEmail().isEmpty()
                && userRepository.findByEmail(signUpRequest.getEmail()).isPresent())
                || userRepository.findByTelephone(signUpRequest.getTelephone()).isPresent();

        User savedUser;

        if (userExists) {
            // Récupérer l'utilisateur existant
            User u1 = userRepository.findByTelephone(signUpRequest.getTelephone()).orElse(null);
            User u2 = (signUpRequest.getEmail() != null && !signUpRequest.getEmail().isEmpty())
                    ? userRepository.findByEmail(signUpRequest.getEmail()).orElse(null)
                    : null;
            savedUser = u1 != null ? u1 : (u2 != null ? u2 : new User());
        } else {
            // Nouveau user → validation mot de passe fort
            passwordValidator.validate(signUpRequest.getPassword());

            User user = User.fromSignUpRequest(signUpRequest);
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            savedUser = userRepository.save(user);
        }

        if (savedUser.getId() == null) {
            throw new IllegalStateException("L'utilisateur sauvegardé n'a pas d'ID : " + savedUser.getEmail());
        }

        // Créer la réservation
        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setPropertyId(id);
        reservationRequest.setUserId(savedUser.getId());
        reservationRequest.setEmail(signUpRequest.getEmail());
        reservationRequest.setTelephone(signUpRequest.getTelephone());

        Reservation reservation = reservationService.createReservation(reservationRequest, false);
        reservationService.valideReservation(reservation.getId(), false);

        return savedUser;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CREATE (copropriétaire, locataire…)
    // ─────────────────────────────────────────────────────────────────────────

    public User create(SignUpRequest signUpRequest) {
        User user = null;
        boolean isNewUser = false;

        if (signUpRequest.getEmail() != null) {
            user = userRepository.findByEmail(signUpRequest.getEmail()).orElse(null);
        }
        if (user == null && signUpRequest.getTelephone() != null) {
            user = userRepository.findByTelephone(signUpRequest.getTelephone()).orElse(null);
        }

        if (user == null) {
            user = User.fromSignUpRequest(signUpRequest);
            isNewUser = true;

            // Générer un mot de passe fort aléatoire
            String newPassword = PasswordUtil.generateRandomPassword();
            user.setPassword(passwordEncoder.encode(newPassword));

            // Stocker temporairement pour l'envoyer par email
            user = userRepository.save(user);

            if (user.getEmail() != null) {
                String html = buildTempPasswordEmail(user.getPrenom(), user.getNom(), newPassword);
                safelySendEmail(user.getEmail(), "Votre accès Solimus", html);
            }
        }

        if (user.getAdressLots() == null) {
            user.setAdressLots(new ArrayList<>());
        }

        AddressLot addressLot = new AddressLot();
        addressLot.setLot(signUpRequest.getAdress());
        addressLot.setPropertyId(signUpRequest.getPropretyId());
        addressLot.setArea(signUpRequest.getArea());
        user.getAdressLots().add(addressLot);

        User savedUser = userRepository.save(user);

        realEstatePropertyService.addOwner(
                signUpRequest.getPropretyId(),
                savedUser.getId(),
                signUpRequest.getArea());

        return savedUser;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SIGNIN
    // ─────────────────────────────────────────────────────────────────────────

    public JwtAuthenticationResponse signin(SigninRequest signinRequest) {
        String loginInput = signinRequest.getEmail();

        // ── Vérification brute-force ──────────────────────────────────────────
        if (loginAttemptService.isBlocked(loginInput)) {
            log.warn("[Auth] Compte bloqué temporairement : {}", loginInput);
            throw new AccountLockedException(
                    "Trop de tentatives échouées. Veuillez réessayer dans 15 minutes.");
        }

        // ── Recherche de l'utilisateur ────────────────────────────────────────
        User user;
        try {
            if (isEmail(loginInput)) {
                user = userRepository.findByEmail(loginInput)
                        .orElseThrow(() -> new IllegalArgumentException("Identifiants incorrects."));
            } else {
                user = userRepository.findByTelephone(loginInput)
                        .orElseThrow(() -> new IllegalArgumentException("Identifiants incorrects."));
            }
        } catch (IllegalArgumentException e) {
            loginAttemptService.loginFailed(loginInput);
            throw e;
        }

        // ── Compte actif ──────────────────────────────────────────────────────
        if (!user.isActivated()) {
            loginAttemptService.loginFailed(loginInput);
            throw new IllegalArgumentException("Votre compte est désactivé. Contactez l'administrateur.");
        }

        // ── Vérification du mot de passe ──────────────────────────────────────
        if (!passwordEncoder.matches(signinRequest.getPassword(), user.getPassword())) {
            loginAttemptService.loginFailed(loginInput);
            int remaining = loginAttemptService.remainingAttempts(loginInput);
            log.warn("[Auth] Mot de passe incorrect pour {} ({} tentatives restantes)", loginInput, remaining);

            String message = remaining > 0
                    ? "Identifiants incorrects. Il vous reste " + remaining + " tentative(s)."
                    : "Compte bloqué temporairement. Veuillez réessayer dans 15 minutes.";

            throw new IllegalArgumentException(message);
        }

        // ── Connexion réussie ─────────────────────────────────────────────────
        loginAttemptService.loginSucceeded(loginInput);
        log.info("[Auth] Connexion réussie : id={}, profil={}", user.getId(), user.getProfil());

        String jwt = jwtService.generateToken(user, user.getId(), user.getProfil());
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user, user.getId(), user.getProfil());

        JwtAuthenticationResponse response = new JwtAuthenticationResponse();
        response.setToken(jwt);
        response.setRefreshToken(refreshToken);
        return response;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CHANGE PASSWORD
    // ─────────────────────────────────────────────────────────────────────────

    public User changePassword(ChangePasswordRequest changePassword) {
        // Validation du nouveau mot de passe fort
        passwordValidator.validate(changePassword.getNewPassword());

        User user = userRepository.findByTelephone(changePassword.getEmail())
                .orElseGet(() -> userRepository.findByEmail(changePassword.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable.")));

        // Empêcher la réutilisation du même mot de passe
        if (passwordEncoder.matches(changePassword.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien.");
        }

        user.setPassword(passwordEncoder.encode(changePassword.getNewPassword()));
        log.info("[Auth] Mot de passe changé pour userId={}", user.getId());
        return userRepository.save(user);
    }

    public User changePassword(ChangePasswordRequest changePasswordRequest, long userId) {
        // Validation du nouveau mot de passe fort
        passwordValidator.validate(changePasswordRequest.getNewPassword());

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable."));

        // Empêcher la réutilisation du même mot de passe
        if (passwordEncoder.matches(changePasswordRequest.getNewPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Le nouveau mot de passe doit être différent de l'ancien.");
        }

        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));
        log.info("[Auth] Mot de passe changé pour userId={}", userId);
        return userRepository.save(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RESET PASSWORD
    // ─────────────────────────────────────────────────────────────────────────

    public User resetPassword(ChangePasswordRequest changePassword) {
        User user = userRepository.findByTelephone(changePassword.getEmail())
                .orElseGet(() -> userRepository.findByEmail(changePassword.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable.")));

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Aucune adresse e-mail n'est associée à cet utilisateur.");
        }

        // Générer un mot de passe fort
        String newPassword = PasswordUtil.generateRandomPassword();

        String html = buildTempPasswordEmail(user.getPrenom(), user.getNom(), newPassword);
        emailService.sendHtmlMessage(user.getEmail(), "Réinitialisation de votre mot de passe Solimus", html, null);

        user.setPassword(passwordEncoder.encode(newPassword));
        log.info("[Auth] Mot de passe réinitialisé pour userId={}", user.getId());
        return userRepository.save(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REFRESH TOKEN
    // ─────────────────────────────────────────────────────────────────────────

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String userEmail = jwtService.extractUserName(refreshTokenRequest.getToken());

        User user = userRepository.findByTelephone(userEmail)
                .orElseGet(() -> userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new IllegalArgumentException("Token invalide.")));

        if (!jwtService.isRefreshTokenValid(refreshTokenRequest.getToken(), user)) {
            throw new IllegalArgumentException("Token expiré ou invalide.");
        }

        String jwt = jwtService.generateToken(user, user.getId(), user.getProfil());

        JwtAuthenticationResponse response = new JwtAuthenticationResponse();
        response.setToken(jwt);
        response.setRefreshToken(jwtService.generateRefreshToken(new HashMap<>(), user, user.getId(), user.getProfil()));
        return response;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS PRIVÉS
    // ─────────────────────────────────────────────────────────────────────────

    private boolean isEmail(String input) {
        return input != null && input.contains("@");
    }

    private void safelySendEmail(String to, String subject, String html) {
        if (to == null || to.isBlank())
            return;
        try {
            emailService.sendHtmlMessage(to, subject, html, null);
        } catch (Exception e) {
            log.error("[Auth] Échec d'envoi d'email à {} : {}", to, e.getMessage());
        }
    }

    private String buildWelcomeEmail(String prenom, String nom) {
        return String.format(
                "<div style='font-family:sans-serif;max-width:480px;margin:auto'>" +
                        "<h2 style='color:#1e3a8a'>Bienvenue sur Solimus ✓</h2>" +
                        "<p>Bonjour <strong>%s %s</strong>,</p>" +
                        "<p>Votre compte a été créé avec succès. " +
                        "Vous pouvez dès maintenant vous connecter avec vos identifiants.</p>" +
                        "<p style='color:#6b7280;font-size:0.85em'>Pour votre sécurité, " +
                        "ne partagez jamais votre mot de passe.</p>" +
                        "<p>Cordialement,<br><strong>L'équipe Solimus</strong></p>" +
                        "</div>",
                prenom, nom);
    }

    private String buildTempPasswordEmail(String prenom, String nom, String tempPassword) {
        return String.format(
                "<div style='font-family:sans-serif;max-width:480px;margin:auto'>" +
                        "<h2 style='color:#1e3a8a'>Votre accès Solimus</h2>" +
                        "<p>Bonjour <strong>%s %s</strong>,</p>" +
                        "<p>Voici votre mot de passe temporaire :</p>" +
                        "<div style='background:#f4f7ff;border:1px solid #e2e8f5;border-radius:8px;" +
                        "padding:16px;text-align:center;font-size:1.3em;font-weight:bold;" +
                        "letter-spacing:2px;color:#1e3a8a;margin:16px 0'>%s</div>" +
                        "<p style='color:#dc2626'><strong>⚠ Changez ce mot de passe dès votre première connexion.</strong></p>"
                        +
                        "<p style='color:#6b7280;font-size:0.85em'>Si vous n'êtes pas à l'origine de cette demande, " +
                        "contactez-nous immédiatement.</p>" +
                        "<p>Cordialement,<br><strong>L'équipe Solimus</strong></p>" +
                        "</div>",
                prenom, nom, tempPassword);
    }
}