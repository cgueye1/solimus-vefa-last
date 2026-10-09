package com.wakana.vefa.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.wakana.vefa.dto.SignUpRequest;
import com.wakana.vefa.dto.TechnicalSheetRequest;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.JobRequest;
import com.wakana.vefa.model.Rating;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.JobRequestRepository;
import com.wakana.vefa.repository.RatingRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.UserSeervice;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserSeervice {
  private final UserRepository userRepository;
  private final JobRequestRepository jobRequestRepository;
  private final RatingRepository ratingRepository;

  public UserDetailsService userDetailsService() {
    return new UserDetailsService() {

      @Override
      public UserDetails loadUserByUsername(String username) {
        return userRepository.findByTelephone(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not foound "));
      }
    };
  }

  public List<User> getAllUsers() {
    return userRepository.findAll();

  }

  @Override
  public Page<User> findByNomContainingOrPrenomContaining(String keyword, Pageable pageable) {
    return userRepository.findByNomContainingOrPrenomContainingAndProfilNot(keyword, keyword, ProfilEnum.PROMOTEUR,
        pageable);
  }

  @Override
  public User getUser(Long id) {

    return userRepository.findById(id).orElse(null);
  }

  @Override
  public User update(Long id, SignUpRequest signUpRequest) {
    User user = userRepository.findById(id).orElse(null);

    ProfilEnum profilEnum;
    try {
      profilEnum = ProfilEnum.valueOf(signUpRequest.getProfil());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Invalid role: " + signUpRequest.getProfil());
    }

    user.setNom(signUpRequest.getNom());
    user.setPrenom(signUpRequest.getPrenom());
    user.setProfil(profilEnum);
    user.setEmail(signUpRequest.getEmail());
    user.setTelephone(signUpRequest.getTelephone());

    return userRepository.save(user);
  }

  public Page<User> findAll(Pageable pageable) {
    // Liste des profils que nous voulons récupérer
    List<ProfilEnum> profils = List.of(
        ProfilEnum.PROMOTEUR,
        ProfilEnum.NOTAIRE,
        ProfilEnum.PROPRIETAIRE,
        ProfilEnum.BANK,
        ProfilEnum.AGENCY,
        ProfilEnum.SYNDIC,
        ProfilEnum.ADMIN);

    // Utilisation de la méthode findByProfilIn pour récupérer les utilisateurs avec
    // ces profils
    return userRepository.findByProfilIn(pageable, profils);
  }

  public List<User> getNotaires() {
    return userRepository.findByProfil(ProfilEnum.NOTAIRE);
  }

  @Override
  public User addTechnicalSheet(TechnicalSheetRequest request) {
    User user = userRepository.findById(request.getId()).orElse(null);
    user.setTechnicalSheet(request.getSheetName());
    return userRepository.save(user);

  }

  public long getUserCountByProfil(ProfilEnum profil) {
    return userRepository.countByProfil(profil);
  }

  @Override
  public List<User> searchUsersByCompanyAndProfile(String keyword) {
    // Search users with profile BANK and company name containing the keyword
    return userRepository.findByProfilAndCompany_NameContaining(ProfilEnum.BANK, keyword);
  }

  @Override
  public List<User> searchUsersByCompanyAndProfileAgency(String keyword) {
    // Search users with profile BANK and company name containing the keyword
    return userRepository.findByProfilAndCompany_NameContaining(ProfilEnum.AGENCY, keyword);
  }

  @Override
  public Set<User> findOwnersByPropertyId(Long propertyId) {
    return userRepository.findOwnersByPropertyId(propertyId);
  }

  ////////
  public void addRating(Long userId, Long jobRequestId, Integer score, String comment) {
    if (score < 1 || score > 5) {
      throw new IllegalArgumentException("La note doit être entre 1 et 5.");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

    Optional<JobRequest> jobRequestOpt = jobRequestRepository.findById(jobRequestId);

    if (jobRequestOpt.isEmpty()) {
      throw new IllegalArgumentException("JobRequest not found");
    }

    JobRequest jobRequest = jobRequestOpt.get();

    Rating rating = new Rating();
    rating.setUser(user);
    rating.setScore(score);
    rating.setComment(comment);
    rating.setJobRequest(jobRequest);

    ratingRepository.save(rating);

    // Calculer la nouvelle moyenne
    double average = user.getRatings().stream()
        .mapToInt(Rating::getScore)
        .average()
        .orElse(0.0);
    user.setNote(average);

    userRepository.save(user);
  }

  public Double getUserRating(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
    return user.getNote();
  }

  @Override
  public User updatePhoto(Long id, String photo) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
    user.setPhoto(photo);

    return userRepository.save(user);

  }

  @Override
  public User updateIdcard(Long id, String idCard) {
    User user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));
    user.setIdCard(idCard);

    return userRepository.save(user);
  }

  // admin

  @Override
  public Page<User> getUsersByProfil(ProfilEnum profil, String keyword, Pageable pageable) {
    return userRepository.findByProfilAndKeyword(profil, keyword, pageable);
  }

  @Override
  public Page<User> findByProfil(ProfilEnum profil, Pageable pageable) {
    return userRepository.findByProfil(profil, pageable);
  }

  @Override
  public Page<User> findByNomOrPrenomAndProfil(String keyword, ProfilEnum profil, Pageable pageable) {
    return userRepository.findByProfilAndNomContainingIgnoreCaseOrProfilAndPrenomContainingIgnoreCase(
        profil, keyword, profil, keyword, pageable);
  }

  @Override
  public Map<String, Object> getUserStatistics(Integer year) {

    Map<String, Object> response = new HashMap<>();
    Map<String, Long> repartitionByProfil = new HashMap<>();

    long totalUsers;
    long totalReservataires;
    long totalPromoteurs;
    long totalSyndics;
    long totalProprietaires;
    long totalAgencies;
    long totalTenants;
    long totalNotaires;

    List<ProfilEnum> profilsPrisEnCompte = List.of(
        ProfilEnum.PROMOTEUR,
        ProfilEnum.SYNDIC,
        ProfilEnum.RESERVATAIRE,
        ProfilEnum.PROPRIETAIRE,
        ProfilEnum.AGENCY,
        ProfilEnum.TENANT,
        ProfilEnum.NOTAIRE);

    if (year != null) {

      LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
      LocalDateTime end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);

      totalUsers = userRepository.countByCreatedAtBetween(start, end);

      totalReservataires = userRepository.countByProfilAndCreatedAtBetween(
          ProfilEnum.RESERVATAIRE, start, end);

      totalPromoteurs = userRepository.countByProfilAndCreatedAtBetween(
          ProfilEnum.PROMOTEUR, start, end);

      totalSyndics = userRepository.countByProfilAndCreatedAtBetween(
          ProfilEnum.SYNDIC, start, end);

      totalProprietaires = userRepository.countByProfilAndCreatedAtBetween(
          ProfilEnum.PROPRIETAIRE, start, end);

      totalAgencies = userRepository.countByProfilAndCreatedAtBetween(
          ProfilEnum.AGENCY, start, end);

      totalTenants = userRepository.countByProfilAndCreatedAtBetween(
          ProfilEnum.TENANT, start, end);

      totalNotaires = userRepository.countByProfilAndCreatedAtBetween(
          ProfilEnum.NOTAIRE, start, end);

      userRepository
          .countByProfilInAndCreatedAtBetween(profilsPrisEnCompte, start, end)
          .forEach(row -> repartitionByProfil.put(
              ((ProfilEnum) row[0]).name(),
              (Long) row[1]));

      response.put("year", year);

    } else {

      totalUsers = userRepository.count();

      totalReservataires = userRepository.countByProfil(ProfilEnum.RESERVATAIRE);
      totalPromoteurs = userRepository.countByProfil(ProfilEnum.PROMOTEUR);
      totalSyndics = userRepository.countByProfil(ProfilEnum.SYNDIC);
      totalProprietaires = userRepository.countByProfil(ProfilEnum.PROPRIETAIRE);
      totalAgencies = userRepository.countByProfil(ProfilEnum.AGENCY);
      totalTenants = userRepository.countByProfil(ProfilEnum.TENANT);
      totalNotaires = userRepository.countByProfil(ProfilEnum.NOTAIRE);

      userRepository
          .countByProfilIn(profilsPrisEnCompte)
          .forEach(row -> repartitionByProfil.put(
              ((ProfilEnum) row[0]).name(),
              (Long) row[1]));

      response.put("year", "ALL");
    }

    response.put("totalUsers", totalUsers);
    response.put("totalReservataires", totalReservataires);
    response.put("totalPromoteurs", totalPromoteurs);
    response.put("totalSyndics", totalSyndics);
    response.put("totalProprietaires", totalProprietaires);
    response.put("totalAgencies", totalAgencies);
    response.put("totalTenants", totalTenants);
    response.put("totalNotaires", totalNotaires);
    response.put("repartitionByProfil", repartitionByProfil);

    return response;
  }

  @Override
  public List<Map<String, Object>> getUserEvolutionByMonth(Integer year) {

    // 👉 SI l'année n'est pas fournie → année en cours
    int targetYear = (year != null)
        ? year
        : LocalDate.now().getYear();

    List<Object[]> results = userRepository.countUsersGroupedByMonth(targetYear);

    Map<Integer, Long> dataByMonth = new HashMap<>();
    for (Object[] row : results) {
      dataByMonth.put(
          ((Number) row[0]).intValue(), // mois
          ((Number) row[1]).longValue() // total users
      );
    }

    String[] months = {
        "Jan", "Fév", "Mar", "Avr", "Mai", "Juin",
        "Juil", "Aoû", "Sep", "Oct", "Nov", "Déc"
    };

    List<Map<String, Object>> response = new ArrayList<>();

    for (int i = 1; i <= 12; i++) {
      Map<String, Object> item = new HashMap<>();
      item.put("name", months[i - 1]);
      item.put("value", dataByMonth.getOrDefault(i, 0L));
      response.add(item);
    }

    return response;
  }

  @Override
  public User changeUserActivationStatus(Long userId, boolean activated) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new EntityNotFoundException("Utilisateur introuvable"));

    user.setActivated(activated);
    return userRepository.save(user); // retourne l'utilisateur mis à jour
  }

 
}
