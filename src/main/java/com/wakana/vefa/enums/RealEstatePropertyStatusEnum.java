package com.wakana.vefa.enums;

public enum RealEstatePropertyStatusEnum {
    AVAILABLE("AVAILABLE"),
    RESERVED("RESERVED"),
    SOLD("SOLD");

    private final String value;

    RealEstatePropertyStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
