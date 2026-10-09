package com.wakana.vefa.config;




import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.wakana.vefa.services.JWTService;
import com.wakana.vefa.services.impl.UserServiceImpl;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JWTService jwtService;
    private final UserServiceImpl userservice;

    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/v1/auth/",
        "/api/files/",
        "/api/job-requests/",
        "/api/notifications/",
        "/api/rapports/",
        "/api/jobs/",
        "/api/ownerpropertyare/",
        "/api/call-for-charges/",
        "/api/actu/",
        "/api/gare/",
        "/api/otp/",
        "/api/meets/",
        "/api/peytech/",
        "/ipn/",
        "/api/v1/vod/",
        "/chat/",
        "/api/messages/",
        "/api/subscription-plans/",
        "/api/subscriptions/",
        "/api/companies/",
        "/api/indicators/",
        "/api/information-sheets/",
        "/api/progress-album/",
        "/api/realestate/",
        "/api/reservations/",
        "/api/property-types/",
        "/api/v1/notifications/",
        "/api/zone/",
        "/api/fare/",
        "/chat-websocket/",
        "/ws/",
        "/swagger-ui/",
        "/swagger-resources/",
        "/v3/api-docs/",
        "/api/v1/parameters/",
        "/api/v1/user/"
    );

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        String authHeader = request.getHeader("Authorization");
        if (StringUtils.isBlank(authHeader) || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authHeader.substring(7);
        String userEmail;

        try {
            userEmail = jwtService.extractUserName(jwt);
        } catch (Exception e) {
            if (isPublicPath(path)) {
                filterChain.doFilter(request, response);
                return;
            }
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired token");
            return;
        }

        if (StringUtils.isNotBlank(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userservice.userDetailsService().loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authToken);
                SecurityContextHolder.setContext(context);
            } else {
                if (!isPublicPath(path)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid token");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}

/*import java.io.IOException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.wakana.vefa.services.JWTService;
import com.wakana.vefa.services.impl.UserServiceImpl;

import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor

public class JwtAuthenticationFilter  extends OncePerRequestFilter{
    
    
    private final JWTService jwtService;
    private final UserServiceImpl userservice;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
                String authHeader = request.getHeader("Authorization");
                final String jwt;
                final String userEmail;
                

        

                if(StringUtils.isEmpty(authHeader )  ||  !org.apache.commons.lang3.StringUtils.startsWith(authHeader , "Bearer ") || authHeader==null   ){
                    
                    filterChain.doFilter(request, response);
                    return ;
                } 
                jwt = authHeader.substring(7);
                userEmail = jwtService.extractUserName(jwt);
                
                
                if(StringUtils.isNotEmpty(userEmail) && SecurityContextHolder.getContext().getAuthentication() == null){
                    UserDetails  userdetails = userservice.userDetailsService().loadUserByUsername(userEmail);
                    if(jwtService.isTokenValid(jwt, userdetails)){
                        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                            userdetails, null, userdetails.getAuthorities()
                        );
                        token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        securityContext.setAuthentication(token);
                        SecurityContextHolder.setContext(securityContext);
                        
                    }
                }
                
                filterChain.doFilter(request, response);
                
                
    }
    
}
*/