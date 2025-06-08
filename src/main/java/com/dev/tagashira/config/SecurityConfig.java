package com.dev.tagashira.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.http.SessionCreationPolicy;




@Configuration
@EnableMethodSecurity(securedEnabled = true)
@EnableWebSecurity
public class SecurityConfig{

    //Config encode password
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomAuthenticationEntryPoint customAuthenticationEntryPoint) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()) // Enable CORS with default configuration

                .formLogin(AbstractHttpConfigurer::disable)


                .headers(headers -> headers
                        .frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)
                )


                .authorizeHttpRequests(authz -> authz
                        // Allow OPTIONS requests for CORS preflight
                        .requestMatchers("OPTIONS", "/**").permitAll()
                        
                        // Public endpoints
                        .requestMatchers("/",
                                "/api/v1/auth/login",
                                "/api/v1/auth/refresh",
                                "/h2-console/**",
                                "/api/v1/users/register",
                                "/users/auth/social-login",
                                "/users/auth/social-login/**",
                                "/users/auth/social/callback",
                                "/users/login",
                                "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        
                        // User management - MANAGER only (except register which is public)
                        .requestMatchers("GET", "/api/v1/users").hasRole("MANAGER")
                        .requestMatchers("POST", "/api/v1/users").hasRole("MANAGER")
                        .requestMatchers("GET", "/api/v1/users/{id}").hasRole("MANAGER")
                        .requestMatchers("PUT", "/api/v1/users/{id}").hasRole("MANAGER")
                        .requestMatchers("DELETE", "/api/v1/users/{id}").hasRole("MANAGER")
                        .requestMatchers("/api/v1/users/{id}/roles/**").hasRole("MANAGER")
                        
                        // Role management - MANAGER only
                        .requestMatchers("/api/v1/roles").hasRole("MANAGER")
                        .requestMatchers("/api/v1/roles/**").hasRole("MANAGER")
                        
                        // Fee management - ACCOUNTANT can modify, both can read
                        .requestMatchers("GET", "/api/v1/fees").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("GET", "/api/v1/fees/**").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("POST", "/api/v1/fees").hasRole("ACCOUNTANT")
                        .requestMatchers("PUT", "/api/v1/fees/**").hasRole("ACCOUNTANT")
                        .requestMatchers("DELETE", "/api/v1/fees/**").hasRole("ACCOUNTANT")
                        
                        // Payment records - ACCOUNTANT can modify, both can read
                        .requestMatchers("GET", "/api/v1/payment-records").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("GET", "/api/v1/payment-records/**").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("POST", "/api/v1/payment-records").hasRole("ACCOUNTANT")
                        .requestMatchers("PUT", "/api/v1/payment-records/**").hasRole("ACCOUNTANT")
                        .requestMatchers("DELETE", "/api/v1/payment-records/**").hasRole("ACCOUNTANT")
                        
                        // Apartment management - ACCOUNTANT can read, MANAGER has full access
                        .requestMatchers("GET", "/api/v1/apartments").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("GET", "/api/v1/apartments/**").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("POST", "/api/v1/apartments").hasRole("MANAGER")
                        .requestMatchers("PUT", "/api/v1/apartments/**").hasRole("MANAGER")
                        .requestMatchers("DELETE", "/api/v1/apartments/**").hasRole("MANAGER")
                        
                        // Resident management - ACCOUNTANT can read, MANAGER has full access
                        .requestMatchers("GET", "/api/v1/residents").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("GET", "/api/v1/residents/**").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("POST", "/api/v1/residents").hasRole("MANAGER")
                        .requestMatchers("PUT", "/api/v1/residents/**").hasRole("MANAGER")
                        .requestMatchers("DELETE", "/api/v1/residents/**").hasRole("MANAGER")
                        
                        // Vehicle management - MANAGER for CRUD, both for count queries
                        .requestMatchers("GET", "/api/v1/vehicles/**").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("/api/v1/vehicles").hasRole("MANAGER")
                        .requestMatchers("/api/v1/vehicles/**").hasRole("MANAGER")
                        
                        // Utility bill management - ACCOUNTANT can modify, both can read
                        .requestMatchers("GET", "/api/v1/utilitybills").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("GET", "/api/v1/utilitybills/**").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("POST", "/api/v1/utilitybills/**").hasRole("ACCOUNTANT")
                        .requestMatchers("PUT", "/api/v1/utilitybills/**").hasRole("ACCOUNTANT")
                        .requestMatchers("DELETE", "/api/v1/utilitybills/**").hasRole("ACCOUNTANT")
                        
                        // Floor area fee config management - ACCOUNTANT can modify, both can read
                        .requestMatchers("GET", "/api/v1/floor-area-fee-configs").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("GET", "/api/v1/floor-area-fee-configs/**").hasAnyRole("ACCOUNTANT", "MANAGER")
                        .requestMatchers("POST", "/api/v1/floor-area-fee-configs").hasRole("ACCOUNTANT")
                        .requestMatchers("PUT", "/api/v1/floor-area-fee-configs/**").hasRole("ACCOUNTANT")
                        .requestMatchers("DELETE", "/api/v1/floor-area-fee-configs/**").hasRole("ACCOUNTANT")
                        
                        // All other requests need authentication
                        .anyRequest().authenticated()
                )

                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults())//Config OAuth2
                        .authenticationEntryPoint(customAuthenticationEntryPoint))//Handle exception in Filter layer

                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

}
