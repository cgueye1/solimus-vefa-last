package com.wakana.vefa.enums;

public  enum ReservationStatus {
    PROSPECT("PROSPECT"),
    RESERVED("RESERVED"),
    REJECTED("REJECTED"),
    CANCELLED("CANCELLED");

    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
