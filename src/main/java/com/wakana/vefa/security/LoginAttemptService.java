package com.wakana.vefa.security;



import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Protection contre les attaques brute-force.
 * Bloque un identifiant après MAX_ATTEMPTS échecs pendant BLOCK_DURATION_MINUTES.
 *
 * Dépendance Guava déjà présente dans Spring Boot.
 */
@Slf4j
@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS       = 5;
    private static final int BLOCK_DURATION_MIN = 15;

    // Cache : clé = login (email ou téléphone), valeur = nombre de tentatives échouées
    private final LoadingCache<String, Integer> attemptsCache;

    public LoginAttemptService() {
        attemptsCache = CacheBuilder.newBuilder()
                .expireAfterWrite(BLOCK_DURATION_MIN, TimeUnit.MINUTES)
                .build(new CacheLoader<>() {
                    @Override
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    /** Appelé après une connexion réussie → remet le compteur à 0. */
    public void loginSucceeded(String login) {
        attemptsCache.invalidate(normalize(login));
        log.debug("[Auth] Connexion réussie pour : {}", normalize(login));
    }

    /** Appelé après un échec → incrémente le compteur. */
    public void loginFailed(String login) {
        String key = normalize(login);
        int attempts;
        try {
            attempts = attemptsCache.get(key);
        } catch (ExecutionException e) {
            attempts = 0;
        }
        attempts++;
        attemptsCache.put(key, attempts);
        log.warn("[Auth] Échec #{} pour : {}", attempts, key);
    }

    /** Retourne true si le compte est bloqué. */
    public boolean isBlocked(String login) {
        try {
            return attemptsCache.get(normalize(login)) >= MAX_ATTEMPTS;
        } catch (ExecutionException e) {
            return false;
        }
    }

    /** Nombre de tentatives restantes avant blocage. */
    public int remainingAttempts(String login) {
        try {
            return Math.max(0, MAX_ATTEMPTS - attemptsCache.get(normalize(login)));
        } catch (ExecutionException e) {
            return MAX_ATTEMPTS;
        }
    }

    private String normalize(String login) {
        return login == null ? "" : login.toLowerCase().trim();
    }
}