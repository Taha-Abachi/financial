package com.pars.financial.security;

import com.pars.financial.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        
        // Skip JWT processing if API key authentication is already present
        if (SecurityContextHolder.getContext().getAuthentication() != null && 
            SecurityContextHolder.getContext().getAuthentication() instanceof ApiKeyAuthenticationToken) {
            filterChain.doFilter(request, response);
            return;
        }
        
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            
            // Check if it's a refresh token (should not be used for authentication)
            if (jwtService.isRefreshToken(jwt)) {
                filterChain.doFilter(request, response);
                return;
            }

            username = jwtService.extractUsername(jwt);

            if (username != null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                
                if (jwtService.validateToken(jwt, userDetails)) {
                    String role = jwtService.extractRole(jwt);
                    logger.debug("JWT authentication successful for user: {}, role: {}, authorities: {}", 
                        username, role, userDetails.getAuthorities());
                    
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );
                    
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Authentication set in SecurityContext for user: {}", username);
                } else {
                    logger.warn("JWT token validation failed for user: {}", username);
                }
            }
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token expired: {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":-805,\"message\":\"Token expired\",\"data\":null}");
            return; // Stop filter chain
        } catch (MalformedJwtException e) {
            logger.warn("JWT token malformed: {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":-804,\"message\":\"Invalid token format\",\"data\":null}");
            return; // Stop filter chain
        } catch (SignatureException e) {
            logger.warn("JWT token signature invalid: {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":-804,\"message\":\"Invalid token signature\",\"data\":null}");
            return; // Stop filter chain
        } catch (JwtException e) {
            logger.warn("JWT token invalid: {}", e.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"status\":-804,\"message\":\"Invalid token\",\"data\":null}");
            return; // Stop filter chain
        } catch (Exception e) {
            // Log other exceptions but don't throw them to avoid breaking the filter chain
            logger.error("JWT authentication error: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
