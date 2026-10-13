package com.wakana.vefa.model;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.wakana.vefa.dto.SignUpRequest;
import com.wakana.vefa.enums.ProfilEnum;

import jakarta.persistence.*;
import lombok.Data;
import java.util.Set;

@Data
@Entity
@Table(name = "user_vefa1", uniqueConstraints = {
      @UniqueConstraint(columnNames = "telephone", name = "unique_telephone"),
      @UniqueConstraint(columnNames = "email", name = "unique_email"),

})

public class User implements UserDetails {
   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;
   private String nom;
   private String prenom;
   private String email;
   private String password;
   private String adress;
   private String technicalSheet;
   private ProfilEnum profil;
   private boolean activated;
   private boolean notifiable;
   @Column(unique = true)
   private String telephone;
   @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
   @JsonManagedReference
   @JsonIgnore
   private List<Subscription> subscriptions;

   @OneToOne(mappedBy = "owner", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
   private Company company;
   private LocalDateTime createdAt;
   private double funds;

   @JsonIgnore
   @ManyToMany(mappedBy = "owners")
   private Set<RealEstateProperty> properties;

   private double note = 0.0;
   @JsonIgnore
   @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
   private List<Rating> ratings;

   // @JsonManagedReference
   @JsonIgnore
   @ManyToOne(fetch = FetchType.LAZY)
   @JoinColumn(name = "solimus_jobs_id", nullable = true)
   private SolimusJobs job;
   private String photo;
   private String idCard;
   @ElementCollection
   @CollectionTable(name = "user_address_lots", joinColumns = @JoinColumn(name = "user_id"))
   @JsonIgnore
   private List<AddressLot> adressLots;

   @Override
   public Collection<? extends GrantedAuthority> getAuthorities() {
      return List.of(new SimpleGrantedAuthority(profil.name()));

   }

   @Override
   public String getUsername() {
      if (telephone != null && !telephone.trim().isEmpty()) {
         return telephone;
      }
      return email;
   }

   @Override
   public boolean isAccountNonExpired() {
      return true;
   }

   @Override
   public boolean isAccountNonLocked() {
      return true;
   }

   @Override
   public boolean isCredentialsNonExpired() {
      return true;
   }

   @Override
   public boolean isEnabled() {
      return true;
   }

   public static User fromSignUpRequest(SignUpRequest signUpRequest) {

      ProfilEnum profilEnum;
      try {
         profilEnum = ProfilEnum.valueOf(signUpRequest.getProfil());
      } catch (IllegalArgumentException e) {
         throw new IllegalArgumentException("Invalid role: " + signUpRequest.getProfil());
      }

      User user = new User();

      user.setNom(signUpRequest.getNom());
      user.setPrenom(signUpRequest.getPrenom());
      user.setPassword(signUpRequest.getPassword());
      user.setProfil(profilEnum);
      user.setEmail(signUpRequest.getEmail());
      user.setTelephone(signUpRequest.getTelephone());
      user.setAdress(signUpRequest.getAdress());
      user.setNotifiable(signUpRequest.isNotifiable());
      user.setActivated(true);
      user.setCreatedAt(LocalDateTime.now());
      user.setFunds(0);

      return user;

   }

   @Override
   public int hashCode() {
      return Objects.hash(id); // Example; adjust based on your properties
   }
}
