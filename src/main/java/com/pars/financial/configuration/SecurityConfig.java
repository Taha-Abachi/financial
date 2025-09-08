package com.pars.financial.configuration;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import com.pars.financial.repository.UserRepository;
import com.pars.financial.security.ApiKeyAuthFilter;
import com.pars.financial.security.JwtAuthenticationFilter;
import com.pars.financial.security.RateLimitFilter;
import com.pars.financial.security.SecurityContextFilter;
import com.pars.financial.utils.ApiKeyEncryption;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;
    private final ApiKeyEncryption apiKeyEncryption;
    private final RateLimitProperties rateLimitProperties;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    public SecurityConfig(UserRepository userRepository, ApiKeyEncryption apiKeyEncryption, 
                         RateLimitProperties rateLimitProperties, JwtAuthenticationFilter jwtAuthenticationFilter,
                         UserDetailsService userDetailsService, CorsConfigurationSource corsConfigurationSource) {
        this.userRepository = userRepository;
        this.apiKeyEncryption = apiKeyEncryption;
        this.rateLimitProperties = rateLimitProperties;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .addFilterBefore(new SecurityContextFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RateLimitFilter(rateLimitProperties), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new ApiKeyAuthFilter(userRepository, apiKeyEncryption), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        // Swagger UI and OpenAPI endpoints
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // Authentication endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // API endpoints
                        .requestMatchers("/api/v1/customer").hasRole("ADMIN")
                        .requestMatchers("/api/v1/store").hasRole("ADMIN")
                        .requestMatchers("/api/v1/giftcard/issue*").hasRole("ADMIN")
                        .requestMatchers("/api/v1/discountcode/issue*").hasRole("ADMIN")
                        .requestMatchers("/api/v1/discountcode/transaction/list").hasRole("ADMIN")
                        .requestMatchers("/api/v1/discountcode/transaction/").hasAnyRole("ADMIN","API_USER")
                        .requestMatchers("/api/v1/discountcode/*").hasAnyRole("ADMIN","API_USER")
                        .requestMatchers("/api/v1/giftcard/all").hasRole("ADMIN")
                        .requestMatchers("/api/v1/giftcard/transaction").hasRole("API_USER")
                        .requestMatchers("/api/v1/giftcard/*").hasAnyRole("ADMIN","API_USER")
                        .requestMatchers("/api/v1/giftcard/identifier/*").hasAnyRole("ADMIN","API_USER")
                        .requestMatchers("/api/v1/giftcard/transaction/checkStatus/").hasAnyRole("ADMIN", "API_USER")
                        // Item Category endpoints - POST, PUT, DELETE operations restricted to ADMIN only
                        .requestMatchers("/api/v1/item-category/create").hasRole("ADMIN")
                        .requestMatchers("/api/v1/item-category/create-bulk").hasRole("ADMIN")
                        .requestMatchers("/api/v1/item-category/update/*").hasRole("ADMIN")
                        .requestMatchers("/api/v1/item-category/delete/*").hasRole("ADMIN")
                        // Item Category endpoints - GET operations accessible to both ADMIN and API_USER
                        .requestMatchers("/api/v1/item-category/list").hasAnyRole("ADMIN", "API_USER")
                        .requestMatchers("/api/v1/item-category/*").hasAnyRole("ADMIN", "API_USER")
                        // User and UserRole endpoints - restricted to ADMIN and SUPERADMIN
                        .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/user-roles/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        // Batch endpoints - restricted to ADMIN and SUPERADMIN
                        .requestMatchers("/api/v1/batches/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        // Admin endpoints - restricted to ADMIN and SUPERADMIN only
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .anyRequest().permitAll()
                )
                .anonymous(anonymous -> anonymous
                    .authorities(Collections.singletonList(new SimpleGrantedAuthority("ROLE_ANONYMOUS")))
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                    .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';")
                    )
                );
        return http.build();
    }
}