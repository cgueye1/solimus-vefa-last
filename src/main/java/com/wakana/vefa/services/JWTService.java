package com.wakana.vefa.services;

import java.util.Map;

import org.springframework.security.core.userdetails.UserDetails;

import com.wakana.vefa.enums.ProfilEnum;

import jakarta.servlet.http.HttpServletRequest;

public interface JWTService {
    String extractUserName(String token);

    String generateToken(UserDetails userDetails,Long userId, ProfilEnum  profil);

    String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails,Long userId ,ProfilEnum  profil);

    boolean isTokenValid(String token, UserDetails userDetails);

    boolean isRefreshTokenValid(String token, UserDetails userDetails);

    String extractToken(HttpServletRequest request);

    Long extractUserId(String token);

    String extractProfil(String token);
}
