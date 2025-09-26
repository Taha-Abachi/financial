package com.pars.financial.security;

import com.pars.financial.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter filter;
    private StringWriter responseWriter;

    @BeforeEach
    void setUp() throws Exception {
        filter = new JwtAuthenticationFilter(jwtService, userDetailsService);
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
    }

    @Test
    void testExpiredJwtToken_ShouldReturn401() throws Exception {
        // Given
        String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJ0ZXN0IiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDB9.expired";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + expiredToken);
        when(jwtService.extractUsername(anyString())).thenThrow(new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired"));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(401);
        verify(response).setContentType("application/json");
        verify(response).getWriter();
        verify(filterChain, never()).doFilter(any(), any());
        
        String responseBody = responseWriter.toString();
        assert responseBody.contains("Token expired");
        assert responseBody.contains("-805");
    }

    @Test
    void testMalformedJwtToken_ShouldReturn401() throws Exception {
        // Given
        String malformedToken = "malformed.token.here";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + malformedToken);
        when(jwtService.extractUsername(anyString())).thenThrow(new io.jsonwebtoken.MalformedJwtException("Malformed JWT"));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).setStatus(401);
        verify(response).setContentType("application/json");
        verify(response).getWriter();
        verify(filterChain, never()).doFilter(any(), any());
        
        String responseBody = responseWriter.toString();
        assert responseBody.contains("Invalid token format");
        assert responseBody.contains("-804");
    }
}
