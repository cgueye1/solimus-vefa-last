package com.wakana.vefa.config;

import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.services.JWTService;
import com.wakana.vefa.services.SubscriptionService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
/* 
@Component
@RequiredArgsConstructor
public class SubscriptionFilter extends OncePerRequestFilter {

    private final SubscriptionService subscriptionService;
    private final JWTService jwtService;

    // Liste des endpoints protégés nécessitant un abonnement actif
    private static final List<String> protectedEndpoints = List.of(
            "/api/realestate/",
            "/api/reservations/");

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        System.out.println("📌 Requête reçue sur : " + path);
        // Extraction du token JWT depuis l'en-tête Authorization
        String token = jwtService.extractToken(request);

        String profil;
        ProfilEnum profilEnum = null;

        if (token != null && !token.isBlank()) {

            try {
                profil = jwtService.extractProfil(token);
                profilEnum = ProfilEnum.valueOf(profil);

            } catch (Exception e) {
                System.out.println("❌ Erreur lors de l'extraction de l'ID utilisateur : " + e.getMessage());
                // sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token
                // invalide.", null);
                return;
            }

        }

        if (matchesProtectedEndpoint(path) && token != null && !token.isBlank() && profilEnum == ProfilEnum.PROMOTEUR) {
            System.out.println("🔒 Endpoint protégé détecté : " + path);

            if (token == null || token.isBlank()) {
                System.out.println("❌ Aucun token trouvé dans la requête !");
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token manquant ou invalide.", null);
                return;
            }

            System.out.println("🔑 Token extrait : " + token);

            Long userId;
            try {
                userId = jwtService.extractUserId(token);
                System.out.println("👤 ID utilisateur extrait : " + userId);
            } catch (Exception e) {
                System.out.println("❌ Erreur lors de l'extraction de l'ID utilisateur : " + e.getMessage());
                sendJsonError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token invalide.", null);
                return;
            }

            // Vérification de l'abonnement
            if (!subscriptionService.hasActiveSubscription(userId)) {
                System.out.println("🚫 Accès refusé : Abonnement inactif pour l'utilisateur ID " + userId);
                sendJsonError(response, HttpServletResponse.SC_FORBIDDEN,
                        "Votre abonnement a expiré. Veuillez le renouveler.", userId);
                return;
            }

            System.out.println("✅ Abonnement actif, accès autorisé.");
        }

        // Continuer la chaîne de filtres
        filterChain.doFilter(request, response);
    }

  
    private boolean matchesProtectedEndpoint(String requestPath) {
        return protectedEndpoints.stream().anyMatch(requestPath::startsWith);
    }

    private void sendJsonError(HttpServletResponse response, int status, String message, Long userId)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{ \"error\": \"Accès refusé\", \"message\": \"%s\", \"userId\": %s }",
                message,
                userId != null ? userId.toString() : "null");

        response.getWriter().write(jsonResponse);
    }
}*/
