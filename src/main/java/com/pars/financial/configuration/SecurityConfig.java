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
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
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
                .authenticationProvider(authenticationProvider())
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
                        .requestMatchers("/api/v1/customer/list").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/customer").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/store").hasAnyRole("ADMIN", "SUPERADMIN", "COMPANY_USER")
                        .requestMatchers("/api/v1/store/transaction-summary").hasAnyRole("ADMIN", "SUPERADMIN", "COMPANY_USER", "STORE_USER")
                        .requestMatchers("/api/v1/companies").hasAnyRole("ADMIN", "SUPERADMIN", "COMPANY_USER")
                        .requestMatchers("/api/v1/giftcard/issue*").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/discountcode/issue*").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/discountcode/transaction/list").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/discountcode/transaction/").hasAnyRole("ADMIN","API_USER", "SUPERADMIN")
                        .requestMatchers("/api/v1/discountcode/report").hasAnyRole("ADMIN", "API_USER", "SUPERADMIN")
                        .requestMatchers("/api/v1/discountcode/report/company/*").hasAnyRole("ADMIN", "API_USER", "SUPERADMIN", "COMPANY_USER")
                        .requestMatchers("/api/v1/discountcode/personal/*").hasAnyRole("ADMIN", "API_USER", "SUPERADMIN", "COMPANY_USER", "STORE_USER")
                        .requestMatchers("/api/v1/discountcode/*").hasAnyRole("ADMIN","API_USER", "SUPERADMIN", "COMPANY_USER")
                        .requestMatchers("/api/v1/giftcard/all").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/giftcard/company/*").hasAnyRole("ADMIN", "SUPERADMIN", "COMPANY_USER")
                        .requestMatchers("/api/v1/giftcard/transaction").hasAnyRole("API_USER", "SUPERADMIN")
                        .requestMatchers("/api/v1/giftcard/transaction/company/*").hasAnyRole("ADMIN", "API_USER", "SUPERADMIN", "COMPANY_USER")
                        .requestMatchers("/api/v1/giftcard/report").hasAnyRole("ADMIN", "API_USER", "SUPERADMIN")
                        .requestMatchers("/api/v1/giftcard/report/company/*").hasAnyRole("ADMIN", "API_USER", "SUPERADMIN", "COMPANY_USER")
                        .requestMatchers("/api/v1/giftcard/block/*").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/giftcard/unblock/*").hasAnyRole("ADMIN", "SUPERADMIN")
                        // Settlement report endpoints - accessible to COMPANY_USER, ADMIN, and SUPERADMIN
                        .requestMatchers("/api/v1/settlement/report").hasAnyRole("COMPANY_USER", "ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/giftcard/*").hasAnyRole("ADMIN","API_USER", "SUPERADMIN", "COMPANY_USER")
                        .requestMatchers("/api/v1/discountcode/block/*").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/discountcode/unblock/*").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/giftcard/identifier/*").hasAnyRole("ADMIN","API_USER", "SUPERADMIN", "COMPANY_USER")
                        .requestMatchers("/api/v1/giftcard/transaction/checkStatus/").hasAnyRole("ADMIN", "API_USER", "SUPERADMIN")
                        // Item Category endpoints - POST, PUT, DELETE operations restricted to SUPERADMIN only
                        .requestMatchers("/api/v1/item-category/create").hasRole("SUPERADMIN")
                        .requestMatchers("/api/v1/item-category/create-bulk").hasRole("SUPERADMIN")
                        .requestMatchers("/api/v1/item-category/update/*").hasRole("SUPERADMIN")
                        .requestMatchers("/api/v1/item-category/delete/*").hasRole("SUPERADMIN")
                        .requestMatchers("/api/v1/item-category/deactivate/*").hasRole("SUPERADMIN")
                        .requestMatchers("/api/v1/item-category/activate/*").hasRole("SUPERADMIN")
                        .requestMatchers("/api/v1/item-category/restore/*").hasRole("SUPERADMIN")
                        // Item Category endpoints - GET operations accessible to ADMIN, API_USER, COMPANY_USER, and SUPERADMIN
                        .requestMatchers("/api/v1/item-category/list").hasAnyRole("ADMIN", "API_USER", "COMPANY_USER", "SUPERADMIN")
                        .requestMatchers("/api/v1/item-category/inactive").hasAnyRole("ADMIN", "API_USER", "COMPANY_USER", "SUPERADMIN")
                        .requestMatchers("/api/v1/item-category/all-including-inactive").hasAnyRole("ADMIN", "API_USER", "COMPANY_USER", "SUPERADMIN")
                        .requestMatchers("/api/v1/item-category/deleted").hasAnyRole("ADMIN", "API_USER", "COMPANY_USER", "SUPERADMIN")
                        .requestMatchers("/api/v1/item-category/*").hasAnyRole("ADMIN", "API_USER", "COMPANY_USER", "SUPERADMIN")
                        // User and UserRole endpoints - restricted to ADMIN and SUPERADMIN
                        .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        .requestMatchers("/api/v1/user-roles/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        // Batch endpoints - restricted to ADMIN and SUPERADMIN
                        .requestMatchers("/api/v1/batches/**").hasAnyRole("ADMIN", "SUPERADMIN")
                        // Batch report endpoints - accessible to ADMIN, API_USER, COMPANY_USER, and SUPERADMIN
                        .requestMatchers("/api/v1/batches/*/report").hasAnyRole("ADMIN", "API_USER", "COMPANY_USER", "SUPERADMIN")
                        .requestMatchers("/api/v1/batches/*/summary").hasAnyRole("ADMIN", "API_USER", "COMPANY_USER", "SUPERADMIN")
                        .requestMatchers("/api/v1/batches/reports/**").hasAnyRole("ADMIN", "API_USER", "COMPANY_USER", "SUPERADMIN")
                        // Data cleansing endpoints - restricted to SUPERADMIN only
                        .requestMatchers("/api/v1/admin/data-cleansing/**").hasRole("SUPERADMIN")
                        // Other admin endpoints - restricted to ADMIN and SUPERADMIN
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