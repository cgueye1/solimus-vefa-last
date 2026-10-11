package com.wakana.vefa.services.impl;

import com.wakana.vefa.dto.ReservationRequest;
import com.wakana.vefa.dto.ReservationStats;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.enums.RealEstatePropertyStatusEnum;
import com.wakana.vefa.enums.ReservationStatus;
import com.wakana.vefa.model.Reservation;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.ReservationRepository;
import com.wakana.vefa.repository.NotificationsRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.EmailService;
import com.wakana.vefa.services.NotificationsService;
import com.wakana.vefa.services.ReservationService;
import com.wakana.vefa.exception.BusinessException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import org.springframework.http.HttpStatus;

@Service
@Slf4j
public class ReservationServiceImpl implements ReservationService {

        @Autowired
        private NotificationsRepository notificationsRepository;

        @Autowired
        private ReservationRepository reservationRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private RealEstatePropertyRepository propertyRepository;
        @Autowired
        private EmailService emailService;

        @Autowired
        private NotificationsService notificationsService;
        @Autowired
        private RealEstatePropertyRepository realEstatePropertyRepository;

        @Transactional
        @Override
        public Reservation createReservation(ReservationRequest request, boolean sendNotif) {

                log.info("▶️ Début création réservation | request={}", request);
                System.out.println("▶️ Début création réservation | request={}");
                try {
                        // =========================
                        // 1. Validation minimale
                        // =========================
                        if (request.getTelephone() == null || request.getTelephone().isBlank()) {
                                log.error("❌ Téléphone manquant dans la requête");
                                throw new IllegalArgumentException("Le numéro de téléphone est obligatoire");
                        }

                        // =========================
                        // 2. Récupération ou création utilisateur
                        // =========================
                        User user = userRepository.findByTelephone(request.getTelephone())
                                        .or(() -> {
                                                if (request.getEmail() != null && !request.getEmail().isBlank()) {
                                                        return userRepository.findByEmail(request.getEmail());
                                                }
                                                return Optional.empty();
                                        })
                                        .orElseGet(() -> {
                                                log.info("ℹ️ Aucun utilisateur trouvé → création d’un nouveau");
                                                return createNewUser(request);
                                        });

                        log.info("✅ Utilisateur utilisé | id={} profil={}", user.getId(), user.getProfil());

                        // =========================
                        // 3. Vérification profil
                        // =========================
                        if (user.getProfil() != ProfilEnum.RESERVATAIRE &&
                                        user.getProfil() != ProfilEnum.TENANT) {

                                log.warn("🚫 Profil non autorisé | userId={} profil={}",
                                                user.getId(), user.getProfil());

                                throw new IllegalStateException(
                                                "Vous n’êtes pas autorisé à effectuer cette action.");
                        }

                        // =========================
                        // 4. Récupération du bien
                        // =========================
                        RealEstateProperty property = propertyRepository.findById(request.getPropertyId())
                                        .orElseThrow(() -> {
                                                log.error("❌ Bien introuvable | propertyId={}",
                                                                request.getPropertyId());
                                                return new IllegalArgumentException("Bien immobilier introuvable");
                                        });

                        if (property.getStatus() != RealEstatePropertyStatusEnum.AVAILABLE) {
                                throw new BusinessException("PROPERTY_NOT_AVAILABLE",
                                                "Ce lot n'est plus disponible.", HttpStatus.CONFLICT);
                        }

                        log.info("🏠 Bien trouvé | id={} name={}", property.getId(), property.getName());

                        // =========================
                        // 5. Vérification réservation existante
                        // =========================
                        Optional<Reservation> existingReservation = reservationRepository
                                        .findByPropertyAndUser(property, user);

                        if (existingReservation.isPresent()
                                        && (existingReservation.get().getStatus() == ReservationStatus.PROSPECT
                                                        || existingReservation.get().getStatus() == ReservationStatus.RESERVED)) {
                                log.warn("⚠️ Réservation déjà existante | reservationId={}",
                                                existingReservation.get().getId());

                                if (sendNotif) {
                                        throw new IllegalStateException(
                                                        "Vous avez déjà manifesté votre intérêt pour ce bien");
                                }
                                return existingReservation.get();
                        }

                        // =========================
                        // 6. Création réservation
                        // =========================
                        Reservation reservation = new Reservation();
                        reservation.setUser(user);
                        reservation.setProperty(property);
                        reservation.setStatus(ReservationStatus.PROSPECT);
                        
                        reservation.setCreatedAt(System.currentTimeMillis());

                        Reservation savedReservation = reservationRepository.save(reservation);

                        log.info("✅ Réservation créée | id={}", savedReservation.getId());

                        // =========================
                        // 7. Notifications
                        // =========================
                        if (sendNotif) {
                                log.info("📢 Envoi notifications");
                                sendReservationNotifications(user, property);
                        }

                        log.info("✔️ Fin création réservation OK");
                        return savedReservation;

                } catch (Exception e) {
                        log.error("💥 ERREUR création réservation | message={}", e.getMessage(), e);
                        throw e; // IMPORTANT : laisser Spring gérer la réponse HTTP
                }
        }

        private void sendReservationNotifications(User user, RealEstateProperty property) {

                String message = user.getPrenom() + " " + user.getNom()
                                + " vient de faire une manifestation d’intérêt pour le bien "
                                + property.getName();

                String htmlContent = emailService.getPropertyHTML(
                                property,
                                "Manifestation d'intérêt",
                                message,
                                "#0f056b");

                emailService.sendHtmlMessage(
                                property.getPromoter().getEmail(),
                                "Manifestation d'intérêt",
                                htmlContent,
                                null);

                notificationsService.sendReservationNotif(
                                "Manifestation d'intérêt",
                                message,
                                property,
                                property.getPromoter());

                if (property.getAgency() != null) {
                        notificationsService.sendReservationNotif(
                                        "Manifestation d'intérêt",
                                        message,
                                        property,
                                        property.getAgency());
                }
        }

        private User createNewUser(ReservationRequest request) {
                User user = new User();

                user.setProfil(request.isTenant()
                                ? ProfilEnum.TENANT
                                : ProfilEnum.RESERVATAIRE);

                user.setPrenom(request.getPrenom());
                user.setNom(request.getNom());
                user.setEmail(request.getEmail());
                user.setTelephone(request.getTelephone());
                user.setActivated(true);
                // passwordEncoder.encode("TEMP_PASSWORD")
                // ⚠️ À remplacer par un vrai mot de passe hashé ou OTP
                user.setPassword("dsjldskdlsdqlkqsdqjsdk");

                return userRepository.save(user);
        }

        @Override
        public Reservation updateReservation(Long id, ReservationRequest reservationRequest) {
                Reservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Reservation not found"));

                if (reservationRequest.getUserId() != 0) {
                        User user = userRepository.findById(reservationRequest.getUserId())
                                        .orElseThrow(() -> new RuntimeException("User not found"));
                        reservation.setUser(user);
                }

                RealEstateProperty property = propertyRepository.findById(reservationRequest.getPropertyId())
                                .orElseThrow(() -> new RuntimeException("Property not found"));

                reservation.setProperty(property);

                return reservationRepository.save(reservation);
        }

        @Override
        @Transactional
        public void deleteReservation(Long id) {
                Reservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Reservation not found"));

                if (reservation.getStatus() == ReservationStatus.RESERVED) {
                        reservation.getProperty().setRecipient(null);
                        reservation.getProperty().restoreAvailability();
                        realEstatePropertyRepository.save(reservation.getProperty());
                }

                reservationRepository.deleteById(id);
        }

        @Override
        public Reservation getReservationById(Long id) {
                return reservationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        }

        @Override
        public List<Reservation> getAllReservations() {
                // Get all reservations ordered by creation date (most recent first)
                return reservationRepository.findAllByOrderByCreatedAtDesc();
        }

        @Override
        public Page<Reservation> getReservationsByParentProperty(Long parentPropertyId, Pageable pageable) {
                return reservationRepository.findByPropertyParentPropertyIdOrderByCreatedAtDesc(parentPropertyId,
                                pageable);
        }

        @Override
        public Page<Reservation> getReservationsByUserId(Long userId, Pageable pageable) {
                return reservationRepository.findByUserIdAndProperty_RentalFalseOrderByCreatedAtDesc(userId, pageable);
        }

        @Override
        public Page<Reservation> getRentalReservationsByUserId(Long userId, Pageable pageable) {
                return reservationRepository
                                .findByUserIdAndProperty_RentalTrueOrderByCreatedAtDesc(userId, pageable);
        }

        @Override
        public Page<Reservation> getReservationsByLotId(Long lotId, Pageable pageable) {
                return reservationRepository.findByPropertyIdOrderByCreatedAtDesc(lotId, pageable);

        }

        @Transactional
        @Override
        public Reservation valideReservation(Long id, boolean canSend) {
                // Récupérer la réservation en vérifiant si elle existe
                Reservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Reservation with ID " + id + " not found"));

                // Vérifier si la réservation est déjà validée pour éviter de refaire le
                // traitement
                if (reservation.getStatus() == ReservationStatus.RESERVED) {
                        throw new IllegalStateException("Reservation is already reserved.");
                }

                // Vérifier que l'utilisateur existe dans la table 'user_vefa'
                User recipient = userRepository.findById(reservation.getUser().getId())
                                .orElseThrow(
                                                () -> new RuntimeException("User with ID "
                                                                + reservation.getUser().getId() + " not found"));

                // Mettre à jour le statut de la réservation
                reservation.setStatus(ReservationStatus.RESERVED);
                reservation.setValidatedAt(LocalDateTime.now());

                // Associer l'utilisateur comme le destinataire du bien immobilier
                reservation.getProperty().setRecipient(recipient);
                reservation.getProperty().changeStatus(RealEstatePropertyStatusEnum.RESERVED);

                // Sauvegarder le bien immobilier avec le nouveau destinataire
                propertyRepository.save(reservation.getProperty());

                // Supprimer toutes les autres réservations pour le même bien
                List<Reservation> otherReservations = reservationRepository
                                .findByPropertyAndIdNot(reservation.getProperty(), reservation.getId());
                reservationRepository.deleteAll(otherReservations);

                if (canSend && !reservation.getProperty().isRental()) {
                        String subject = emailService.getPropertyHTML(
                                        reservation.getProperty(),
                                        "Réservation validée",
                                        "Félicitations ! Votre réservation pour le bien <b>"
                                                        + reservation.getProperty().getName()
                                                        + "</b> a été validée avec succès. " +
                                                        "Nous vous remercions pour votre confiance et vous tiendrons informé(e) des prochaines étapes.",
                                        "#062b16");
                        List<String> ccEmails = Arrays.asList(reservation.getProperty().getPromoter().getEmail(),
                                        reservation.getProperty().getNotary().getEmail());
                        emailService.sendHtmlMessage(recipient.getEmail(), "Réservation validée", subject, ccEmails);

                }

                notificationsService.sendReservationNotif(
                                "Réservation validée",
                                "Félicitations ! Votre réservation pour le bien "
                                                + (reservation.getProperty().getName().isEmpty()
                                                                ? reservation.getProperty().getPropertyType()
                                                                                .getTypeName()
                                                                : reservation.getProperty().getName())
                                                + " a été validée avec succès.",
                                reservation.getProperty(),
                                recipient);

                return reservationRepository.save(reservation);
        }

        @Transactional
        @Override
        public Reservation validateReservation(Long id, User promoter, boolean canSend) {
                Reservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new BusinessException("RESERVATION_NOT_FOUND",
                                                "Réservation introuvable.", HttpStatus.NOT_FOUND));
                RealEstateProperty property = reservation.getProperty();

                if (promoter.getProfil() != ProfilEnum.PROMOTEUR
                                || property.getPromoter() == null
                                || !promoter.getId().equals(property.getPromoter().getId())) {
                        throw new BusinessException("RESERVATION_NOT_AUTHORIZED",
                                        "Le promoteur connecté ne possède pas ce lot.", HttpStatus.FORBIDDEN);
                }
                if (reservation.getStatus() != ReservationStatus.PROSPECT) {
                        throw new BusinessException("RESERVATION_ALREADY_PROCESSED",
                                        "Cette réservation n'est plus en attente.", HttpStatus.CONFLICT);
                }
                if (property.getStatus() != RealEstatePropertyStatusEnum.AVAILABLE) {
                        throw new BusinessException("PROPERTY_NOT_AVAILABLE",
                                        "Ce lot n'est plus disponible.", HttpStatus.CONFLICT);
                }

                reservation.setStatus(ReservationStatus.RESERVED);
                reservation.setValidatedAt(LocalDateTime.now());
                property.setRecipient(reservation.getUser());
                property.changeStatus(RealEstatePropertyStatusEnum.RESERVED);
                propertyRepository.save(property);

                reservationRepository.findByProperty(property).stream()
                                .filter(other -> !other.getId().equals(reservation.getId()))
                                .filter(other -> other.getStatus() == ReservationStatus.PROSPECT)
                                .forEach(other -> {
                                        other.setStatus(ReservationStatus.REJECTED);
                                        other.setRejectedAt(LocalDateTime.now());
                                        other.setRejectionReason("Une autre réservation du lot a été validée.");
                                        reservationRepository.save(other);
                                });

                return reservationRepository.save(reservation);
        }

        @Transactional
        @Override
        public Reservation rejectReservation(Long id, User promoter, String reason) {
                Reservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new BusinessException("RESERVATION_NOT_FOUND",
                                                "Réservation introuvable.", HttpStatus.NOT_FOUND));
                RealEstateProperty property = reservation.getProperty();
                if (promoter.getProfil() != ProfilEnum.PROMOTEUR
                                || property.getPromoter() == null
                                || !promoter.getId().equals(property.getPromoter().getId())) {
                        throw new BusinessException("RESERVATION_NOT_AUTHORIZED",
                                        "Le promoteur connecté ne possède pas ce lot.", HttpStatus.FORBIDDEN);
                }
                if (reservation.getStatus() != ReservationStatus.PROSPECT) {
                        throw new BusinessException("RESERVATION_ALREADY_PROCESSED",
                                        "Seule une réservation en attente peut être refusée.", HttpStatus.CONFLICT);
                }
                reservation.setStatus(ReservationStatus.REJECTED);
                reservation.setRejectedAt(LocalDateTime.now());
                reservation.setRejectionReason(reason);
                return reservationRepository.save(reservation);
        }

        @Transactional
        @Override
        public Reservation cancelReservation(Long id, User reservataire) {
                Reservation reservation = reservationRepository.findById(id)
                                .orElseThrow(() -> new BusinessException("RESERVATION_NOT_FOUND",
                                                "Réservation introuvable.", HttpStatus.NOT_FOUND));
                if (reservataire.getProfil() != ProfilEnum.RESERVATAIRE
                                || reservation.getUser() == null
                                || !reservataire.getId().equals(reservation.getUser().getId())) {
                        throw new BusinessException("RESERVATION_NOT_AUTHORIZED",
                                        "Cette réservation n'appartient pas au réservataire connecté.",
                                        HttpStatus.FORBIDDEN);
                }
                if (reservation.getStatus() != ReservationStatus.PROSPECT) {
                        throw new BusinessException("RESERVATION_ALREADY_PROCESSED",
                                        "Seule une réservation en attente peut être annulée.", HttpStatus.CONFLICT);
                }
                reservation.setStatus(ReservationStatus.CANCELLED);
                reservation.setCancelledAt(LocalDateTime.now());
                return reservationRepository.save(reservation);
        }

        @Override
        public List<ReservationStats> getReservationsByMonth(
                        int year,
                        Long promoterId,
                        String planName) {

                User user = userRepository.findById(promoterId)
                                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

                ProfilEnum planEnum = ProfilEnum.valueOf(planName.toUpperCase());

                LocalDateTime startOfYear = LocalDateTime.of(year, Month.JANUARY, 1, 0, 0);
                LocalDateTime endOfYear = LocalDateTime.of(year, Month.DECEMBER, 31, 23, 59);

                // Mapping des méthodes selon le profil utilisateur
                Map<ProfilEnum, Function<Long, List<Reservation>>> searchMethods = Map.of(
                                ProfilEnum.PROMOTEUR,
                                id -> reservationRepository
                                                .findByValidatedAtBetweenAndProperty_Promoter_IdAndProperty_Subscription_SubscriptionPlans_Name(
                                                                startOfYear, endOfYear, id, planEnum),

                                ProfilEnum.AGENCY,
                                id -> reservationRepository
                                                .findByValidatedAtBetweenAndProperty_ParentProperty_Agency_IdAndProperty_Subscription_SubscriptionPlans_Name(
                                                                startOfYear, endOfYear, id, planEnum),

                                ProfilEnum.BANK,
                                id -> reservationRepository
                                                .findByValidatedAtBetweenAndProperty_ParentProperty_Bank_IdAndProperty_Subscription_SubscriptionPlans_Name(
                                                                startOfYear, endOfYear, id, planEnum));

                List<Reservation> reservations = searchMethods
                                .getOrDefault(user.getProfil(), id -> Collections.emptyList())
                                .apply(promoterId);

                // Grouper par mois
                Map<Integer, Long> reservationsByMonth = reservations.stream()
                                .collect(Collectors.groupingBy(
                                                r -> r.getValidatedAt().getMonthValue(),
                                                Collectors.counting()));

                // Construire la réponse sur 12 mois
                return IntStream.rangeClosed(1, 12)
                                .mapToObj(month -> new ReservationStats(
                                                month,
                                                reservationsByMonth.getOrDefault(month, 0L).intValue()))
                                .collect(Collectors.toList());
        }

        /*
         * public List<ReservationStats> getReservationsByMonth(int year, Long
         * promoterId) {
         * User user = userRepository.findById(promoterId)
         * .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
         * 
         * LocalDateTime startOfYear = LocalDateTime.of(year, Month.JANUARY, 1, 0, 0);
         * LocalDateTime endOfYear = LocalDateTime.of(year, Month.DECEMBER, 31, 23, 59);
         * 
         * // Mapping des méthodes de recherche selon le profil utilisateur
         * Map<ProfilEnum, Function<Long, List<Reservation>>> searchMethods = Map.of(
         * ProfilEnum.PROMOTEUR,
         * id -> reservationRepository.findByValidatedAtBetweenAndProperty_Promoter_Id(
         * startOfYear,
         * endOfYear, id),
         * ProfilEnum.AGENCY,
         * id -> reservationRepository
         * .findByValidatedAtBetweenAndProperty_ParentProperty_Agency_Id(
         * startOfYear,
         * endOfYear, id),
         * ProfilEnum.BANK, id -> reservationRepository
         * .findByValidatedAtBetweenAndProperty_ParentProperty_Bank_Id(startOfYear,
         * endOfYear, id));
         * 
         * List<Reservation> reservations = searchMethods
         * .getOrDefault(user.getProfil(), id -> Collections.emptyList())
         * .apply(promoterId);
         * 
         * // Grouper les réservations par mois
         * Map<Integer, Long> reservationsByMonth = reservations.stream()
         * .collect(Collectors.groupingBy(res -> res.getValidatedAt().getMonthValue(),
         * Collectors.counting()));
         * 
         * // Construire la liste des statistiques de réservations par mois
         * return IntStream.rangeClosed(1, 12)
         * .mapToObj(month -> new ReservationStats(month,
         * reservationsByMonth.getOrDefault(month, 0L).intValue()))
         * .collect(Collectors.toList());
         * }
         */

        public List<ReservationStats> getReservationsByDateAndParentProperty(
                        int year,
                        Long parentPropertyId,
                        Long promoterId,
                        String planName) {

                User user = userRepository.findById(promoterId)
                                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

                ProfilEnum planEnum = ProfilEnum.valueOf(planName.toUpperCase());

                LocalDateTime startOfYear = LocalDateTime.of(year, Month.JANUARY, 1, 0, 0);
                LocalDateTime endOfYear = LocalDateTime.of(year, Month.DECEMBER, 31, 23, 59);

                Map<ProfilEnum, Function<Long, List<Reservation>>> searchMethods = Map.of(
                                ProfilEnum.PROMOTEUR, id -> reservationRepository
                                                .findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_Promoter_IdAndProperty_Subscription_SubscriptionPlans_Name(
                                                                startOfYear, endOfYear, parentPropertyId, id, planEnum),

                                ProfilEnum.AGENCY, id -> reservationRepository
                                                .findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_ParentProperty_Agency_IdAndProperty_Subscription_SubscriptionPlans_Name(
                                                                startOfYear, endOfYear, parentPropertyId, id, planEnum),

                                ProfilEnum.BANK, id -> reservationRepository
                                                .findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_ParentProperty_Bank_IdAndProperty_Subscription_SubscriptionPlans_Name(
                                                                startOfYear, endOfYear, parentPropertyId, id,
                                                                planEnum));

                List<Reservation> reservations = searchMethods
                                .getOrDefault(user.getProfil(), id -> Collections.emptyList())
                                .apply(promoterId);

                Map<Integer, Long> reservationsByMonth = reservations.stream()
                                .collect(Collectors.groupingBy(
                                                r -> r.getValidatedAt().getMonthValue(),
                                                Collectors.counting()));

                return IntStream.rangeClosed(1, 12)
                                .mapToObj(month -> new ReservationStats(
                                                month,
                                                reservationsByMonth.getOrDefault(month, 0L).intValue()))
                                .collect(Collectors.toList());
        }

        /*
         * public List<ReservationStats> getReservationsByDateAndParentProperty(
         * int year, Long parentPropertyId, Long promoterId) {
         * 
         * User user = userRepository.findById(promoterId)
         * .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
         * 
         * LocalDateTime startOfYear = LocalDateTime.of(year, Month.JANUARY, 1, 0, 0);
         * LocalDateTime endOfYear = LocalDateTime.of(year, Month.DECEMBER, 31, 23, 59);
         * 
         * // Mapping des méthodes de recherche selon le profil utilisateur
         * Map<ProfilEnum, Function<Long, List<Reservation>>> searchMethods = Map.of(
         * ProfilEnum.PROMOTEUR, id -> reservationRepository
         * .findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_Promoter_Id(
         * startOfYear,
         * endOfYear, parentPropertyId, id),
         * ProfilEnum.AGENCY, id -> reservationRepository
         * .findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_ParentProperty_Agency_Id(
         * startOfYear,
         * endOfYear, parentPropertyId, id),
         * ProfilEnum.BANK, id -> reservationRepository
         * .findByValidatedAtBetweenAndProperty_ParentProperty_IdAndProperty_ParentProperty_Bank_Id(
         * startOfYear,
         * endOfYear, parentPropertyId, id));
         * 
         * // Récupération des réservations selon le profil, ou une liste vide si le
         * profil
         * // n'est pas trouvé
         * List<Reservation> reservations = searchMethods
         * .getOrDefault(user.getProfil(), id -> Collections.emptyList())
         * .apply(promoterId);
         * 
         * // Grouper les réservations par mois
         * Map<Integer, Long> reservationsByMonth = reservations.stream()
         * .collect(Collectors.groupingBy(res -> res.getValidatedAt().getMonthValue(),
         * Collectors.counting()));
         * 
         * // Construire la liste des statistiques de réservations par mois
         * return IntStream.rangeClosed(1, 12)
         * .mapToObj(month -> new ReservationStats(month,
         * reservationsByMonth.getOrDefault(month, 0L).intValue()))
         * .collect(Collectors.toList());
         * }
         */

}
