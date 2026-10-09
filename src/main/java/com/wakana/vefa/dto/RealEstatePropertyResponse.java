package com.wakana.vefa.dto;

import com.wakana.vefa.enums.RealEstatePropertyStatusEnum;
import com.wakana.vefa.model.PropertyType;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.User;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
public class RealEstatePropertyResponse {

    private Long id;
    private String name;
    private String number;
    private String address;
    private Double price;
    private Integer numberOfRooms;
    private Double area;
    private Double latitude;
    private Double longitude;
    private  boolean isAvailable;
    private Double reservationFee;
    private String description;
    private Integer numberOfLots;
    private double discount;
    private double budget;
    private double feesFile;
    private int level; // The level (floor) of the apartment
    // Équipements communs
    private Boolean hasHall;
    private Boolean hasParking;
    private Boolean hasElevator;
    private Boolean hasSwimmingPool;
    private Boolean hasGym;
    private Boolean hasPlayground;
    private Boolean hasSecurityService;
    private Boolean hasGarden;
    private Boolean hasSharedTerrace;
    private Boolean hasBicycleStorage;
    private Boolean hasLaundryRoom;
    private Boolean hasStorageRooms;
    private Boolean hasWasteDisposalArea;
    private PropertyType propertyType;
    private User promoter;
    private User recipient;
    private User notary;
    private User agency;
    private User bank;
    private List<String> pictures;
    private String plan;
    private String legalStatus;
    private RealEstateProperty parentProperty;  
    private RealEstatePropertyStatusEnum status;
    private boolean coOwner;
    private Set<User> owners;
    private boolean rental;
    
    private LocalDateTime allocateDate;
    
    private Boolean  mezzanine ;
}
