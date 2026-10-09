package com.wakana.vefa.dto;

import lombok.Data;

@Data
public class RealEstatePropertySearchRequest {
  private Long promoterId; // ID du promoteur
  private Long parentPropertyId; // ID du parent property
  private String name;
  private Long propertyTypeId;
  private Double minPrice;
  private Double maxPrice;
  private String address;
  private String latitude; // Latitude for location-based search
  private String longitude; // Longitude for location-based search
  private String planName;

}
