package com.wakana.vefa.security;


import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validateur de mot de passe fort.
 * Règles : min 8 chars, 1 majuscule, 1 minuscule, 1 chiffre, 1 caractère spécial.
 */
@Component
public class PasswordValidator {

    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE  = Pattern.compile("[A-Z]");
    private static final Pattern LOWERCASE  = Pattern.compile("[a-z]");
    private static final Pattern DIGIT      = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL    = Pattern.compile("[^a-zA-Z0-9]");

    /**
     * Valide le mot de passe et lève une exception si invalide.
     *
     * @param password mot de passe en clair
     * @throws WeakPasswordException si le mot de passe ne respecte pas les règles
     */
    public void validate(String password) {
        List<String> errors = getViolations(password);
        if (!errors.isEmpty()) {
            throw new WeakPasswordException(errors);
        }
    }

    /**
     * Retourne la liste des violations (vide = mot de passe valide).
     */
    public List<String> getViolations(String password) {
        List<String> errors = new ArrayList<>();

        if (password == null || password.isBlank()) {
            errors.add("Le mot de passe est obligatoire.");
            return errors;
        }

        if (password.length() < MIN_LENGTH) {
            errors.add("Le mot de passe doit contenir au moins " + MIN_LENGTH + " caractères.");
        }
        if (!UPPERCASE.matcher(password).find()) {
            errors.add("Le mot de passe doit contenir au moins une lettre majuscule.");
        }
        if (!LOWERCASE.matcher(password).find()) {
            errors.add("Le mot de passe doit contenir au moins une lettre minuscule.");
        }
        if (!DIGIT.matcher(password).find()) {
            errors.add("Le mot de passe doit contenir au moins un chiffre.");
        }
        if (!SPECIAL.matcher(password).find()) {
            errors.add("Le mot de passe doit contenir au moins un caractère spécial (!@#$%^&*...).");
        }

        return errors;
    }

    /**
     * Vérifie sans lever d'exception.
     */
    public boolean isValid(String password) {
        return getViolations(password).isEmpty();
    }
}