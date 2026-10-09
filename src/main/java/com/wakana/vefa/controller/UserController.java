package com.wakana.vefa.controller;

import java.io.IOException;
import java.util.Collections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.wakana.vefa.dto.ChangePasswordRequest;
import com.wakana.vefa.dto.DevisRequest;
import com.wakana.vefa.dto.SignUpRequest;
import com.wakana.vefa.dto.TechnicalSheetRequest;
import com.wakana.vefa.enums.ProfilEnum;

import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.SubscriptionPlanRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.AuthenticationService;
import com.wakana.vefa.services.SubscriptionService;
import com.wakana.vefa.services.UserSeervice;
import com.wakana.vefa.util.FileTraferUtil;

import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {
  @Autowired
  private UserRepository userRepository;
  private final UserSeervice userSeervice;
  private final AuthenticationService authenticationService;
  private final SubscriptionService subscriptionService;

  private final SubscriptionPlanRepository subscriptionPlanRepository;

  @PostMapping("/save")
  public ResponseEntity<?> save(@RequestBody SignUpRequest signUpRequest) {
    try {
      User savedUser = authenticationService.create(signUpRequest);
      return ResponseEntity.ok(savedUser);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }

  @GetMapping("/me")
  public ResponseEntity<User> getMe() {
    UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

    String username = userDetails.getUsername();

    User user = userRepository.findByTelephone(username).orElse(null); // $

    /*
     * SubscriptionPlan subscriptionPlan =
     * subscriptionPlanRepository.findByName(SubscriptionPlanEnum.NOTAIRE_PLAN)
     * .orElseThrow(() -> new RuntimeException("SubscriptionPlan not found "));
     * 
     * subscriptionService.createSubscription( user ,subscriptionPlan.getId());
     */

    return ResponseEntity.ok(user);
  }

  @GetMapping("/all")
  public ResponseEntity<?> getAll() {
    return ResponseEntity.ok(userSeervice.getAllUsers());
  }

  @PostMapping("/password/change")
  public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {

    try {
      /*
       * UserDetails userDetails = (UserDetails)
       * SecurityContextHolder.getContext().getAuthentication().getPrincipal();
       * String username = userDetails.getUsername();
       * changePasswordRequest.setEmail(username);
       */
      return ResponseEntity.ok(authenticationService.changePassword(changePasswordRequest));
    } catch (Exception e) {
      return ResponseEntity
          .status(HttpStatus.OK)
          .body(Collections.singletonMap("error", "Email ou mot de passe incorrecte"));
    }

  }

  @DeleteMapping("/{id:.+}")
  public ResponseEntity<?> deleteUser(@PathVariable Long id) {
    User user = userRepository.findById(id).orElse(null);
    userRepository.delete(user);
    return ResponseEntity.ok("User deleted");

  }

  @PutMapping("/{id:.+}")
  public ResponseEntity<?> putUser(@PathVariable Long id) {
    User user = userRepository.findById(id).orElse(null);
    user.setActivated(!user.isActivated());
    return ResponseEntity.ok(userRepository.save(user));

  }

  @PutMapping("/notifiable/{id:.+}")
  public ResponseEntity<?> setNotifiable(@PathVariable Long id) {
    User user = userRepository.findById(id).orElse(null);
    user.setNotifiable(!user.isNotifiable());
    return ResponseEntity.ok(userRepository.save(user));

  }

  // @PreAuthorize("hasAuthority('ADMIN')")
  @PutMapping("/update/{id:.+}")
  public ResponseEntity<?> update(@PathVariable Long id, @RequestBody SignUpRequest signUpRequest) {

    return ResponseEntity.ok(userSeervice.update(id, signUpRequest));

  }

  @GetMapping("/{id:.+}")
  public ResponseEntity<?> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(userSeervice.getUser(id));
  }

  @GetMapping("/notaires")
  public List<User> getNotaires() {
    return userSeervice.getNotaires();
  }

  @PutMapping("/technical-sheet")
  public ResponseEntity<?> addSheet(@ModelAttribute TechnicalSheetRequest request) {
    try {

      String sheetName = FileTraferUtil.handleFileUpload(request.getFile());
      request.setSheetName(sheetName);
      User user = userSeervice.addTechnicalSheet(request);
      return ResponseEntity.ok(user);
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Échec de la mise à jour : " + e.getMessage());
    }
  }

  @GetMapping("/user-count")
  public ResponseEntity<Long> getUserCountByProfil(@RequestParam("profil") ProfilEnum profil) {
    // long count = userSeervice.getUserCountByProfil(profil);
    return ResponseEntity.ok(userSeervice.getUserCountByProfil(profil));
  }

  @GetMapping("/search/bank")
  public ResponseEntity<List<User>> searchUsersByCompanyAndProfile(@RequestParam String keyword) {
    List<User> users = userSeervice.searchUsersByCompanyAndProfile(keyword);
    return ResponseEntity.ok(users);
  }

  @GetMapping("/search/agency")
  public ResponseEntity<List<User>> searchUsersByCompanyAndProfileAgency(@RequestParam String keyword) {
    List<User> users = userSeervice.searchUsersByCompanyAndProfileAgency(keyword);
    return ResponseEntity.ok(users);
  }

  @PostMapping("/photo/{id:.+}")
  public ResponseEntity<?> updatePhoto(@ModelAttribute SignUpRequest request, @PathVariable Long id) {
    try {
      String file = FileTraferUtil.handleFileUpload(request.getFile());
      return ResponseEntity.ok(userSeervice.updatePhoto(id, file));
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to update files: " + e.getMessage());
    }
  }

  @PostMapping("/idcard/{id:.+}")
  public ResponseEntity<?> updateidCard(@ModelAttribute SignUpRequest request, @PathVariable Long id) {
    try {
      String file = FileTraferUtil.handleFileUpload(request.getFile());
      return ResponseEntity.ok(userSeervice.updateIdcard(id, file));
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("Failed to update files: " + e.getMessage());
    }
  }

  // copropriete
  @GetMapping("/{id}/owners")
  public ResponseEntity<Set<User>> getOwners(@PathVariable Long id) {
    Set<User> owners = userSeervice.findOwnersByPropertyId(id);
    return owners.isEmpty() ? ResponseEntity.notFound().build() : ResponseEntity.ok(owners);
  }

  /////// ratings///////////:
  @PostMapping("/{userId}/rate")
  public ResponseEntity<String> rateUser(@PathVariable Long userId,
      @RequestParam Integer score,
      @RequestParam Long jobRequestId,

      @RequestParam(required = false) String comment) {
    userSeervice.addRating(userId, jobRequestId, score, comment);
    return ResponseEntity.ok("La note a été ajoutée avec succès.");
  }

  @GetMapping("/{userId}/rating")
  public ResponseEntity<Double> getUserRating(@PathVariable Long userId) {
    Double rating = userSeervice.getUserRating(userId);
    return ResponseEntity.ok(rating);
  }

  /// admin
  @GetMapping("/by-profil")
  public Page<User> getUsersByProfil(
      @RequestParam ProfilEnum profil,
      @RequestParam(required = false) String keyword,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);
    return userSeervice.getUsersByProfil(profil, keyword, pageable);
  }

  // espace admin ____ admin _____ admin
  // @PreAuthorize("hasAuthority('ADMIN')")
  @GetMapping("/search")
  public ResponseEntity<Page<User>> search(
      @RequestParam(required = false) String keyword,
      @RequestParam(required = false) String profil,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<User> userPage;
    ProfilEnum profilEnum = null;

    // Conversion du profil en enum si présent
    if (profil != null && !profil.isBlank()) {
      try {
        profilEnum = ProfilEnum.valueOf(profil.toUpperCase());
      } catch (IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(Page.empty());
      }
    }

    // Logique de filtrage
    if ((keyword == null || keyword.trim().isEmpty()) && profilEnum == null) {
      userPage = userSeervice.findAll(pageable);
    } else if (profilEnum != null && (keyword == null || keyword.trim().isEmpty())) {
      userPage = userSeervice.findByProfil(profilEnum, pageable);
    } else if (profilEnum != null) {
      userPage = userSeervice.findByNomOrPrenomAndProfil(keyword, profilEnum, pageable);
    } else {
      userPage = userSeervice.findByNomContainingOrPrenomContaining(keyword, pageable);
    }

    HttpHeaders responseHeaders = new HttpHeaders();
    responseHeaders.add("X-Total-Elements", String.valueOf(userPage.getTotalElements()));
    responseHeaders.add("X-Total-Pages", String.valueOf(userPage.getTotalPages()));

    return ResponseEntity.ok()
        .headers(responseHeaders)
        .body(userPage);
  }

  @GetMapping("/users/statistics")
  public ResponseEntity<Map<String, Object>> getUserStatistics(
      @RequestParam(required = false) Integer year) {
    return ResponseEntity.ok(userSeervice.getUserStatistics(year));
  }

  @GetMapping("/users/evolution")
  public ResponseEntity<List<Map<String, Object>>> getUserEvolution(
      @RequestParam(required = false) Integer year) {
    return ResponseEntity.ok(userSeervice.getUserEvolutionByMonth(year));
  }

  @PutMapping("/{id}/activation")
  @ResponseStatus(HttpStatus.OK) // maintenant OK 200
  public User changeUserActivationStatus(
      @PathVariable Long id,
      @RequestParam boolean activated) {

    return userSeervice.changeUserActivationStatus(id, activated);
  }

}
