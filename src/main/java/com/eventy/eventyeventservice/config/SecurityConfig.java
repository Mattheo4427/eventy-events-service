package com.eventy.eventyeventservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${keycloak.server-url}")
    private String keycloakServerUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 1. Documentation & Monitoring (Public)
                        .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 2. Lecture publique du catalogue (Tout le monde peut voir les événements)
                        .requestMatchers(HttpMethod.GET, "/events/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/event-categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/event-types/**").permitAll()

                        // 3. Gestion (Admin uniquement)
                        // Création, Modification, Suppression, Changement de statut
                        .requestMatchers(HttpMethod.POST, "/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/events/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/events/**").hasRole("ADMIN")

                        // 4. Par défaut, tout le reste est authentifié
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                                .decoder(jwtDecoder())
                        )
                );

        return http.build();
    }

    /**
     * Décodeur JWT "Docker-friendly" (Ignore l'Issuer Mismatch)
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        String jwkSetUri = String.format("%s/realms/%s/protocol/openid-connect/certs", keycloakServerUrl, realm);
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * Convertisseur de rôles (Lit 'realm_access' et 'app_role')
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);

            // Rôles standards Keycloak
            if (jwt.hasClaim("realm_access")) {
                Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
                if (realmAccess.get("roles") instanceof Collection<?> roles) {
                    authorities.addAll(roles.stream()
                            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toString().toUpperCase()))
                            .collect(Collectors.toList()));
                }
            }

            // Attribut personnalisé 'app_role'
            if (jwt.hasClaim("app_role")) {
                String appRole = jwt.getClaimAsString("app_role");
                if (appRole != null && !appRole.trim().isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + appRole.toUpperCase()));
                }
            }
            return authorities;
        });
        return converter;
    }
}