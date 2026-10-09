package com.wakana.vefa.services.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import com.wakana.vefa.dto.ChangePasswordRequest;
import com.wakana.vefa.dto.JwtAuthenticationResponse;
import com.wakana.vefa.dto.RefreshTokenRequest;
import com.wakana.vefa.dto.ReservationRequest;
import com.wakana.vefa.dto.SignUpRequest;
import com.wakana.vefa.dto.SigninRequest;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.enums.SubscriptionPlanEnum;
import com.wakana.vefa.model.AddressLot;
import com.wakana.vefa.model.Company;
import com.wakana.vefa.model.Reservation;
import com.wakana.vefa.model.SolimusJobs;
import com.wakana.vefa.model.SubscriptionPlan;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.SubscriptionPlanRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.AuthenticationService;
import com.wakana.vefa.services.CompanyService;
import com.wakana.vefa.services.EmailService;
import com.wakana.vefa.services.JWTService;
import com.wakana.vefa.services.LamSmsService;
import com.wakana.vefa.services.OwnerPropertyAreaService;
import com.wakana.vefa.services.RealEstatePropertyService;
import com.wakana.vefa.services.ReservationService;
import com.wakana.vefa.services.SolimusJobsService;
import com.wakana.vefa.services.SubscriptionService;
import com.wakana.vefa.util.PasswordUtil;

import lombok.RequiredArgsConstructor;

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
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EmailService emailService;
    private final LamSmsService lamSmsService;

    public static String generateUniqueCardNumber(long userId) {
        long shiftedId = userId + 100000000;
        String userIdString = Long.toString(shiftedId);
        String uniqueCardNumber = userIdString.substring(userIdString.length() - 9);
        return uniqueCardNumber;
    }

    @Transactional
    public User signUp(SignUpRequest signUpRequest) {
        if (signUpRequest.getEmail() != null && userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {

            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà.");
        }
        if (userRepository.findByTelephone(signUpRequest.getTelephone()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec ce numéro de téléphone existe déjà.");
        }
        if (signUpRequest.getEmail() != null && !signUpRequest.getEmail().isBlank()) {
            if (userRepository.existsByEmail(signUpRequest.getEmail())) {
                throw new IllegalArgumentException("Un utilisateur avec cet e-mail existe déjà.");
            }
        }
        // Création de l'utilisateur
        User user = User.fromSignUpRequest(signUpRequest);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));

        // Sauvegarder l'utilisateur
        User savedUser = userRepository.save(user);
        userRepository.flush();

        if (ProfilEnum.PRESTATAIRE.equals(savedUser.getProfil())) {
            SolimusJobs job = solimusJobsService.getJobById(signUpRequest.getJobId());
            savedUser.setJob(job);
            userRepository.save(savedUser);

        }

        // Si l'utilisateur est un promoteur ou un notaire, créer une société
        if (ProfilEnum.PROMOTEUR.equals(savedUser.getProfil()) || ProfilEnum.NOTAIRE.equals(savedUser.getProfil())
                || ProfilEnum.AGENCY.equals(savedUser.getProfil()) || ProfilEnum.BANK.equals(savedUser.getProfil())) {
            Company company = new Company();
            company.setName(signUpRequest.getCompagnyName()); // Définir un nom d'entreprise par défaut si nécessaire
            company.setOwner(savedUser); // Associer l'utilisateur comme propriétaire
            company.setLogo("");
            companyService.create(company); // Sauvegarder la société

        }

        /*
         * if (ProfilEnum.PROMOTEUR.equals(savedUser.getProfil())) {
         * SubscriptionPlan subscriptionPlan = subscriptionPlanRepository
         * .findByName(SubscriptionPlanEnum.PROMOTEUR_PLAN)
         * .orElseThrow(() -> new RuntimeException("SubscriptionPlan not found "));
         * 
         * subscriptionService.createSubscription(savedUser, subscriptionPlan.getId());
         * 
         * } else if (ProfilEnum.NOTAIRE.equals(savedUser.getProfil())) {
         * SubscriptionPlan subscriptionPlan =
         * subscriptionPlanRepository.findByName(SubscriptionPlanEnum.NOTAIRE_PLAN)
         * .orElseThrow(() -> new RuntimeException("SubscriptionPlan not found "));
         * subscriptionService.createSubscription(savedUser, subscriptionPlan.getId());
         * 
         * } else if (ProfilEnum.BANK.equals(savedUser.getProfil())) {
         * SubscriptionPlan subscriptionPlan =
         * subscriptionPlanRepository.findByName(SubscriptionPlanEnum.BANK_PLAN)
         * .orElseThrow(() -> new RuntimeException("SubscriptionPlan not found "));
         * subscriptionService.createSubscription(savedUser, subscriptionPlan.getId());
         * 
         * } else if (ProfilEnum.AGENCY.equals(savedUser.getProfil())) {
         * SubscriptionPlan subscriptionPlan =
         * subscriptionPlanRepository.findByName(SubscriptionPlanEnum.AGENCY_PLAN)
         * .orElseThrow(() -> new RuntimeException("SubscriptionPlan not found "));
         * subscriptionService.createSubscription(savedUser, subscriptionPlan.getId());
         * 
         * } else if (ProfilEnum.SYNDIC.equals(savedUser.getProfil())) {
         * SubscriptionPlan subscriptionPlan =
         * subscriptionPlanRepository.findByName(SubscriptionPlanEnum.SYNDIC_PLAN)
         * .orElseThrow(() -> new RuntimeException("SubscriptionPlan not found "));
         * subscriptionService.createSubscription(savedUser, subscriptionPlan.getId());
         * 
         * }
         */

        String message = "Votre mot de passe temporaire est : " + signUpRequest.getPassword()
                + ". Veuillez le modifier dès votre première connexion.";

        /*
         * String html = String.format(
         * "<p>Bonjour %s %s</p>" +
         * "<p>Votre mot de passe temporaire est : <strong>%s</strong></p>" +
         * "<p>Cordialement,<br>L'équipe de SOLIMUS</p>",
         * signUpRequest.getPrenom(), signUpRequest.getNom(),
         * signUpRequest.getPassword());
         */

        String html = String.format(
                "<p>Bonjour %s %s</p>" +
                        "<p>Votre compte Solimus a été créé avec succès.</p>" +
                        "<p>Cordialement,<br>L'équipe de SOLIMUS</p>",
                signUpRequest.getPrenom(), signUpRequest.getNom());

        // lamSmsService.sendSms(signUpRequest.getTelephone(), message);
        // Envoi d'un email
        emailService.sendHtmlMessage(signUpRequest.getEmail(), "Compte solimus", html,
                null);

        return savedUser;
    }

    @Transactional
    public User saveReservataire(SignUpRequest signUpRequest, Long id) {
        User savedUser = new User();

        // Vérification de l'email et du téléphone
        if ((signUpRequest.getEmail() != null && !signUpRequest.getEmail().isEmpty()
                && (userRepository.findByEmail(signUpRequest.getEmail()).isPresent())
                || userRepository.findByTelephone(signUpRequest.getTelephone()).isPresent())) {

            // Chercher par téléphone et email
            User u1 = userRepository.findByTelephone(signUpRequest.getTelephone()).orElse(null);
            User u2 = new User();
            if (!signUpRequest.getEmail().isEmpty()) {
                u2 = userRepository.findByEmail(signUpRequest.getEmail()).orElse(null);
            }

            if (u1 != null) {
                savedUser = u1;
            } else if (u2 != null) {
                savedUser = u2;
            }

        } else {
            // Création de l'utilisateur
            User user = User.fromSignUpRequest(signUpRequest);
            user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
            // Sauvegarder l'utilisateur
            savedUser = userRepository.save(user);
        }

        // Vérifier que l'utilisateur a bien un ID
        if (savedUser.getId() == null) {
            throw new IllegalStateException("L'utilisateur sauvegardé n'a pas d'ID" + savedUser.getEmail());
        }

        ReservationRequest reservationRequest = new ReservationRequest();
        reservationRequest.setPropertyId(id);
        reservationRequest.setUserId(savedUser.getId());
        reservationRequest.setEmail(signUpRequest.getEmail());
        reservationRequest.setTelephone(signUpRequest.getTelephone());

        // Création et validation de la réservation
        Reservation reservation = reservationService.createReservation(reservationRequest, false);

        reservationService.valideReservation(reservation.getId(), false);

        return savedUser;
    }

    public User create(SignUpRequest signUpRequest) {

        User user = null;
        boolean isNewUser = false;

        // 1️⃣ Recherche par email
        if (signUpRequest.getEmail() != null) {
            user = userRepository.findByEmail(signUpRequest.getEmail()).orElse(null);
        }

        // 2️⃣ Recherche par téléphone (si pas trouvé par email)
        if (user == null && signUpRequest.getTelephone() != null) {
            user = userRepository.findByTelephone(signUpRequest.getTelephone()).orElse(null);
        }

        // 3️⃣ Création si inexistant
        if (user == null) {
            user = User.fromSignUpRequest(signUpRequest);
            isNewUser = true;

            String newPassword = PasswordUtil.generateRandomPassword();
            user.setPassword(passwordEncoder.encode(newPassword));
        }

        // 4️⃣ Initialisation adressLots si nécessaire
        if (user.getAdressLots() == null) {
            user.setAdressLots(new ArrayList<>());
        }

        // 5️⃣ Ajout TOUJOURS du AddressLot
        AddressLot addressLot = new AddressLot();
        addressLot.setLot(signUpRequest.getAdress());
        addressLot.setPropertyId(signUpRequest.getPropretyId());
        addressLot.setArea(signUpRequest.getArea());
        user.getAdressLots().add(addressLot);

        // 6️⃣ Sauvegarde du user (nouveau ou existant)
        User savedUser = userRepository.save(user);

        // 7️⃣ Ajout comme owner (idempotent côté service si bien fait)
        realEstatePropertyService.addOwner(
                signUpRequest.getPropretyId(),
                savedUser.getId(),
                signUpRequest.getArea());

        // 8️⃣ Envoi mail uniquement si nouveau user
        if (isNewUser && savedUser.getEmail() != null) {
            String html = String.format(
                    "<p>Bonjour %s %s</p>" +
                            "<p>Votre mot de passe temporaire est : <strong>%s</strong></p>" +
                            "<p>Cordialement,<br>L'équipe de SOLIMUS</p>",
                    savedUser.getPrenom(),
                    savedUser.getNom(),
                    "******");

            emailService.sendHtmlMessage(
                    savedUser.getEmail(),
                    "Mot de passe",
                    html,
                    null);
        }

        return savedUser;
    }

    public User changePassword(ChangePasswordRequest changePassword) {
        User user = userRepository.findByTelephone(changePassword.getEmail())
                .orElseGet(() -> userRepository.findByEmail(changePassword.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid email or call")));
        user.setPassword(passwordEncoder.encode(changePassword.getNewPassword()));

        return userRepository.save(user);

    }

    public User resetPassword(ChangePasswordRequest changePassword) {
        User user = userRepository.findByTelephone(changePassword.getEmail())
                .orElseGet(() -> userRepository.findByEmail(changePassword.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("Invalid email or call")));
        String newPassword = PasswordUtil.generateRandomPassword();

        user.setPassword(passwordEncoder.encode(newPassword));
        String html = String.format(
                "<p>Bonjour %s %s</p>" +
                        "<p>Votre mot de passe temporaire est : <strong>%s</strong></p>" +
                        "<p>Cordialement,<br>L'équipe de SOLIMUS</p>",
                user.getPrenom(), user.getNom(), newPassword);
        emailService.sendHtmlMessage(changePassword.getEmail(), "Mot de passe ", html,
                null);
        return userRepository.save(user);

    }

    public JwtAuthenticationResponse signin(SigninRequest signinRequest) {
        String loginInput = signinRequest.getEmail();
        String rawPassword = signinRequest.getPassword();

        User user;
        if (isEmail(loginInput)) {
            user = userRepository.findByEmail(loginInput)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        } else {
            user = userRepository.findByTelephone(loginInput)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid telephone number or password"));
        }

        if (!user.isActivated()) {
            throw new IllegalArgumentException("Votre compte est bloqué");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email/telephone or password");
        }

        String jwt = jwtService.generateToken(user, user.getId(), user.getProfil());
        String refreshToken = jwtService.generateRefreshToken(new HashMap<>(), user, user.getId(), user.getProfil());
        JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
        jwtAuthenticationResponse.setToken(jwt);
        jwtAuthenticationResponse.setRefreshToken(refreshToken);

        return jwtAuthenticationResponse;
    }

    private boolean isEmail(String input) {
        return input != null && input.contains("@");
    }

    public JwtAuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String userEmail = jwtService.extractUserName(refreshTokenRequest.getToken());
        User user = userRepository.findByTelephone(userEmail).orElseThrow();
        if (jwtService.isTokenValid(refreshTokenRequest.getToken(), user)) {
            var jwt = jwtService.generateToken(user, user.getId(), user.getProfil());
            JwtAuthenticationResponse jwtAuthenticationResponse = new JwtAuthenticationResponse();
            jwtAuthenticationResponse.setToken(jwt);
            jwtAuthenticationResponse.setRefreshToken(refreshTokenRequest.getToken());
            return jwtAuthenticationResponse;

        }
        return null;

    }

    public User changePassword(ChangePasswordRequest changePasswordRequest, long userId) {
        // Fetch user by ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Debugging: Print current password and encoded password to see why it's
        // failing
        /*
         * System.out.println("Current Password (raw): " +
         * changePasswordRequest.getPassword());
         * System.out.println("Current Password (encoded in DB): " +
         * user.getPassword());
         * 
         * // Verify that the current password matches the user's existing password
         * if (!passwordEncoder.matches(changePasswordRequest.getPassword(),
         * user.getPassword())) {
         * throw new RuntimeException("Current password is incorrect");
         * }
         * 
         * // Check if the new password is different from the current password
         * if (passwordEncoder.matches(changePasswordRequest.getNewPassword(),
         * user.getPassword())) {
         * throw new
         * RuntimeException("New password cannot be the same as the current password");
         * }
         */

        // Set and encode the new password
        user.setPassword(passwordEncoder.encode(changePasswordRequest.getNewPassword()));

        // Save and return the updated user
        return userRepository.save(user);
    }

}
