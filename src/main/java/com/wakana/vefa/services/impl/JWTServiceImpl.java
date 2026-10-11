package com.wakana.vefa.services.impl;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.wakana.vefa.enums.ProfilEnum;
import com.wakana.vefa.services.JWTService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class JWTServiceImpl implements JWTService {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";
    private static final long ACCESS_TOKEN_DURATION_MS = 15 * 60 * 1000L;
    private static final long REFRESH_TOKEN_DURATION_MS = 7 * 24 * 60 * 60 * 1000L;

    public String generateToken(UserDetails userDetails, Long userId, ProfilEnum profil) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("profil", profil);
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_DURATION_MS))
                .signWith(getSiginKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Map<String, Object> extraClaims, UserDetails userDetails, Long userId,
            ProfilEnum profil) {
        if (extraClaims == null) {
            extraClaims = new HashMap<>();
        }

        extraClaims.put("userId", userId);
        extraClaims.put("profil", profil);
        extraClaims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_DURATION_MS))
                .signWith(getSiginKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUserName(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsRosolvers) {
        final Claims claims = extractAllClaims(token);
        return claimsRosolvers.apply(claims);

    }

    private Key getSiginKey() {
        byte[] key = Decoders.BASE64.decode("4f1feeca525de4cdb064656007da3edac7895a87ff0ea865693300fb8b6e8f9c");
        return Keys.hmacShaKeyFor(key);

    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(getSiginKey()).build().parseClaimsJws(token).getBody();

    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)
            && ACCESS_TOKEN.equals(extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class))));
        }

        @Override
        public boolean isRefreshTokenValid(String token, UserDetails userDetails) {
        final String username = extractUserName(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token)
            && REFRESH_TOKEN.equals(extractClaim(token, claims -> claims.get(TOKEN_TYPE_CLAIM, String.class))));
    }

    private boolean isTokenExpired(String token) {

        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // ✅ **Nouvelle méthode pour extraire l'ID utilisateur depuis le token**

    @Override
    public String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Supprime "Bearer " pour ne garder que le token
        }
        return null;
    }

    public Long extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSiginKey()) // ✅ Utilisation correcte de la clé secrète
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("🔍 Contenu du token : " + claims);

            Object userIdObj = claims.get("userId"); // Assurez-vous que l'ID est bien stocké sous "userId"
            if (userIdObj == null) {
                System.out.println("❌ Aucune clé 'userId' trouvée dans le token !");
                return null;
            }

            return Long.valueOf(userIdObj.toString()); // Conversion sûre
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du parsing du token : " + e.getMessage());
            return null;
        }
    }
    
    
    
    public String extractProfil(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSiginKey()) // ✅ Utilisation correcte de la clé secrète
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("🔍 Contenu du token : " + claims);

            Object profilObj = claims.get("profil"); // Assurez-vous que l'ID est bien stocké sous "userId"
            if (profilObj  == null) {
                System.out.println("❌ Aucune clé 'profil' trouvée dans le token !");
                return null;
            }

            return profilObj.toString(); // Conversion sûre
        } catch (Exception e) {
            System.out.println("❌ Erreur lors du parsing du token : " + e.getMessage());
            return null;
        }
    }

}
