package com.pars.financial.configuration;

import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.pars.financial.config.RateLimitProperties;
import com.pars.financial.repository.ApiUserRepository;
import com.pars.financial.security.ApiKeyAuthFilter;
import com.pars.financial.security.RateLimitFilter;
import com.pars.financial.security.SecurityContextFilter;
import com.pars.financial.utils.ApiKeyEncryption;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApiUserRepository apiUserRepository;
    private final ApiKeyEncryption apiKeyEncryption;
    private final RateLimitProperties rateLimitProperties;

    public SecurityConfig(ApiUserRepository apiUserRepository, ApiKeyEncryption apiKeyEncryption, RateLimitProperties rateLimitProperties) {
        this.apiUserRepository = apiUserRepository;
        this.apiKeyEncryption = apiKeyEncryption;
        this.rateLimitProperties = rateLimitProperties;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(new SecurityContextFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new RateLimitFilter(rateLimitProperties), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new ApiKeyAuthFilter(apiUserRepository, apiKeyEncryption), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        // Swagger UI and OpenAPI endpoints
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                        // API endpoints
                        .requestMatchers("/api/v1/customer").hasRole("ADMIN")
                        .requestMatchers("/api/v1/store").hasRole("ADMIN")
                        .requestMatchers("/api/v1/giftcard/issue*").hasRole("ADMIN")
                        .requestMatchers("/api/v1/discountcode/issue*").hasRole("ADMIN")
                        .requestMatchers("/api/v1/discountcode/transaction/").hasRole("API_USER")
                        .requestMatchers("/api/v1/discountcode/*").hasAnyRole("ADMIN","API_USER")
                        .requestMatchers("/api/v1/giftcard/all").hasRole("ADMIN")
                        .requestMatchers("/api/v1/giftcard/transaction").hasRole("API_USER")
                        .requestMatchers("/api/v1/giftcard/*").hasRole("API_USER")
                        .requestMatchers("/api/v1/giftcard/identifier/*").hasRole("API_USER")
                        .requestMatchers("/api/v1/giftcard/transaction/checkStatus/").hasAnyRole("ADMIN", "API_USER")
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