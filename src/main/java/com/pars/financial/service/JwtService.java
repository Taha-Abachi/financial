package com.pars.financial.service;

import com.pars.financial.configuration.JwtProperties;
import com.pars.financial.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtService {
    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user, String loginIp, String userAgent, String referrer) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().getName());
        claims.put("loginIp", loginIp);
        claims.put("userAgent", userAgent);
        claims.put("referrer", referrer);
        claims.put("loginTime", LocalDateTime.now().toString());
        
        return createToken(claims, user.getUsername(), jwtProperties.getAccessTokenExpirationMinutes());
    }

    public String generateRefreshToken(User user, String loginIp, String userAgent, String referrer) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("role", user.getRole().getName());
        claims.put("loginIp", loginIp);
        claims.put("userAgent", userAgent);
        claims.put("referrer", referrer);
        claims.put("loginTime", LocalDateTime.now().toString());
        claims.put("tokenType", "refresh");
        
        return createToken(claims, user.getUsername(), jwtProperties.getRefreshTokenExpirationDays() * 24 * 60);
    }

    private String createToken(Map<String, Object> claims, String subject, long expirationMinutes) {
        Date now = new Date();
        Date expiration = Date.from(
            LocalDateTime.now()
                .plusMinutes(expirationMinutes)
                .atZone(ZoneId.systemDefault())
                .toInstant()
        );

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuer(jwtProperties.getIssuer())
                .setAudience(jwtProperties.getAudience())
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    public String extractLoginIp(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("loginIp", String.class);
    }

    public String extractUserAgent(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userAgent", String.class);
    }

    public String extractReferrer(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("referrer", String.class);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    public Boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return "refresh".equals(claims.get("tokenType", String.class));
        } catch (Exception e) {
            return false;
        }
    }

    public LocalDateTime getTokenExpirationTime(String token) {
        Date expiration = extractExpiration(token);
        return expiration.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
