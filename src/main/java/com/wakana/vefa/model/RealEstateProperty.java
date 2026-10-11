package com.wakana.vefa.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.wakana.vefa.enums.RealEstatePropertyStatusEnum;
import com.wakana.vefa.exception.BusinessException;
import org.springframework.http.HttpStatus;
import java.util.Set;

@Data
@Entity
@Table(name = "real_estate_properties")
public class RealEstateProperty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // Name of the property
    private String number; // Property number
    private String address; // Property address
    private double price; // Property price
    private int numberOfRooms; // Number of rooms
    private double area; // Area in square meters
    @Column
    private Double latitude;

    @Column
    private Double longitude;
    private boolean available; // Availability status
    private double reservationFee; // Reservation fee
    private double discount; // remise
    private double feesFile;
    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String description; // Long text description
    private String plan; // Plan (e.g., building plans)
    private String legalStatus; // Legal status (Real rights status)
    private int numberOfLots; // Number of lots in the property
    private int level; // The level (floor) of the apartment
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promoter_id", nullable = false)
    private User promoter; // Promoter of the property
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_recipient_id", nullable = true)
    private User recipient; // Recipient of the property
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notary_id", nullable = true)
    private User notary; // Notary of the property
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = true)
    private User agency; // Notary of the property
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_id", nullable = true)
    private User bank; // Notary of the property
    // Self-referencing relationship for sub-properties (e.g., apartments/lots in a
    // building)
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_property_id")
    private RealEstateProperty parentProperty; // Parent property (e.g., the building)

    @JsonIgnore
    @OneToMany(mappedBy = "parentProperty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RealEstateProperty> subProperties; // Sub-properties (e.g., apartments/lots in the building)
    private Long timestamp;
    @ElementCollection
    @Column(name = "pictures")
    private List<String> pictures;
    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_type_id", nullable = false)
    private PropertyType propertyType;
    @JsonIgnore
    @OneToMany(mappedBy = "realEstateProperty", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ConstructionPhaseIndicator> constructionPhaseIndicators;

    // Indique la présence d'un hall d'entrée commun
    private boolean hasHall;

    // Indique si l'immeuble dispose d'un parking commun
    private boolean hasParking;

    // Précise la présence d'un ou plusieurs ascenseurs
    private boolean hasElevator;

    // Indique si l'immeuble possède une piscine commune
    private boolean hasSwimmingPool;

    // Indique la présence d'une salle de sport commune
    private boolean hasGym;
    // Précise s'il y a une aire de jeux commune
    private boolean hasPlayground;

    // Indique la présence d'un service de sécurité (gardiennage)
    private boolean hasSecurityService;

    // Indique s'il y a un jardin ou une cour commune
    private boolean hasGarden;

    // Précise la présence d'une terrasse commune
    private boolean hasSharedTerrace;

    // Indique la présence d'un local pour vélos
    private boolean hasBicycleStorage;

    // Indique s'il y a une buanderie commune
    private boolean hasLaundryRoom;

    // Précise s'il existe des caves ou locaux de stockage
    private boolean hasStorageRooms;

    // Indique la présence d'une zone pour les poubelles
    private boolean hasWasteDisposalArea;

    @Enumerated(EnumType.STRING)
    private RealEstatePropertyStatusEnum status;

    @Version
    private Long version;

    private boolean lotFeesPaid;

    @JsonManagedReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_fee_id", nullable = true)
    private Fee lotFee;
    private boolean coOwner;

    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "property_user_mapping", joinColumns = @JoinColumn(name = "property_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> owners;
    private double budget;
    private LocalDateTime allocateDate;
    private boolean rental;
    private int commentcount;
    private LocalDateTime rentalDate;

    private LocalDateTime soldAt;

    public void initializeAsAvailable() {
        status = RealEstatePropertyStatusEnum.AVAILABLE;
        available = true;
    }

    public void restoreAvailability() {
        if (status == RealEstatePropertyStatusEnum.SOLD) {
            throw new BusinessException("SOLD_PROPERTY_CANNOT_BE_REOPENED",
                    "Un lot vendu ne peut pas redevenir disponible.", HttpStatus.CONFLICT);
        }
        status = RealEstatePropertyStatusEnum.AVAILABLE;
        available = true;
    }

    public void changeStatus(RealEstatePropertyStatusEnum newStatus) {
        if (status == newStatus) {
            available = newStatus == RealEstatePropertyStatusEnum.AVAILABLE;
            return;
        }
        boolean validTransition = (status == RealEstatePropertyStatusEnum.AVAILABLE
                && newStatus == RealEstatePropertyStatusEnum.RESERVED)
                || (status == RealEstatePropertyStatusEnum.RESERVED
                        && newStatus == RealEstatePropertyStatusEnum.SOLD);
        if (!validTransition) {
            throw new BusinessException("INVALID_PROPERTY_STATUS_TRANSITION",
                    "Transition de statut du lot non autorisée.", HttpStatus.CONFLICT);
        }
        status = newStatus;
        available = newStatus == RealEstatePropertyStatusEnum.AVAILABLE;
    }

    @JsonIgnore
    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reservation> reservations;
    @JsonIgnore
    @OneToMany(mappedBy = "realEstateProperty", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Rapport> rapports;
    @JsonIgnore
    @OneToMany(mappedBy = "property", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<JobRequest> jobRequests;
    @JsonIgnore
    @OneToMany(mappedBy = "realEstateProperty", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Meet> meets;
    @JsonIgnore
    @OneToMany(mappedBy = "property", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Notifications> notifications;
    @JsonIgnore
    @OneToMany(mappedBy = "realEstateProperty", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ProgressAlbum> progressAlbums;

    private boolean isMezzanine;

    @ManyToOne
    @JoinColumn(name = "subscription2_id")
    private Subscriptions subscription;

}
