package com.wakana.vefa.util;

import java.security.SecureRandom;
/* 
public class PasswordUtil {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int PASSWORD_LENGTH = 6;
    private static final SecureRandom random = new SecureRandom();

    public static String generateRandomPassword() {
        StringBuilder sb = new StringBuilder(PASSWORD_LENGTH);
        for (int i = 0; i < PASSWORD_LENGTH; i++) {
            sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
        }
        return sb.toString();
    }
}
*/



import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utilitaire de génération de mots de passe forts.
 * Garantit la présence de tous les types de caractères requis.
 */
public class PasswordUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String UPPERCASE  = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String LOWERCASE  = "abcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS     = "0123456789";
    private static final String SPECIALS   = "!@#$%^&*()-_=+[]{}|;:,.<>?";
    private static final String ALL_CHARS  = UPPERCASE + LOWERCASE + DIGITS + SPECIALS;

    private static final int DEFAULT_LENGTH = 12;

    private PasswordUtil() {}

    /**
     * Génère un mot de passe fort de longueur par défaut (12 caractères).
     * Garantit : 1 majuscule, 1 minuscule, 2 chiffres, 2 spéciaux.
     */
    public static String generateRandomPassword() {
        return generateRandomPassword(DEFAULT_LENGTH);
    }

    /**
     * Génère un mot de passe fort de la longueur demandée (minimum 8).
     */
    public static String generateRandomPassword(int length) {
        if (length < 8) length = 8;

        List<Character> chars = new ArrayList<>();

        // Garantir au moins 1 de chaque type requis
        chars.add(randomChar(UPPERCASE));
        chars.add(randomChar(LOWERCASE));
        chars.add(randomChar(DIGITS));
        chars.add(randomChar(DIGITS));
        chars.add(randomChar(SPECIALS));
        chars.add(randomChar(SPECIALS));

        // Remplir le reste avec des caractères aléatoires
        for (int i = chars.size(); i < length; i++) {
            chars.add(randomChar(ALL_CHARS));
        }

        // Mélanger pour éviter un pattern prévisible
        Collections.shuffle(chars, RANDOM);

        StringBuilder sb = new StringBuilder();
        for (char c : chars) sb.append(c);
        return sb.toString();
    }

    private static char randomChar(String pool) {
        return pool.charAt(RANDOM.nextInt(pool.length()));
    }
}