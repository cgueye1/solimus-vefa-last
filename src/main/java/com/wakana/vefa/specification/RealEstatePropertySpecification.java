package com.wakana.vefa.specification;

import com.wakana.vefa.enums.RealEstatePropertyStatusEnum;
import com.wakana.vefa.model.RealEstateProperty;

import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;

import org.springframework.data.jpa.domain.Specification;

public class RealEstatePropertySpecification {
    // Default radius of 5 kilometers for the search
    private static final double DEFAULT_RADIUS_KM = 2.0;
    private static final double EARTH_RADIUS_KM = 6371.0;

    public static Specification<RealEstateProperty> hasPropertyType(Long propertyTypeId) {
        return (root, query, criteriaBuilder) -> propertyTypeId != null
                ? criteriaBuilder.equal(root.get("propertyType").get("id"), propertyTypeId)
                : null;
    }

    public static Specification<RealEstateProperty> hasParentId(Long parentId) {
        return (root, query, criteriaBuilder) -> {
            if (parentId != null) {
                return criteriaBuilder.equal(root.get("parentProperty").get("id"), parentId);
            }
            return null;
        };
    }

    public static Specification<RealEstateProperty> hasPriceBetween(Double minPrice, Double maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice != null && maxPrice != null) {
                return criteriaBuilder.between(root.get("price"), minPrice, maxPrice);
            } else if (minPrice != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
            } else if (maxPrice != null) {
                return criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
            }
            return null;
        };
    }

    public static Specification<RealEstateProperty> addressContains(String address) {
        return (root, query, criteriaBuilder) -> address != null && !address.isEmpty()
                ? criteriaBuilder.like(root.get("address"), "%" + address + "%")
                : null;
    }

    // Method to find properties within the default radius of the provided location
    // Method to find properties within the default radius of the provided location
    public static Specification<RealEstateProperty> withinDefaultRadius(
            String latitude, String longitude) {

        return (root, query, cb) -> {

            if (latitude == null || longitude == null) {
                return cb.conjunction();
            }

            double lat = Double.parseDouble(latitude);
            double lon = Double.parseDouble(longitude);

            double latTolerance = Math.toDegrees(DEFAULT_RADIUS_KM / EARTH_RADIUS_KM);
            double lonTolerance = Math.toDegrees(
                    DEFAULT_RADIUS_KM / (EARTH_RADIUS_KM * Math.cos(Math.toRadians(lat))));

            double minLat = lat - latTolerance;
            double maxLat = lat + latTolerance;
            double minLon = lon - lonTolerance;
            double maxLon = lon + lonTolerance;


            return cb.and(
                    cb.between(root.get("latitude"), minLat, maxLat),
                    cb.between(root.get("longitude"), minLon, maxLon));
        };
    }

    // Méthode pour vérifier que parentProperty est différent de null
    public static Specification<RealEstateProperty> hasParentProperty() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNotNull(root.get("parentProperty"));
    }

    // Méthode pour vérifier que parentProperty est différent de null
    public static Specification<RealEstateProperty> hasNoParentProperty() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("parentProperty"));
    }

    // Méthode pour vérifier que status est soit null soit 'AVAILABLE'
    public static Specification<RealEstateProperty> hasAvailableStatus() {
        return (root, query, criteriaBuilder) -> {
            return criteriaBuilder.or(
                    criteriaBuilder.isNull(root.get("status")),
                    criteriaBuilder.equal(root.get("status"), RealEstatePropertyStatusEnum.AVAILABLE));
        };
    }

    public static Specification<RealEstateProperty> hasNameContaining(String name) {
        return (root, query, cb) -> {
            if (name != null && !name.trim().isEmpty()) {
                // Normalisation de l'entrée utilisateur
                String normalizedInput = name.replaceAll("\\s+", "").toLowerCase();

                // Normalisation du champ root.name : suppression espaces + minuscule
                Expression<String> normalizedOwnName = cb.lower(
                        cb.function("REPLACE", String.class, root.get("name"),
                                cb.literal(" "), cb.literal("")));

                // Vérifie si le parentProperty existe avant de tenter d'accéder à son nom
                Expression<String> normalizedParentName = cb.lower(
                        cb.function("REPLACE", String.class,
                                root.join("parentProperty", JoinType.LEFT).get("name"),
                                cb.literal(" "), cb.literal("")));

                return cb.or(
                        cb.like(normalizedOwnName, "%" + normalizedInput + "%"),
                        cb.like(normalizedParentName, "%" + normalizedInput + "%"));
            }
            return cb.conjunction(); // retourne une condition neutre (true)
        };
    }

    public static Specification<RealEstateProperty> isRentalFalse() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("rental"));
    }

    public static Specification<RealEstateProperty> isCoOwnerFalse() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isFalse(root.get("coOwner"));
    }

    public static Specification<RealEstateProperty> isRentalTrue() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("rental"));
    }

}
