package com.rfidcampus.rfid_campus.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter,
                          CustomUserDetailsService userDetailsService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.userDetailsService = userDetailsService;
    }

    // ✅ CADENA 1: Solo asistencia RFID
    @Bean
    @Order(1)
    public SecurityFilterChain attendanceChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/asistencia/**")
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((req, res, exc) -> res.setStatus(HttpServletResponse.SC_OK))
            )
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()   // ✅ NECESARIO
                .anyRequest().permitAll()
            );
        return http.build();
    }

    // ✅ CADENA 2: Sistema normal con JWT
    @Bean
    @Order(2)
    public SecurityFilterChain appChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex
                .accessDeniedHandler((req, res, exc) -> res.setStatus(HttpServletResponse.SC_FORBIDDEN))
            )
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()            // ✅ NECESARIO TAMBIÉN AQUÍ
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/tarjetas/**").permitAll()
                .requestMatchers("/api/bar/**").permitAll()
                .requestMatchers("/api/biblioteca/**").permitAll()
                // asistencia ya está en chain 1
                .anyRequest().authenticated()
            )
            .authenticationProvider(daoAuthProvider())
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider daoAuthProvider() {
        DaoAuthenticationProvider prov = new DaoAuthenticationProvider();
        prov.setUserDetailsService(userDetailsService);
        prov.setPasswordEncoder(passwordEncoder());
        return prov;
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
