package com.wakana.vefa.security;

import java.util.List;

public class WeakPasswordException extends RuntimeException {

    private final List<String> violations;

    public WeakPasswordException(List<String> violations) {
        super("Mot de passe trop faible : " + String.join(" | ", violations));
        this.violations = violations;
    }

    public List<String> getViolations() {
        return violations;
    }
}