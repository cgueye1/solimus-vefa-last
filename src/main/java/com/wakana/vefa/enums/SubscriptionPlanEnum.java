
package com.wakana.vefa.enums;

public enum SubscriptionPlanEnum {
    PROMOTEUR_PLAN(" PROMOTEUR_PLAN"),
    NOTAIRE_PLAN("NOTAIRE_PLAN"),
    AGENCY_PLAN("AGENCY_PLAN"),
    BANK_PLAN("BANK_PLAN"),
    JOB_PROVIDER("JOB_PROVIDER"),
     SYNDIC_PLAN("SYNDIC_PLAN");

    private final String value;

    SubscriptionPlanEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
