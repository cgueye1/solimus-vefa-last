
package com.wakana.vefa.enums;

public enum ProfilEnum {
    PROMOTEUR("PROMOTEUR"),
    NOTAIRE("NOTAIRE"),
    RESERVATAIRE("RESERVATAIRE"),
    BANK("BANK"),
    AGENCY("AGENCY"),
    ADMIN("ADMIN"),

    PROPRIETAIRE("PROPRIETAIRE"),

    SYNDIC("SYNDIC"),
    LOCATAIRE("LOCATAIRE"),
    PRESTATAIRE("PRESTATAIRE"),
    TOM("TOM"),
    COPROPRIETAIRE("COPROPRIETAIRE"),
    TENANT("TENENT");

    private final String value;

    ProfilEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
