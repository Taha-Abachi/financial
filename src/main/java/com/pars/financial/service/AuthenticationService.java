package com.pars.financial.service;

import com.pars.financial.configuration.JwtProperties;
import com.pars.financial.dto.LoginRequest;
import com.pars.financial.dto.LoginResponse;
import com.pars.financial.dto.RefreshTokenRequest;
import com.pars.financial.dto.RefreshTokenResponse;
import com.pars.financial.entity.RefreshToken;
import com.pars.financial.entity.User;
import com.pars.financial.exception.GenericException;
import com.pars.financial.repository.RefreshTokenRepository;
import com.pars.financial.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthenticationService {
    
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtProperties jwtProperties;

    public AuthenticationService(AuthenticationManager authenticationManager,
                               JwtService jwtService,
                               RefreshTokenRepository refreshTokenRepository,
                               UserRepository userRepository,
                               JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
        this.jwtProperties = jwtProperties;
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest, String loginIp, String userAgent, String referrer) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            if (!user.isActive()) {
                throw new GenericException("User account is deactivated");
            }

            // Generate tokens
            String accessToken = jwtService.generateAccessToken(user, loginIp, userAgent, referrer);
            String refreshTokenValue = generateRefreshTokenValue();
            
            // Save refresh token to database
            LocalDateTime refreshTokenExpiry = LocalDateTime.now()
                .plusDays(jwtProperties.getRefreshTokenExpirationDays());
            
            RefreshToken refreshToken = new RefreshToken(refreshTokenValue, user, refreshTokenExpiry, loginIp, userAgent, referrer);
            refreshTokenRepository.save(refreshToken);

            return new LoginResponse(
                accessToken,
                refreshTokenValue,
                jwtService.getTokenExpirationTime(accessToken),
                loginIp,
                userAgent,
                referrer
            );

        } catch (Exception e) {
            throw new GenericException("Authentication failed: " + e.getMessage());
        }
    }

    @Transactional
    public RefreshTokenResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        try {
            // Validate refresh token from JWT
            if (!jwtService.isRefreshToken(refreshTokenRequest.getRefreshToken())) {
                throw new GenericException("Invalid refresh token");
            }

            String username = jwtService.extractUsername(refreshTokenRequest.getRefreshToken());
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            // Check if refresh token exists in database and is not revoked
            RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshTokenRequest.getRefreshToken())
                .orElseThrow(() -> new GenericException("Refresh token not found"));

            if (storedRefreshToken.isRevoked()) {
                throw new GenericException("Refresh token has been revoked");
            }

            if (storedRefreshToken.isExpired()) {
                storedRefreshToken.revoke("Token expired");
                refreshTokenRepository.save(storedRefreshToken);
                throw new GenericException("Refresh token has expired");
            }

            // Generate new access token
            String newAccessToken = jwtService.generateAccessToken(
                user,
                storedRefreshToken.getLoginIp(),
                storedRefreshToken.getUserAgent(),
                storedRefreshToken.getReferrer()
            );

            // Generate new refresh token
            String newRefreshTokenValue = generateRefreshTokenValue();
            LocalDateTime newRefreshTokenExpiry = LocalDateTime.now()
                .plusDays(jwtProperties.getRefreshTokenExpirationDays());

            RefreshToken newRefreshToken = new RefreshToken(
                newRefreshTokenValue,
                user,
                newRefreshTokenExpiry,
                storedRefreshToken.getLoginIp(),
                storedRefreshToken.getUserAgent(),
                storedRefreshToken.getReferrer()
            );

            // Revoke old refresh token
            storedRefreshToken.revoke("Replaced by new refresh token");
            refreshTokenRepository.save(storedRefreshToken);

            // Save new refresh token
            refreshTokenRepository.save(newRefreshToken);

            return new RefreshTokenResponse(
                newAccessToken,
                newRefreshTokenValue,
                jwtService.getTokenExpirationTime(newAccessToken)
            );

        } catch (Exception e) {
            throw new GenericException("Token refresh failed: " + e.getMessage());
        }
    }

    @Transactional
    public void logout(String refreshToken) {
        try {
            RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new GenericException("Refresh token not found"));

            storedRefreshToken.revoke("User logout");
            refreshTokenRepository.save(storedRefreshToken);

        } catch (Exception e) {
            throw new GenericException("Logout failed: " + e.getMessage());
        }
    }

    @Transactional
    public void logoutAllSessions(String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            refreshTokenRepository.revokeAllUserTokens(user, LocalDateTime.now(), "All sessions revoked");

        } catch (Exception e) {
            throw new GenericException("Logout all sessions failed: " + e.getMessage());
        }
    }

    private String generateRefreshTokenValue() {
        return UUID.randomUUID().toString();
    }
}
