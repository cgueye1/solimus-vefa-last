package com.wakana.vefa.dto;

import org.springframework.web.multipart.MultipartFile;

import lombok.Data;
import java.util.List;

@Data
public class RealEstatePropertyRequest {

    private String name;
    private String number;
    private String address;
    private double price;
    private double feesFile;
    private int numberOfRooms;
    private double area;
    private String latitude;
    private String longitude;
    private boolean available;
    private double reservationFee;
    private String description;
    private int numberOfLots;
    private long promoterId;
    private long recipientId;
    private long notaryId;
    private long bankId;
    private long agencyId;
    private long parentPropertyId;
    private long propertyTypeId;
    private double discount;
    private MultipartFile plan;
    private int level;
    private MultipartFile legalStatus;
    private List<MultipartFile> pictures;

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

    private String compagnyName;

    private boolean coOwner;
    private boolean rental;

    private boolean mezzanine;

    private String profil;

}
