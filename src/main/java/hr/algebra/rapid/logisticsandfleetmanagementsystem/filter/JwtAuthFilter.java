package hr.algebra.rapid.logisticsandfleetmanagementsystem.filter;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserDetailsServiceImpl;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(JwtAuthFilter.class);
    private static final String BEARER_PREFIX = "Bearer ";
    private static final int BEARER_PREFIX_LENGTH = 7;

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {
            String username = extractUsernameFromToken(token, request, response, filterChain);

            if (username == null) {
                return; // Token greška - već handleano u extractUsernameFromToken()
            }

            authenticateUser(username, token, request);
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX_LENGTH);
        }

        return null;
    }

    private String extractUsernameFromToken(
            String token,
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String username = jwtService.extractUsername(token);
            LOG.debug("Successfully extracted username from JWT token: {}", username);
            return username;

        } catch (ExpiredJwtException e) {
            handleExpiredToken(request, e);
            sendUnauthorizedResponse(response, filterChain, request);
            return null;

        } catch (JwtException | IllegalArgumentException e) {
            handleInvalidToken(request, e);
            sendUnauthorizedResponse(response, filterChain, request);
            return null;
        }
    }

    private void handleExpiredToken(HttpServletRequest request, ExpiredJwtException e) {
        LOG.warn("JWT token expired for request [{}]. Token expired at: {}.",
                request.getRequestURI(),
                e.getClaims().getExpiration());
    }

    private void handleInvalidToken(HttpServletRequest request, Exception e) {
        LOG.error("Invalid JWT token for request [{}]. Error: {}",
                request.getRequestURI(),
                e.getMessage());
    }

    private void sendUnauthorizedResponse(
            HttpServletResponse response,
            FilterChain filterChain,
            HttpServletRequest request
    ) throws ServletException, IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        filterChain.doFilter(request, response);
    }

    private void authenticateUser(String username, String token, HttpServletRequest request) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return; // Već autentificiran
        }

        try {
            UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(username);

            if (Boolean.TRUE.equals(jwtService.validateToken(token, userDetails))) {
                setAuthenticationContext(userDetails, request);
                LOG.debug("Successfully authenticated user [{}] for request [{}]", username, request.getRequestURI());
            } else {
                LOG.warn("JWT token validation failed for user [{}] on request [{}]", username, request.getRequestURI());
            }

        } catch (Exception e) {
            LOG.error("Error during authentication for user [{}]. Error: {}", username, e.getMessage());
        }
    }

    private void setAuthenticationContext(UserDetails userDetails, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
        authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }
}