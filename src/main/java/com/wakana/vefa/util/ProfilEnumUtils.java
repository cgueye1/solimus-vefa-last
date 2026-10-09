package com.wakana.vefa.util;

import com.wakana.vefa.enums.ProfilEnum;

public final class ProfilEnumUtils {

  
    /**
     * Convertit une String en ProfilEnum (case-insensitive)
     *
     * @param profil valeur reçue (ex: "basic", "BASIC", "Basic")
     * @return ProfilEnum correspondant
     * @throws IllegalArgumentException si le profil est invalide
     */
    public static ProfilEnum toProfilEnum(String profil) {
        if (profil == null || profil.trim().isEmpty()) {
            throw new IllegalArgumentException("Profil cannot be null or empty");
        }

        try {
            return ProfilEnum.valueOf(profil.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid profil: " + profil);
        }
    }
}
