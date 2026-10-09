package com.wakana.vefa.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wakana.vefa.dto.MeetRequest;
import com.wakana.vefa.dto.response.MeetStatsResponse;
import com.wakana.vefa.model.Meet;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.MeetRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.EmailService;
import com.wakana.vefa.services.MeetService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import java.time.YearMonth;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collections;

@Service
@Transactional
@RequiredArgsConstructor
public class MeetServiceImpl implements MeetService {

    @Autowired
    private MeetRepository meetRepository;
    @Autowired
    private EmailService emailService;

    @Autowired
    private UserRepository userRepository;
    private final RealEstatePropertyRepository realEstatePropertyRepository;

    // Mise à jour du formatter pour inclure les secondes
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    public Meet createMeet(MeetRequest meetRequest) {
        User creator = userRepository.findById(meetRequest.getUserId()).orElse(null);
        // Rechercher la propriété existante
        RealEstateProperty existingProperty = realEstatePropertyRepository.findById(meetRequest.getPropertyId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Property not found with id: " + meetRequest.getPropertyId()));

        // Convertir la date en LocalDateTime en utilisant le formatteur
        // DateTimeFormatter formatter =
        // DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); // Exemple de format
        LocalDateTime dateTime = LocalDateTime.parse(meetRequest.getDateTime(), formatter);
        LocalDateTime endTime = dateTime.plusMinutes(meetRequest.getDuration());

        // Vérifier le chevauchement des réunions
        /*
         * if (isOverlapping(dateTime, meetRequest.getDuration())) {
         * throw new
         * IllegalArgumentException("The meeting time overlaps with an existing meeting."
         * );
         * }
         */

        // Créer une nouvelle réunion
        Meet meet = new Meet();
        meet.setDateTime(dateTime);
        meet.setAddress(meetRequest.getAddress());
        meet.setTitle(meetRequest.getTitle());
        meet.setDuration(meetRequest.getDuration());
        meet.setCreator(creator);
        meet.setRealEstateProperty(existingProperty);
        meet.setIsOnline(meetRequest.getIsOnline());

        // Initialiser la liste des participants
        List<User> participants = new ArrayList<>(); // Initialisation de la liste
        participants.add(existingProperty.getPromoter());
        participants.add(existingProperty.getRecipient());
        participants.add(existingProperty.getNotary());

        if (existingProperty.getParentProperty().getAgency() != null) {
            participants.add(existingProperty.getParentProperty().getAgency());
        }

        if (existingProperty.getParentProperty().getBank() != null) {
            participants.add(existingProperty.getParentProperty().getBank());
        }

        // Assigner les participants à la réunion
        meet.setParticipants(participants);
        String meetLink = generateInstantMeetLink();
        String emailContent = "Bonjour,\n\n"
                + "Une réunion est programmée sur le sujet : *" + meetRequest.getTitle() + "*.\n"
               // + "Si vous souhaitez y participer à distance, vous pouvez rejoindre la réunion en cliquant sur le lien ci-dessous :\n\n"
               // + "🔗 " + meetLink + "\n\n"
                + "Merci de votre participation.\n\n"
                + "Cordialement,\n"
                + "";

        emailService.sendMeetingInvite(
                List.of(
                        existingProperty.getPromoter().getEmail(),
                        existingProperty.getRecipient().getEmail(),
                        existingProperty.getNotary().getEmail(),
                        "kheuchebambiste@gmail.com"), // TO
                List.of(), // CC
                List.of(), // BCC
                meetRequest.getTitle(),
                emailContent ,
                dateTime, // Date et heure de début
                endTime, // Date et heure de fin calculée dynamiquement
                meetRequest.getAddress());

        // Sauvegarder et retourner la réunion créée
        return meetRepository.save(meet);
    }

    @Override
    public Meet updateMeet(Long id, MeetRequest meetRequest) {
        // Recherche de la réunion existante avec gestion d'exception si non trouvée
        Meet meet = meetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meet not found with id: " + id));

        // Conversion de la date de réunion en LocalDateTime en utilisant le formatteur
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"); // Exemple de format
        LocalDateTime newDateTime = LocalDateTime.parse(meetRequest.getDateTime(), formatter);

        // Vérification de chevauchement des réunions
        if (isOverlapping(newDateTime, meetRequest.getDuration())) {
            throw new IllegalArgumentException("The meeting time overlaps with an existing meeting.");
        }

        // Mise à jour des champs de la réunion
        meet.setDateTime(newDateTime);
        meet.setDuration(meetRequest.getDuration());

        // Récupération et mise à jour des participants
        List<User> participants = userRepository.findAllById(meetRequest.getParticipants());
        meet.setParticipants(participants);

        // Sauvegarde et retour de la réunion mise à jour
        return meetRepository.save(meet);
    }

    @Override
    public void deleteMeet(Long id) {
        meetRepository.deleteById(id);
    }

    @Override
    public List<Meet> getAllMeets() {
        return meetRepository.findAll();
    }

    @Override
    public Meet getMeetById(Long id) {
        return meetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meet not found"));
    }

    @Override
    public void addParticipants(Long meetId, List<Long> participantIds) {
        Meet meet = meetRepository.findById(meetId)
                .orElseThrow(() -> new IllegalArgumentException("Meet not found"));

        List<User> participants = userRepository.findAllById(participantIds);
        meet.getParticipants().addAll(participants);

        meetRepository.save(meet);
    }

    @Override
    public Page<Meet> getMeetsByCreator(Long creatorId, int month, int year, Pageable pageable) {
        // Get the start and end of the month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // Query the repository with creatorId and date range
        return meetRepository.findByCreator_IdAndDateTimeBetween(creatorId, startDate, endDate, pageable);
    }

    @Override
    public Page<Meet> getMeetsByParticipant(Long userId, int month, int year, Pageable pageable) {
        // Get the start and end of the month
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        // Query the repository with userId and date range
        return meetRepository.findByParticipantIdAndDateTimeBetween(userId, startDate, endDate, pageable);
    }

    @Override
    public boolean isOverlapping(LocalDateTime dateTime, int duration) {
        List<Meet> allMeets = meetRepository.findAll();
        for (Meet meet : allMeets) {
            LocalDateTime startTime = meet.getDateTime();
            LocalDateTime endTime = startTime.plusMinutes(meet.getDuration());
            LocalDateTime requestedEndTime = dateTime.plusMinutes(duration);

            // Check if there is an overlap
            if (dateTime.isBefore(endTime) && requestedEndTime.isAfter(startTime)) {
                return true;
            }
        }
        return false;
    }

    /// COP
    @Override
    public Meet createMeetCOP(MeetRequest meetRequest) {
        User creator = userRepository.findById(meetRequest.getUserId()).orElse(null);
        // Rechercher la propriété existante
        RealEstateProperty existingProperty = realEstatePropertyRepository.findById(meetRequest.getPropertyId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Property not found with id: " + meetRequest.getPropertyId()));

        LocalDateTime dateTime = LocalDateTime.parse(meetRequest.getDateTime(), formatter);

        // Vérifier le chevauchement des réunions
        /*
         * if (isOverlapping(dateTime, meetRequest.getDuration())) {
         * throw new
         * IllegalArgumentException("The meeting time overlaps with an existing meeting."
         * );
         * }
         */

        // Créer une nouvelle réunion
        Meet meet = new Meet();
        meet.setDateTime(dateTime);
        meet.setAddress(meetRequest.getAddress());
        meet.setTitle(meetRequest.getTitle());
        meet.setDuration(meetRequest.getDuration());
        meet.setDescription(meetRequest.getDescription());
        meet.setRealEstateProperty(existingProperty);
        meet.setCreator(creator);
        Set<User> setUsers = userRepository.findOwnersByPropertyId(existingProperty.getId());
        List<User> participants = Collections.unmodifiableList(new ArrayList<>(setUsers));

        meet.setParticipants(participants);

        // Sauvegarder et retourner la réunion créée
        return meetRepository.save(meet);
    }

    public Page<Meet> getUpcomingMeets(Long realEstatePropertyId, Pageable pageable) {
        return meetRepository.findUpcomingByRealEstatePropertyId(realEstatePropertyId, LocalDateTime.now(), pageable);
    }

    public Page<Meet> getPastMeets(Long realEstatePropertyId, Pageable pageable) {
        return meetRepository.findPastByRealEstatePropertyId(realEstatePropertyId, LocalDateTime.now(), pageable);
    }

    @Override
    public Meet addFiles(Long id, List<String> files) {
        Meet meet = meetRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Meet not found"));
        meet.setFiles(files);
        return meetRepository.save(meet);
    }

    public static String generateInstantMeetLink() {
        return "https://meet.google.com/new";
    }





 @Override
    public MeetStatsResponse getMeetStats(Long propertyId) {

        LocalDateTime now = LocalDateTime.now();

        long upcoming = meetRepository
                .countByRealEstatePropertyIdAndDateTimeAfter(propertyId, now);

        long past = meetRepository
                .countByRealEstatePropertyIdAndDateTimeBefore(propertyId, now);

        // 🔥 LOGIQUE MÉTIER IMPORTANTE
        // Nombre de participants possibles = users liés à la propriété
        RealEstateProperty property = realEstatePropertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Propriété introuvable"));

        long totalParticipants = property.getOwners() != null
                ? property.getOwners().size()
                : 0;

        return new MeetStatsResponse(upcoming, past, totalParticipants);
    }
}
