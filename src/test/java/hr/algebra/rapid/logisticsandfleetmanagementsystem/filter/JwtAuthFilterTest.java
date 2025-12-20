package hr.algebra.rapid.logisticsandfleetmanagementsystem.filter;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock private JwtService jwtService;
    @Mock private UserDetailsServiceImpl userDetailsServiceImpl;
    @Mock private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("doFilter - bez Authorization headera (nastavlja chain)")
    void doFilter_NoHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("doFilter - ispravan Bearer token (autentificira korisnika)")
    void doFilter_ValidToken() throws Exception {
        String token = "valid.jwt.token";
        String username = "testUser";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        UserDetails userDetails = new User(username, "", Collections.emptyList());

        when(jwtService.extractUsername(token)).thenReturn(username);
        when(userDetailsServiceImpl.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.validateToken(token, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo(username);
    }

    @Test
    @DisplayName("doFilter - istekao token (vraća 401)")
    void doFilter_ExpiredToken() throws Exception {
        // 1. Priprema podataka
        String token = "expired.jwt.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // 2. Kreiranje Claims objekta koji NIJE null (ovo rješava tvoj NPE)
        // Koristimo Jwts.claims() da bismo dobili builder/mapu
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.claims()
                .subject("testUser")
                .expiration(new java.util.Date(System.currentTimeMillis() - 1000)) // Datum u prošlosti
                .build();

        // 3. Kreiranje ExpiredJwtException s tim claimsima
        ExpiredJwtException expiredException = new ExpiredJwtException(
                null,
                claims,
                "Token expired"
        );

        // 4. Simulacija bacanja iznimke
        when(jwtService.extractUsername(token)).thenThrow(expiredException);

        // 5. Izvršavanje
        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        // 6. Provjera
        assertThat(response.getStatus()).isEqualTo(401);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("doFilter - nevažeći token format (JwtException)")
    void doFilter_InvalidToken() throws Exception {
        String token = "malformed.token";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token);
        MockHttpServletResponse response = new MockHttpServletResponse();

        // Simuliramo općenitu JwtException (npr. krivi potpis)
        when(jwtService.extractUsername(token)).thenThrow(new io.jsonwebtoken.JwtException("Invalid signature"));

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        assertThat(response.getStatus()).isEqualTo(401);
        verify(filterChain).doFilter(request, response);
    }
}
