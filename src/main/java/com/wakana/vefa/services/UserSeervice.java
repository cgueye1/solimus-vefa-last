package com.wakana.vefa.services;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.RequestBody;

import com.wakana.vefa.dto.SignUpRequest;
import com.wakana.vefa.dto.TechnicalSheetRequest;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.model.User;

public interface UserSeervice {

  UserDetailsService userDetailsService();

  List<User> getAllUsers();

  Page<User> findByNomContainingOrPrenomContaining(String keyword, Pageable pageable);

  Page<User> findAll(Pageable pageable);

  User getUser(Long id);

  User updatePhoto(Long id, String photo);

  User updateIdcard(Long id, String idCard);

  User update(Long id, @RequestBody SignUpRequest signUpRequest);

  User addTechnicalSheet(@RequestBody TechnicalSheetRequest request);

  List<User> getNotaires();

  long getUserCountByProfil(ProfilEnum profil);

  List<User> searchUsersByCompanyAndProfile(String keyword);

  List<User> searchUsersByCompanyAndProfileAgency(String keyword);

  // Méthode pour trouver les propriétaires d'une propriété par son ID
  Set<User> findOwnersByPropertyId(Long propertyId);

  /// rating///
  public void addRating(Long userId, Long propertyId, Integer score, String comment);

  public Double getUserRating(Long userId);

  /// admin
  public Page<User> getUsersByProfil(ProfilEnum profil, String keyword, Pageable pageable);

  Page<User> findByProfil(ProfilEnum profil, Pageable pageable);

  Page<User> findByNomOrPrenomAndProfil(String keyword, ProfilEnum profil, Pageable pageable);

  Map<String, Object> getUserStatistics(Integer year);

  List<Map<String, Object>> getUserEvolutionByMonth(Integer year);

  User changeUserActivationStatus(Long userId, boolean activated);
  
  


}
