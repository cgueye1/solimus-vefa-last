package com.wakana.vefa.services.impl;

import org.springframework.stereotype.Service;
import com.wakana.vefa.dto.DashboardStats;
import com.wakana.vefa.dto.RealEstatePropertyRequest;
import com.wakana.vefa.dto.RealEstatePropertyResponse;
import com.wakana.vefa.dto.RealEstatePropertySearchRequest;
import com.wakana.vefa.dto.ReservationStats;
import com.wakana.vefa.enums.FeeEnum;
import com.wakana.vefa.enums.PaymentCallStatusEnum;
import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.enums.RealEstatePropertyStatusEnum;
import com.wakana.vefa.enums.ReservationStatus;
import com.wakana.vefa.model.CallForCharge;
import com.wakana.vefa.model.ConstructionPhaseIndicator;
import com.wakana.vefa.model.Fee;
import com.wakana.vefa.model.OwnerPropertyArea;
import com.wakana.vefa.model.PaymentCall;
import com.wakana.vefa.model.PropertyType;
import com.wakana.vefa.model.RealEstateProperty;
import com.wakana.vefa.model.Reservation;
import com.wakana.vefa.model.Subscriptions;
import com.wakana.vefa.model.User;
import com.wakana.vefa.repository.CallForChargeRepository;
import com.wakana.vefa.repository.ConstructionPhaseIndicatorRepository;
import com.wakana.vefa.repository.OwnerPropertyAreaRepository;
import com.wakana.vefa.repository.PaymentCallRepository;
import com.wakana.vefa.repository.PropertyTypeRepository;
import com.wakana.vefa.repository.RealEstatePropertyRepository;
import com.wakana.vefa.repository.ReservationRepository;
import com.wakana.vefa.repository.UserRepository;
import com.wakana.vefa.services.EmailService;
import com.wakana.vefa.services.FeeService;
import com.wakana.vefa.services.NotificationsService;
import com.wakana.vefa.services.OwnerPropertyAreaService;
import com.wakana.vefa.services.RealEstatePropertyService;
import com.wakana.vefa.services.SubscriptionService;
import com.wakana.vefa.services.SubscriptionServices;
import com.wakana.vefa.specification.RealEstatePropertySpecification;
import com.wakana.vefa.util.ProfilEnumUtils;
import com.wakana.vefa.util.XssSanitizer;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class RealEstatePropertyServiceImpl implements RealEstatePropertyService {
        private final PaymentCallRepository paymentCallRepository;
        private final RealEstatePropertyRepository realEstatePropertyRepository;
        private final UserRepository userRepository;
        private final PropertyTypeRepository propertyTypeRepository;
        private final FeeService feeService;
        private final OwnerPropertyAreaService ownerPropertyAreaService;

        private final ReservationRepository reservationRepository;

        private final OwnerPropertyAreaRepository ownerPropertyAreaRepository;

        private final CallForChargeRepository callForChargeRepository;
        private final ConstructionPhaseIndicatorRepository constructionPhaseIndicatorRepository;
        @Autowired
        private EmailService emailService;

        @Autowired
        private NotificationsService notificationsService;

        private final SubscriptionServices subscriptionsService;

        @Override
        public RealEstateProperty save(RealEstatePropertyRequest request, String planFileName,
                        String legalStatusFileName,
                        List<String> pictureUrls) {
                RealEstateProperty realEstateProperty = new RealEstateProperty();

                Fee fee = feeService.getPlanByName(FeeEnum.LOTFEE);
                ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Africa/Dakar"));

                long timestamp = now.toEpochSecond();
                realEstateProperty.setLotFee(fee);
                realEstateProperty.setLotFeesPaid(false);
                realEstateProperty.setName(XssSanitizer.clean(request.getName()));
                realEstateProperty.setNumber(XssSanitizer.clean(request.getNumber()));
                realEstateProperty.setAddress(XssSanitizer.clean(request.getAddress()));
                realEstateProperty.setPrice(request.getPrice());
                realEstateProperty.setNumberOfRooms(request.getNumberOfRooms());
                realEstateProperty.setArea(request.getArea());
                realEstateProperty.setLatitude(Double.parseDouble(request.getLatitude()));
                realEstateProperty.setLongitude(Double.parseDouble(request.getLongitude()));
                realEstateProperty.setAvailable(request.isAvailable());
                realEstateProperty.setReservationFee(request.getReservationFee());
                realEstateProperty.setDescription(XssSanitizer.clean(request.getDescription()));
                realEstateProperty.setNumberOfLots(request.getNumberOfLots());
                realEstateProperty.setDiscount(request.getDiscount());
                realEstateProperty.setLevel(request.getLevel());
                realEstateProperty.setFeesFile(request.getFeesFile());
                realEstateProperty.setTimestamp(timestamp);
                realEstateProperty.setRental(request.isRental());
                // Gérer les champs booléens pour les équipements communs
                realEstateProperty.setHasHall(request.isHasHall());
                realEstateProperty.setHasParking(request.isHasParking());
                realEstateProperty.setHasElevator(request.isHasElevator());
                realEstateProperty.setHasSwimmingPool(request.isHasSwimmingPool());
                realEstateProperty.setHasGym(request.isHasGym());
                realEstateProperty.setHasPlayground(request.isHasPlayground());
                realEstateProperty.setHasSecurityService(request.isHasSecurityService());
                realEstateProperty.setHasGarden(request.isHasGarden());
                realEstateProperty.setHasSharedTerrace(request.isHasSharedTerrace());
                realEstateProperty.setHasBicycleStorage(request.isHasBicycleStorage());
                realEstateProperty.setHasLaundryRoom(request.isHasLaundryRoom());
                realEstateProperty.setHasStorageRooms(request.isHasStorageRooms());
                realEstateProperty.setHasWasteDisposalArea(request.isHasWasteDisposalArea());
                realEstateProperty.setStatus(RealEstatePropertyStatusEnum.AVAILABLE);
                realEstateProperty.setCoOwner(request.isCoOwner());
                realEstateProperty.setMezzanine(request.isMezzanine());
                // Set promoter, recipient, and notary
                User promoter = userRepository.findById(request.getPromoterId())
                                .orElseThrow(() -> new RuntimeException("Promoter not found"));
                realEstateProperty.setPromoter(promoter);
                if (request.getRecipientId() != 0) {
                        User recipient = userRepository.findById(request.getRecipientId()).orElse(null);
                        realEstateProperty.setRecipient(recipient);
                }
                if (request.getNotaryId() != 0) {
                        User notary = userRepository.findById(request.getNotaryId()).orElse(null);
                        realEstateProperty.setNotary(notary);
                }
                if (request.getPropertyTypeId() != 0) {
                        PropertyType propertyType = propertyTypeRepository.findById(request.getPropertyTypeId())
                                        .orElse(null);
                        realEstateProperty.setPropertyType(propertyType);
                }

                if (request.getAgencyId() != 0) {
                        User agency = userRepository.findById(request.getAgencyId()).orElse(null);
                        realEstateProperty.setAgency(agency);

                }

                if (request.getBankId() != 0) {
                        User bank = userRepository.findById(request.getBankId()).orElse(null);
                        realEstateProperty.setBank(bank);

                }

                // Set parent property if applicable
                if (request.getParentPropertyId() != 0) {
                        RealEstateProperty parentProperty = realEstatePropertyRepository
                                        .findById(request.getParentPropertyId())
                                        .orElse(null);
                        realEstateProperty.setParentProperty(parentProperty);
                }

                // Set file names for plan, legal status, and pictures
                realEstateProperty.setPlan(planFileName);
                realEstateProperty.setLegalStatus(legalStatusFileName);
                realEstateProperty.setPictures(pictureUrls);

                Subscriptions sub = subscriptionsService.getUserSubscriptionByPlan(promoter.getId(),
                                ProfilEnumUtils.toProfilEnum(request.getProfil()));

                realEstateProperty.setSubscription(sub);

                // Save the RealEstateProperty entity
                RealEstateProperty savedProperty = realEstatePropertyRepository.save(realEstateProperty);
                if (savedProperty != null && promoter.getId() != 0) {
                        subscriptionsService.onProjectCreated(promoter.getId(), request.getProfil());
                }

                if (!request.isRental()) {
                        // Automatically create the three default construction phase indicators
                        List<ConstructionPhaseIndicator> indicators = List.of(
                                        new ConstructionPhaseIndicator("Gros œuvre", savedProperty),
                                        new ConstructionPhaseIndicator("Second œuvre", savedProperty),
                                        new ConstructionPhaseIndicator("Finition", savedProperty));

                        // Save the indicators
                        constructionPhaseIndicatorRepository.saveAll(indicators);

                        PaymentCall paymentCall = new PaymentCall();
                        paymentCall.setLabel("Frais de dossier");
                        paymentCall.setPercentage(0);
                        paymentCall.setExpectedDate("");
                        paymentCall.setAmount(request.getFeesFile());
                        paymentCall.setStatus(PaymentCallStatusEnum.PENDING);
                        paymentCall.setRealEstateProperty(realEstateProperty);
                        paymentCall.setCreatedAt(System.currentTimeMillis());
                        paymentCall.setUpdatedAt(System.currentTimeMillis());
                        paymentCallRepository.save(paymentCall);
                }

                return savedProperty;

        }

        @Override
        public RealEstateProperty update(Long id, RealEstatePropertyRequest request, String planFileName,
                        String legalStatusFileName, List<String> pictureUrls) {
                // Retrieve the existing property
                RealEstateProperty existingProperty = realEstatePropertyRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));

                Fee fee = feeService.getPlanByName(FeeEnum.LOTFEE);

                // Update fields
                existingProperty.setLotFee(fee);
                existingProperty.setLotFeesPaid(false);
                existingProperty.setName(XssSanitizer.clean(request.getName()));
                existingProperty.setNumber(XssSanitizer.clean(request.getNumber()));
                existingProperty.setAddress(XssSanitizer.clean(request.getAddress()));
                existingProperty.setPrice(request.getPrice());
                existingProperty.setNumberOfRooms(request.getNumberOfRooms());
                existingProperty.setArea(request.getArea());
                existingProperty.setLatitude(Double.parseDouble(request.getLatitude()));
                existingProperty.setLongitude(Double.parseDouble(request.getLongitude()));
                existingProperty.setAvailable(request.isAvailable());
                existingProperty.setReservationFee(request.getReservationFee());
                existingProperty.setDescription(XssSanitizer.clean(request.getDescription()));
                existingProperty.setNumberOfLots(request.getNumberOfLots());
                existingProperty.setDiscount(request.getDiscount());
                existingProperty.setLevel(request.getLevel());
                existingProperty.setFeesFile(request.getFeesFile());
                existingProperty.setRental(request.isRental());
                // Mettre à jour les champs booléens pour les équipements communs
                existingProperty.setHasHall(request.isHasHall());
                existingProperty.setHasParking(request.isHasParking());
                existingProperty.setHasElevator(request.isHasElevator());
                existingProperty.setHasSwimmingPool(request.isHasSwimmingPool());
                existingProperty.setHasGym(request.isHasGym());
                existingProperty.setHasPlayground(request.isHasPlayground());
                existingProperty.setHasSecurityService(request.isHasSecurityService());
                existingProperty.setHasGarden(request.isHasGarden());
                existingProperty.setHasSharedTerrace(request.isHasSharedTerrace());
                existingProperty.setHasBicycleStorage(request.isHasBicycleStorage());
                existingProperty.setHasLaundryRoom(request.isHasLaundryRoom());
                existingProperty.setHasStorageRooms(request.isHasStorageRooms());
                existingProperty.setHasWasteDisposalArea(request.isHasWasteDisposalArea());
                existingProperty.setCoOwner(request.isCoOwner());
                existingProperty.setRental(request.isRental());
                existingProperty.setMezzanine(request.isMezzanine());

                // Update promoter, recipient, and notary
                User promoter = userRepository.findById(request.getPromoterId())
                                .orElseThrow(() -> new RuntimeException("Promoter not found"));
                existingProperty.setPromoter(promoter);

                if (request.getRecipientId() != 0) {
                        User recipient = userRepository.findById(request.getRecipientId()).orElse(null);
                        existingProperty.setRecipient(recipient);
                }

                if (request.getNotaryId() != 0) {
                        User notary = userRepository.findById(request.getNotaryId()).orElse(null);
                        existingProperty.setNotary(notary);
                }

                if (request.getAgencyId() != 0) {
                        User agency = userRepository.findById(request.getAgencyId()).orElse(null);
                        existingProperty.setAgency(agency);
                        ;

                }

                if (request.getBankId() != 0) {
                        User bank = userRepository.findById(request.getBankId()).orElse(null);
                        existingProperty.setBank(bank);
                        ;
                }

                // Update parent property if applicable
                if (request.getParentPropertyId() != 0) {
                        RealEstateProperty parentProperty = realEstatePropertyRepository
                                        .findById(request.getParentPropertyId())
                                        .orElse(null);
                        existingProperty.setParentProperty(parentProperty);
                }

                // Update file names for plan, legal status, and pictures
                existingProperty.setPlan(
                                planFileName != null && !planFileName.trim().isEmpty() ? planFileName
                                                : existingProperty.getPlan());
                existingProperty
                                .setLegalStatus(
                                                legalStatusFileName != null && !legalStatusFileName.trim().isEmpty()
                                                                ? legalStatusFileName
                                                                : existingProperty.getLegalStatus());

                if (pictureUrls != null && !pictureUrls.isEmpty()) {
                        List<String> existingPictures = new ArrayList<>(existingProperty.getPictures());
                        existingPictures.addAll(pictureUrls);
                        existingProperty.setPictures(existingPictures);
                }

                if (existingProperty.getSubscription() == null) {
                        Subscriptions sub = subscriptionsService.getUserSubscriptionByPlan(promoter.getId(),
                                        ProfilEnumUtils.toProfilEnum(request.getProfil()));

                        existingProperty.setSubscription(sub);
                }

                return realEstatePropertyRepository.save(existingProperty);

        }

        @Override
        public RealEstateProperty addSub(Long id, String profil) {
                // Retrieve the existing property
                RealEstateProperty existingProperty = realEstatePropertyRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));

                if (existingProperty.getSubscription() == null) {
                        Subscriptions sub = subscriptionsService.getUserSubscriptionByPlan(
                                        existingProperty.getPromoter().getId(),
                                        ProfilEnumUtils.toProfilEnum(profil));

                        existingProperty.setSubscription(sub);
                }

                return realEstatePropertyRepository.save(existingProperty);

        }

        @Transactional
        public void delete(Long id) {
                RealEstateProperty existingProperty = realEstatePropertyRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));

                // Delete all dependent entities first
                paymentCallRepository.deleteByRealEstatePropertyId(id);
                reservationRepository.deleteByPropertyId(id); // Make sure this method exists

                // Flush to ensure deletions are executed before property deletion
                reservationRepository.flush();
                paymentCallRepository.flush();

                // Now delete the property
                realEstatePropertyRepository.delete(existingProperty);
        }

        @Override
        public Page<RealEstatePropertyResponse> searchByPromoterAndParentIsNull(
                        Long promoterId,
                        ProfilEnum planName,
                        Pageable pageable) {

                return realEstatePropertyRepository
                                .searchByPromoterAndParentIsNullWithPlan(
                                                promoterId,
                                                planName,
                                                pageable)
                                .map(this::convertToResponse);
        }

        /*
         * @Override
         * public Page<RealEstatePropertyResponse> searchByPromoterAndParentIsNull(Long
         * promoterId, Pageable pageable) {
         * 
         * return realEstatePropertyRepository
         * .searchByPromoterAndParentIsNullWithPlan(
         * promoterId, pageable)
         * .map(this::convertToResponse);
         * }
         */

        // Conversion method
        private RealEstatePropertyResponse convertToResponse(RealEstateProperty property) {
                RealEstatePropertyResponse response = new RealEstatePropertyResponse();

                // Set basic fields
                response.setId(property.getId());
                response.setName(property.getName());
                response.setNumber(property.getNumber());
                response.setAddress(property.getAddress());
                response.setPrice(property.getPrice());
                response.setNumberOfRooms(property.getNumberOfRooms());
                response.setArea(property.getArea());
                response.setLatitude(property.getLatitude());
                response.setLongitude(property.getLongitude());
                response.setAvailable(property.isAvailable());
                response.setReservationFee(property.getReservationFee());
                response.setDescription(property.getDescription());
                response.setPlan(property.getPlan());
                response.setLegalStatus(property.getLegalStatus());
                response.setNumberOfLots(property.getNumberOfLots());
                response.setLevel(property.getLevel());
                response.setFeesFile(property.getFeesFile());
                response.setBudget(property.getBudget());
                response.setDiscount(property.getDiscount());
                // Set pictures
                response.setPictures(property.getPictures());
                // Set common area fields
                response.setHasHall(property.isHasHall());
                response.setHasParking(property.isHasParking());
                response.setHasElevator(property.isHasElevator());
                response.setHasSwimmingPool(property.isHasSwimmingPool());
                response.setHasGym(property.isHasGym());
                response.setHasPlayground(property.isHasPlayground());
                response.setHasSecurityService(property.isHasSecurityService());
                response.setHasGarden(property.isHasGarden());
                response.setHasSharedTerrace(property.isHasSharedTerrace());
                response.setHasBicycleStorage(property.isHasBicycleStorage());
                response.setHasLaundryRoom(property.isHasLaundryRoom());
                response.setHasStorageRooms(property.isHasStorageRooms());
                response.setHasWasteDisposalArea(property.isHasWasteDisposalArea());

                // Set status
                response.setStatus(property.getStatus());
                response.setPromoter(property.getPromoter());
                response.setNotary(property.getNotary());
                response.setPropertyType(property.getPropertyType());
                response.setRecipient(property.getRecipient());
                response.setAgency(property.getAgency());
                response.setBank(property.getBank());
                response.setCoOwner(property.isCoOwner());
                response.setOwners(property.getOwners());
                response.setRental(property.isRental());
                response.setAllocateDate(property.getAllocateDate());
                response.setMezzanine(property.isMezzanine());

                if (null != property.getParentProperty()) {
                        response.setParentProperty(property.getParentProperty());
                }

                return response;
        }

        @Override
        public Page<RealEstateProperty> findByParentPropertyId(Long parentPropertyId, Pageable pageable) {

                Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

                return realEstatePropertyRepository.findByParentPropertyIdAndRentalFalseAndCoOwnerFalse(
                                parentPropertyId,
                                pageable);
        }

        @Override
        public Page<RealEstateProperty> findAvailableByParentPropertyId(Long parentPropertyId, Pageable pageable) {

                Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

                return realEstatePropertyRepository.findByParentPropertyIdAndRentalFalseAndCoOwnerFalseAndStatus(
                                parentPropertyId,
                                RealEstatePropertyStatusEnum.AVAILABLE,
                                pageable);
        }

        public Page<RealEstateProperty> searchProperties(RealEstatePropertySearchRequest request, Pageable pageable) {
                Specification<RealEstateProperty> specification = Specification
                                .where(RealEstatePropertySpecification.hasPropertyType(request.getPropertyTypeId()))
                                .and(RealEstatePropertySpecification.hasPriceBetween(request.getMinPrice(),
                                                request.getMaxPrice()))
                               // .and(RealEstatePropertySpecification.addressContains(request.getAddress()))
                                .and(RealEstatePropertySpecification.withinDefaultRadius(request.getLatitude(),
                                                request.getLongitude()))

                                .and(RealEstatePropertySpecification.hasNameContaining(request.getName()))
                                .and(RealEstatePropertySpecification.hasNoParentProperty())
                                .and(RealEstatePropertySpecification.hasAvailableStatus())
                                .and(RealEstatePropertySpecification.isRentalFalse())
                                .and(RealEstatePropertySpecification.isCoOwnerFalse());

                return realEstatePropertyRepository.findAll(specification, pageable);

        }

        public Page<RealEstateProperty> searchLotPropertiesByParent(RealEstatePropertySearchRequest request,
                        Pageable pageable) {
                Specification<RealEstateProperty> specification = Specification
                                .where(RealEstatePropertySpecification.hasPropertyType(request.getPropertyTypeId()))
                                .and(RealEstatePropertySpecification.hasPriceBetween(request.getMinPrice(),
                                                request.getMaxPrice()))
                                .and(RealEstatePropertySpecification.addressContains(request.getAddress()))
                                .and(RealEstatePropertySpecification.withinDefaultRadius(request.getLatitude(),
                                                request.getLongitude()))
                                .and(RealEstatePropertySpecification.hasParentProperty())
                                .and(RealEstatePropertySpecification.hasAvailableStatus())
                                .and(RealEstatePropertySpecification.isRentalFalse())
                                .and(RealEstatePropertySpecification.hasParentId(request.getParentPropertyId()))
                                .and(RealEstatePropertySpecification.hasNameContaining(request.getName()))
                                .and(RealEstatePropertySpecification.isCoOwnerFalse());

                return realEstatePropertyRepository.findAll(specification, pageable);

        }

        public Page<RealEstateProperty> searchPropertiesRental(RealEstatePropertySearchRequest request,
                        Pageable pageable) {
                Specification<RealEstateProperty> specification = Specification
                                .where(RealEstatePropertySpecification.hasPropertyType(request.getPropertyTypeId()))
                                .and(RealEstatePropertySpecification.hasPriceBetween(request.getMinPrice(),
                                                request.getMaxPrice()))
                                .and(RealEstatePropertySpecification.addressContains(request.getAddress()))
                                .and(RealEstatePropertySpecification.withinDefaultRadius(request.getLatitude(),
                                                request.getLongitude()))
                                .and(RealEstatePropertySpecification.hasAvailableStatus())
                                .and(RealEstatePropertySpecification.isRentalTrue());

                return realEstatePropertyRepository.findAll(specification, pageable);
        }

        public RealEstateProperty changeStatus(Long propertyId, RealEstatePropertyStatusEnum status) {
                RealEstateProperty property = realEstatePropertyRepository.findById(propertyId)
                                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

                property.setStatus(status);
                return realEstatePropertyRepository.save(property);
        }

        public RealEstateProperty changeRecipient(Long propertyId, Long userId) {
                RealEstateProperty property = realEstatePropertyRepository.findById(propertyId)
                                .orElseThrow(() -> new EntityNotFoundException("Property not found"));

                if (userId == null) {
                        property.setRecipient(null);
                } else {
                        var recipient = userRepository.findById(userId)
                                        .orElseThrow(() -> new EntityNotFoundException("User not found"));

                        property.setRecipient(recipient);
                }

                return realEstatePropertyRepository.save(property);
        }

        @Override
        public RealEstatePropertyResponse getProperty(Long propertyId) {
                // Fetch the property from the repository (assuming realEstatePropertyRepository
                // is injected)
                RealEstateProperty property = realEstatePropertyRepository.findById(propertyId)
                                .orElseThrow();

                // Convert the entity to a response DTO
                return convertToResponse(property);
        }

        public Page<RealEstateProperty> getPropertiesByRecipientId(Long recipientId, Pageable pageable) {

                return realEstatePropertyRepository.findByRecipientIdAndRentalFalseAndCoOwnerFalse(recipientId,
                                pageable);
        }

        public Page<RealEstateProperty> getPropertiesByRecipientIdAll(Long recipientId, Pageable pageable) {

                return realEstatePropertyRepository.findByRecipientId(recipientId, pageable);
        }

        public Page<RealEstateProperty> getReservedPropertiesByNotaryId(Long notaryId, String name,
                        RealEstatePropertyStatusEnum status, Pageable pageable) {

                Specification<RealEstateProperty> spec = Specification
                                .where(RealEstatePropertySpecification.hasParentProperty())
                                .and((root, query, cb) -> cb.equal(root.get("notary").get("id"), notaryId));

                // Si aucun status n'est fourni → RESERVED
                if (status == null) {
                        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"),
                                        RealEstatePropertyStatusEnum.RESERVED));
                } else {
                        spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), status));
                }

                if (name != null && !name.isEmpty()) {
                        spec = spec.and(RealEstatePropertySpecification.hasNameContaining(name));
                }

                return realEstatePropertyRepository.findAll(spec, pageable);
        }

        @Transactional
        @Override
        public RealEstatePropertyResponse sold(Long propertyId) {
                RealEstateProperty existingProperty = realEstatePropertyRepository.findById(propertyId)
                                .orElseThrow(() -> new RuntimeException("Property not found with id: " + propertyId));

                if (existingProperty.getStatus() == RealEstatePropertyStatusEnum.RESERVED) {
                        existingProperty.setStatus(RealEstatePropertyStatusEnum.SOLD);
                        existingProperty.setSoldAt(LocalDateTime.now());

                        String subject = emailService.getPropertyHTML(
                                        existingProperty,
                                        "🏡 Vente validée par le notaire",
                                        "Félicitations ! La vente de votre bien <b>" + existingProperty.getName()
                                                        + "</b> a été officiellement validée par le notaire <b>" +
                                                        existingProperty.getNotary().getPrenom() + " "
                                                        + existingProperty.getNotary().getNom()
                                                        + "</b>. " +
                                                        // "Vous recevrez bientôt toutes les informations nécessaires
                                                        // pour finaliser les
                                                        // démarches administratives. " +
                                                        "Nous vous remercions pour votre confiance et restons à votre disposition pour toute question.",
                                        "#062b16");

                        List<String> ccEmails = Arrays.asList(
                                        existingProperty.getPromoter().getEmail(),
                                        existingProperty.getNotary().getEmail());

                        emailService.sendHtmlMessage(
                                        existingProperty.getRecipient().getEmail(),
                                        "🏡 Vente validée par le notaire",
                                        subject,
                                        ccEmails);

                        /// notifcations vers le reservataire
                        notificationsService.sendReservationNotif(
                                        " Vente validée par le notaire ",
                                        "Félicitations ! La vente de " + existingProperty.getName()
                                                        + "a été officiellement validée par le notaire "
                                                        + existingProperty.getNotary().getPrenom()
                                                        + " " + existingProperty.getNotary().getNom(),

                                        existingProperty, existingProperty.getRecipient());
                        /// notifcations vers le promoteur
                        notificationsService.sendReservationNotif(
                                        " Vente validée par le notaire ",
                                        "Félicitations ! La vente " + existingProperty.getName()
                                                        + "a été officiellement validée par le notaire "
                                                        + existingProperty.getNotary().getPrenom()
                                                        + " " + existingProperty.getNotary().getNom(),

                                        existingProperty, existingProperty.getPromoter());

                        if (existingProperty.getAgency() != null) {
                                /// notifcations vers le promoteur
                                notificationsService.sendReservationNotif(
                                                " Vente validée par le notaire ",
                                                "Félicitations ! La vente de votre bien " + existingProperty.getName()
                                                                + "a été officiellement validée par le notaire "
                                                                + existingProperty.getNotary().getPrenom()
                                                                + " " + existingProperty.getNotary().getNom(),

                                                existingProperty, existingProperty.getAgency());
                        }

                } else {
                        existingProperty.setStatus(RealEstatePropertyStatusEnum.RESERVED);
                }

                return convertToResponse(realEstatePropertyRepository.save(existingProperty));
        }

        public long countPropertiesWithoutParent() {
                return realEstatePropertyRepository.countByParentPropertyIsNullAndRentalFalseAndCoOwnerFalse();
        }

        @Override
        public Page<RealEstatePropertyResponse> searchByBankAndParentIsNull(Long id, Pageable pageable) {
                Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");

                return realEstatePropertyRepository
                                .findByBankIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalse(id, pageable)
                                .map(this::convertToResponse);
        }

        /*
         * @Override
         * public Page<RealEstatePropertyResponse> searchByAgencyAndParentIsNull(Long
         * id, Pageable pageable) {
         * Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
         * 
         * return realEstatePropertyRepository
         * .findByAgencyIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalse(id,
         * pageable)
         * .map(this::convertToResponse);
         * }
         */

        /// dash bord
        public DashboardStats getDashboardStats(Long promoterId, String planName) {

                // Conversion du nom du plan
                ProfilEnum planEnum = ProfilEnum.valueOf(planName.toUpperCase());

                // 1️⃣ Biens principaux (parent = null) AVEC subscription du plan
                long nombreDeBien = realEstatePropertyRepository
                                .countByPromoterIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalseAndSubscription_SubscriptionPlans_Name(
                                                promoterId, planEnum);

                // 2️⃣ Lots vendus (plan porté par le parent)
                long nombreDeLotsVendus = realEstatePropertyRepository
                                .countByParentProperty_Promoter_IdAndStatusAndParentProperty_Subscription_SubscriptionPlans_Name(
                                                promoterId,
                                                RealEstatePropertyStatusEnum.SOLD,
                                                planEnum);

                // 3️⃣ Lots disponibles
                long nombreDeLotsDisponible = realEstatePropertyRepository
                                .countByParentProperty_Promoter_IdAndStatusAndParentProperty_Subscription_SubscriptionPlans_Name(
                                                promoterId,
                                                RealEstatePropertyStatusEnum.AVAILABLE,
                                                planEnum);

                // 4️⃣ Lots réservés
                long nombreDeLotsReserve = realEstatePropertyRepository
                                .countByParentProperty_Promoter_IdAndStatusAndParentProperty_Subscription_SubscriptionPlans_Name(
                                                promoterId,
                                                RealEstatePropertyStatusEnum.RESERVED,
                                                planEnum);

                return new DashboardStats(
                                nombreDeBien,
                                nombreDeLotsVendus,
                                nombreDeLotsDisponible,
                                nombreDeLotsReserve);
        }

        /*
         * public DashboardStats getDashboardStats(Long id) {
         * 
         * User user = userRepository.findById(id).orElse(null);
         * 
         * if (ProfilEnum.PROMOTEUR.equals(user.getProfil())) {
         * long nombreDeBien = realEstatePropertyRepository
         * .countByPromoterIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalse(id);
         * 
         * long nombreDeLotsVendus = realEstatePropertyRepository
         * .countByPromoterIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
         * id, RealEstatePropertyStatusEnum.SOLD);
         * 
         * long nombreDeLotsDisponible = realEstatePropertyRepository
         * .countByPromoterIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
         * id, RealEstatePropertyStatusEnum.AVAILABLE);
         * 
         * long nombreDeLotsReserve = realEstatePropertyRepository
         * .countByPromoterIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
         * id, RealEstatePropertyStatusEnum.RESERVED);
         * 
         * return new DashboardStats(nombreDeBien, nombreDeLotsVendus,
         * nombreDeLotsDisponible,
         * nombreDeLotsReserve);
         * 
         * } else if (ProfilEnum.BANK.equals(user.getProfil())) {
         * 
         * long nombreDeBien = realEstatePropertyRepository
         * .countByBankIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalse(id);
         * 
         * long nombreDeLotsVendus = realEstatePropertyRepository
         * .countByParentPropertyBankIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
         * id, RealEstatePropertyStatusEnum.SOLD);
         * 
         * long nombreDeLotsDisponible = realEstatePropertyRepository
         * .countByParentPropertyBankIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
         * id, RealEstatePropertyStatusEnum.AVAILABLE);
         * 
         * long nombreDeLotsReserve = realEstatePropertyRepository
         * .countByParentPropertyBankIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
         * id, RealEstatePropertyStatusEnum.RESERVED);
         * 
         * return new DashboardStats(nombreDeBien, nombreDeLotsVendus,
         * nombreDeLotsDisponible,
         * nombreDeLotsReserve);
         * 
         * } else {
         * long nombreDeBien = realEstatePropertyRepository
         * .countByAgencyIdAndParentPropertyIsNullAndRentalFalseAndCoOwnerFalse(id);
         * 
         * long nombreDeLotsVendus = realEstatePropertyRepository
         * .countByParentPropertyAgencyIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
         * id, RealEstatePropertyStatusEnum.SOLD);
         * 
         * long nombreDeLotsDisponible = realEstatePropertyRepository
         * .countByParentPropertyAgencyIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
         * id, RealEstatePropertyStatusEnum.AVAILABLE);
         * 
         * long nombreDeLotsReserve = realEstatePropertyRepository
         * .countByParentPropertyAgencyIdAndParentPropertyIsNotNullAndStatusAndRentalFalseAndCoOwnerFalse(
         * id, RealEstatePropertyStatusEnum.RESERVED);
         * 
         * return new DashboardStats(nombreDeBien, nombreDeLotsVendus,
         * nombreDeLotsDisponible,
         * nombreDeLotsReserve);
         * }
         * 
         * }
         */

        @Override
        public RealEstatePropertyResponse payLotFee(Long propertyId) {
                RealEstateProperty existingProperty = realEstatePropertyRepository.findById(propertyId)
                                .orElseThrow(() -> new RuntimeException("Property not found with id: " + propertyId));
                existingProperty.setLotFeesPaid(true);

                return convertToResponse(realEstatePropertyRepository.save(existingProperty));
        }

        /// copropriete

        @Override

        public RealEstatePropertyResponse addOwner(Long propertyId, Long userId, double area) {
                // Rechercher la propriété existante
                RealEstateProperty existingProperty = realEstatePropertyRepository.findById(propertyId)
                                .orElseThrow(() -> new RuntimeException("Property not found with id: " + propertyId));

                // Rechercher l'utilisateur existant
                User owner = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

                // Ajouter l'utilisateur à la liste des propriétaires de la propriété
                if (existingProperty.getOwners() == null) {
                        existingProperty.setOwners(new HashSet<>());
                }
                existingProperty.getOwners().add(owner);

                // Ajouter la propriété à la liste des propriétés de l'utilisateur
                if (owner.getProperties() == null) {
                        owner.setProperties(new HashSet<>());
                }
                owner.getProperties().add(existingProperty);

                // Sauvegarder les modifications
                RealEstateProperty savedProperty = realEstatePropertyRepository.save(existingProperty);
                userRepository.save(owner);

                ownerPropertyAreaService.add(propertyId, userId, area);

                return convertToResponse(savedProperty);

        }

        @Override
        public Page<RealEstatePropertyResponse> searchByPromoterAndSyndic(
                        Long promoterId,
                        ProfilEnum planName,
                        Pageable pageable) {

                return realEstatePropertyRepository
                                .searchByPromoterAndSyndicWithPlan(
                                                promoterId,
                                                planName,
                                                pageable)
                                .map(this::convertToResponse);
        }

        /*
         * @Override
         * public Page<RealEstatePropertyResponse> findByPromoterIdAndCoOwnerTrue(Long
         * promoterId, Pageable pageable) {
         * return realEstatePropertyRepository
         * .findByPromoterIdAndCoOwnerTrueAndRentalFalse(promoterId, pageable)
         * .map(this::convertToResponse);
         * }
         */

        @Override
        public Set<RealEstateProperty> findPropertiesByOwnerId(Long userId) {

                return realEstatePropertyRepository.findPropertiesByOwnerId(userId);
        }

        @Override
        public RealEstatePropertyResponse addBudget(Long id, double budget) {
                RealEstateProperty existingProperty = realEstatePropertyRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));
                existingProperty.setBudget(budget);

                return convertToResponse(realEstatePropertyRepository.save(existingProperty));
        }

        public void allocateBudgetAndGenerateCalls(Long propertyId) {
                // Récupérer la propriété immobilière
                /*
                 * RealEstateProperty property =
                 * realEstatePropertyRepository.findById(propertyId)
                 * .orElseThrow(() -> new IllegalArgumentException("Propriété non trouvée"));
                 * 
                 * // Récupérer les zones possédées par les propriétaires pour cette propriété
                 * List<OwnerPropertyArea> ownerAreas =
                 * ownerPropertyAreaRepository.findByRealEstatePropertyId(propertyId);
                 * 
                 * // Calculer la superficie totale possédée
                 * double totalArea = ownerAreas.stream()
                 * .mapToDouble(OwnerPropertyArea::getAreaInSquareMeters)
                 * .sum();
                 * 
                 * // Parcourir chaque propriétaire et calculer sa part du budget
                 * for (OwnerPropertyArea ownerArea : ownerAreas) {
                 * User owner = ownerArea.getOwner();
                 * double ownerAreaSize = ownerArea.getAreaInSquareMeters();
                 * // Calculer le pourcentage de possession
                 * double ownershipPercentage = ownerAreaSize / totalArea;
                 * // Calculer la part du budget
                 * double ownerBudgetShare = ownershipPercentage * property.getBudget();
                 * 
                 * // Diviser la part du budget en 12 mensualités
                 * double monthlyCharge = ownerBudgetShare / 12;
                 * 
                 * // Générer des appels de fonds mensuels
                 * for (int month = 1; month <= 12; month++) {
                 * CallForCharge call = new CallForCharge();
                 * call.setLabel("Appel de charge - Mois " + month);
                 * call.setFee(monthlyCharge);
                 * call.setExpectedDate(LocalDate.now().plusMonths(month).toString());
                 * call.setStatus(PaymentCallStatusEnum.PENDING);
                 * call.setRealEstateProperty(property);
                 * call.setOwner(owner);
                 * call.setCreatedAt(System.currentTimeMillis());
                 * // Enregistrer l'appel de fonds
                 * callForChargeRepository.save(call);
                 * }
                 * }
                 * 
                 * property.setAllocateDate(LocalDateTime.now());
                 * realEstatePropertyRepository.save(property);
                 */
        }
        //// locations

        @Override
        public Page<RealEstatePropertyResponse> searchByPromoterAndRental(
                        Long promoterId,
                        ProfilEnum planName,
                        Pageable pageable) {

                return realEstatePropertyRepository
                                .searchByPromoterAndRentalWithPlan(
                                                promoterId,
                                                planName,
                                                pageable)
                                .map(this::convertToResponse);
        }

        /*
         * @Override
         * public Page<RealEstatePropertyResponse> searchByPromoterAndRental(Long
         * promoterId, Pageable pageable) {
         * 
         * return realEstatePropertyRepository
         * .findByPromoterIdAndParentPropertyIsNullAndRentalTrueAndCoOwnerFalseOrderByIdDesc(
         * promoterId, pageable)
         * .map(this::convertToResponse);
         * }
         */

        // dahs bord

        @Override
        public List<ReservationStats> getSalesByMonth(int year, Long promoterId) {
                List<RealEstateProperty> soldProperties = fetchSoldProperties(year, null, promoterId);

                return generateMonthlySalesStats(soldProperties);
        }

        @Override
        public List<ReservationStats> getSalesByMonthByPropertyId(int year, Long parentPropertyId, Long promoterId) {
                List<RealEstateProperty> soldProperties = fetchSoldProperties(year, parentPropertyId, promoterId);

                return generateMonthlySalesStats(soldProperties);
        }

        @Override
        public DashboardStats getLotsDashboardStats(
                        Long parentPropertyId,
                        String planName) {

                ProfilEnum profilEnum;
                try {
                        profilEnum = ProfilEnum.valueOf(planName.toUpperCase());
                } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Plan invalide : " + planName);
                }

                long totalLots = realEstatePropertyRepository
                                .countByParentProperty_IdAndSubscription_SubscriptionPlans_Name(
                                                parentPropertyId,
                                                profilEnum);

                long lotsSold = realEstatePropertyRepository
                                .countByParentProperty_IdAndStatusAndSubscription_SubscriptionPlans_Name(
                                                parentPropertyId,
                                                RealEstatePropertyStatusEnum.SOLD,
                                                profilEnum);

                long lotsReserved = realEstatePropertyRepository
                                .countByParentProperty_IdAndStatusAndSubscription_SubscriptionPlans_Name(
                                                parentPropertyId,
                                                RealEstatePropertyStatusEnum.RESERVED,
                                                profilEnum);

                long lotsAvailable = totalLots - (lotsSold + lotsReserved);

                return new DashboardStats(
                                totalLots,
                                lotsSold,
                                lotsAvailable,
                                lotsReserved);
        }

        /// fetch
        private List<RealEstateProperty> fetchSoldProperties(int year, Long parentPropertyId, Long promoterId) {
                User user = userRepository.findById(promoterId)
                                .orElseThrow(() -> new IllegalArgumentException("Utilisateur introuvable."));

                LocalDateTime startDate = LocalDateTime.of(year, 1, 1, 0, 0);
                LocalDateTime endDate = LocalDateTime.of(year, 12, 31, 23, 59);

                if (user.getProfil() == ProfilEnum.AGENCY) {
                        return (parentPropertyId == null)
                                        ? realEstatePropertyRepository.findBySoldAtBetweenAndParentProperty_Agency_Id(
                                                        startDate, endDate,
                                                        promoterId)
                                        : realEstatePropertyRepository
                                                        .findBySoldAtBetweenAndParentProperty_IdAndParentProperty_Agency_Id(
                                                                        startDate, endDate, parentPropertyId,
                                                                        promoterId);
                } else if (user.getProfil() == ProfilEnum.BANK) {

                        return (parentPropertyId == null)
                                        ? realEstatePropertyRepository.findBySoldAtBetweenAndParentProperty_Bank_Id(
                                                        startDate, endDate,
                                                        promoterId)
                                        : realEstatePropertyRepository
                                                        .findBySoldAtBetweenAndParentProperty_IdAndParentProperty_Bank_Id(
                                                                        startDate, endDate, parentPropertyId,
                                                                        promoterId);
                }

                else {
                        return (parentPropertyId == null)
                                        ? realEstatePropertyRepository.findBySoldAtBetweenAndPromoter_Id(startDate,
                                                        endDate, promoterId)
                                        : realEstatePropertyRepository
                                                        .findBySoldAtBetweenAndParentProperty_IdAndPromoter_Id(
                                                                        startDate, endDate, parentPropertyId,
                                                                        promoterId);
                }
        }

        private List<ReservationStats> generateMonthlySalesStats(List<RealEstateProperty> soldProperties) {
                Map<Integer, Long> salesByMonth = soldProperties.stream()
                                .collect(Collectors.groupingBy(prop -> prop.getSoldAt().getMonthValue(),
                                                Collectors.counting()));

                return IntStream.rangeClosed(1, 12)
                                .mapToObj(month -> new ReservationStats(month,
                                                salesByMonth.getOrDefault(month, 0L).intValue()))
                                .collect(Collectors.toList());
        }

        @Override
        public void cancelRes(Long id) {

                RealEstateProperty existingProperty = realEstatePropertyRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Property not found with id: " + id));

                existingProperty.setStatus(RealEstatePropertyStatusEnum.AVAILABLE);
                existingProperty.setRecipient(null);
                realEstatePropertyRepository.save(existingProperty);
                /// cancel res

                List<Reservation> allReservations = reservationRepository
                                .findByProperty(existingProperty);
                for (Reservation res : allReservations) {
                        res.setStatus(ReservationStatus.PROSPECT);
                        reservationRepository.save(res);
                }

        }

        @Override
        public Page<RealEstatePropertyResponse> searchByAgencyAndParentIsNull(
                        Long agencyId,
                        ProfilEnum planName,
                        Pageable pageable) {

                return realEstatePropertyRepository
                                .searchByAgencyAndParentIsNullWithPlan(
                                                agencyId,
                                                planName,
                                                pageable)
                                .map(this::convertToResponse);
        }

        @Override
        public RealEstatePropertyResponse removeOwner(Long propertyId, Long userId) {

                // 1️⃣ Récupérer la propriété
                RealEstateProperty property = realEstatePropertyRepository.findById(propertyId)
                                .orElseThrow(() -> new RuntimeException("Property not found with id: " + propertyId));

                // 2️⃣ Récupérer l'utilisateur
                User owner = userRepository.findById(userId)
                                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

                // 3️⃣ Supprimer la relation Property ↔ Owner
                if (property.getOwners() != null) {
                        property.getOwners().remove(owner);
                }

                if (owner.getProperties() != null) {
                        owner.getProperties().remove(property);
                }

                // 4️⃣ Supprimer OwnerPropertyArea correspondant
                ownerPropertyAreaService.deleteByOwnerAndProperty(userId, propertyId);

                // 5️⃣ Sauvegarder
                realEstatePropertyRepository.save(property);
                userRepository.save(owner);

                return convertToResponse(property);
        }

}
