package com.server.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuración de seguridad temporal para permitir acceso a Swagger.
 * Esta configuración será reemplazada cuando implementemos JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(authz -> authz
                // Permitir acceso público a Swagger/OpenAPI
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**",
                    "/api-docs/**"
                ).permitAll()
                // Permitir acceso público a la API de secciones por ahora
                .requestMatchers("/api/secciones/**").permitAll()
                // Permitir acceso a actuator para health checks
                .requestMatchers("/actuator/**").permitAll()
                // Cualquier otra petición requiere autenticación
                .anyRequest().authenticated()
            )
            .csrf(csrf -> csrf.disable()) // Desactivar CSRF para facilitar las pruebas
            .httpBasic(httpBasic -> {}); // Mantener autenticación básica para otras rutas

        return http.build();
    }
}
