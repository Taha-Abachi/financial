package com.pars.financial.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.pars.financial.repository.ApiUserRepository;
import com.pars.financial.security.ApiKeyAuthFilter;
import com.pars.financial.security.RateLimitFilter;
import com.pars.financial.utils.ApiKeyEncryption;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final ApiUserRepository apiUserRepository;
    private final ApiKeyEncryption apiKeyEncryption;

    public SecurityConfig(ApiUserRepository apiUserRepository, ApiKeyEncryption apiKeyEncryption) {
        this.apiUserRepository = apiUserRepository;
        this.apiKeyEncryption = apiKeyEncryption;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .addFilterBefore(new RateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(new ApiKeyAuthFilter(apiUserRepository, apiKeyEncryption), UsernamePasswordAuthenticationFilter.class)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/api/v1/customer").hasRole("ADMIN")
                        .requestMatchers("/api/v1/store").hasRole("ADMIN")
                        .requestMatchers("/api/v1/giftcard/issue*").hasRole("ADMIN")
                        .requestMatchers("/api/v1/discountcode/issue*").hasRole("ADMIN")
                        .requestMatchers("/api/v1/discountcode/transaction/").hasRole("API_USER")
                        .requestMatchers("/api/v1/discountcode/*").hasAnyRole("ADMIN","API_USER")
                        .requestMatchers("/api/v1/giftcard/all").hasRole("ADMIN")
                        .requestMatchers("/api/v1/giftcard/transaction").hasRole("API_USER")
                        .requestMatchers("/api/v1/giftcard/*").hasRole("API_USER")
                        .requestMatchers("/api/v1/giftcard/transaction/checkStatus/").hasAnyRole("ADMIN", "API_USER")
                        .anyRequest().permitAll()
                )
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers
                    .contentSecurityPolicy(csp -> csp
                        .policyDirectives("default-src 'self'")
                    )
                );
        return http.build();
    }
}