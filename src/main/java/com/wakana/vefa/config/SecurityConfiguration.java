package com.wakana.vefa.config;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.wakana.vefa.services.UserSeervice;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserSeervice userService;
    // private final SubscriptionFilter subscriptionFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/v1/auth/**",
                                "/api/files/**",
                                "/api/job-requests/**",
                                "/api/notifications/**",
                                "/api/rental-invoices/**",
                                "/api/rapports/**",
                                "/api/works/**",
                                "/api/payments-historics/**",
                                "/api/rental-contracts/**",

                                "/api/jobs/**",
                                "/api/ownerpropertyare/**",
                                "/api/call-for-charges/**",
                                "/api/actu/**",
                                "/api/gare/**",
                                "/api/otp/**",
                                "/api/meets/**",
                                "/api/peytech/**",
                                "/ipn/**",
                                "/api/v1/vod/**",
                                "/chat/**",

                                "/api/files/**",

                                "/api/docs/**",
                                "/api/otp/**",
                                "/api/dashboard/commercial/**",
                                "/api/dashboard/financier/**",
                                //"/api/properties-v2/**",

                                "/api/messages/**",
                                "/api/subscriptions-plans/**",
                                "/api/subscriptions/**",
                                "/api/user-subscriptions/**",
                                "/api/companies/**",
                                "/api/indicators/**",
                                "/api/information-sheets/**",
                                "/api/progress-album/**",
                                "/api/payment-calls/**",
                                "/api/subscription-plan-types/**",
                                "/api/realestate/**",
                                "/api/reservations/**",
                                "/api/property-types/**",
                                "/api/v1/notifications/**",
                                "/api/notifications/**",
                                "/api/zone/**",
                                "/api/fare/**",
                                "/chat-websocket/**",
                                "/ws/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",

                                "/api/v1/parameters/**",
                                "/api/v1/user/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                // .addFilterBefore(subscriptionFilter,
                // UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userService.userDetailsService());
        authenticationProvider.setPasswordEncoder(passwordEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
