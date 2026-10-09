package com.wakana.vefa.enums;

public enum PaymentCallStatusEnum {
    PENDING("PENDING"),
    PAID("PAID"),
    OVERDUE("OVERDUE");

    private final String value;

    PaymentCallStatusEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
