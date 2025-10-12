package hr.algebra.rapid.logisticsandfleetmanagementsystem.configuration;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.filter.JwtAuthFilter;
import org.springframework.security.authentication.AuthenticationManager;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import jakarta.servlet.http.HttpServletResponse;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor // Zamjena za @AllArgsConstructor za final polja
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

    // Morate osigurati da je ova klasa kreirana i da radi JWT validaciju
    private final JwtAuthFilter jwtAuthenticationFilter;

    // --- 1. CORS Konfiguracija (Za React Frontend) ---
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Postavite port na kojem se pokreće vaš React frontend (Vite = 5173, CRA = 3000)
        String reactDevPort = "http://localhost:5173";

        registry.addMapping("/**")
                .allowedOrigins(reactDevPort)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // --- 2. Security Filter Chain (Pravila) ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((auth) -> auth
                        // A. Javno Dostupne Rute (PermitAll)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/auth/login", "/auth/register").permitAll() // Login i registracija
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll() // Javne rute za pregled
                        .requestMatchers(HttpMethod.GET, "/api/welcome").permitAll() // Vaš test endpoint

                        // B. Rute za Korisnike (REGISTERED)
                        .requestMatchers(HttpMethod.GET, "/api/v1/drivers/my-info").hasAnyRole("REGISTERED", "DRIVER")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/drivers/{id}").hasAnyRole("REGISTERED", "DRIVER") // Ažuriranje vlastitih podataka

                        // C. Rute za Administratora (ADMIN)
                        .requestMatchers("/api/v1/vehicles/**").hasRole("ADMIN")       // Upravljanje vozilima
                        .requestMatchers("/api/v1/routes/**").hasRole("ADMIN")         // Upravljanje rutama
                        .requestMatchers("/api/v1/drivers/**").hasRole("ADMIN")        // Upravljanje vozačima (svim)
                        .requestMatchers("/api/v1/reports/**").hasRole("ADMIN")        // Financijski izvještaji
                        .requestMatchers("/auth/update-role/**").hasRole("ADMIN")      // Admin rute za sigurnost

                        // D. Sve ostalo zahtijeva autentifikaciju
                        .anyRequest().authenticated()
                )
                // Dodajemo naš custom JWT filter PRIJE standardnog Spring Security filtera
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(authenticationEntryPoint())
                                .accessDeniedHandler(accessDeniedHandler()))
                .cors(httpSecurityCorsConfigurer -> {}); // Koristimo globalnu CORS konfiguraciju iz addCorsMappings

        return http.build();
    }

    // --- 3. Exception Handlers ---

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            System.out.println("NEAUTENTIFICIRAN zahtjev na: " + request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Korisnik nije autentificiran ili token nije ispravan.");
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            System.out.println("ZABRANJEN pristup na: " + request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Korisnik nema potrebnu ulogu (role) za pristup ovom resursu.");
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        // Spring Boot automatski konfigurira AuthenticationConfiguration
        // koja se koristi za dobivanje AuthenticationManagera
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // Vratite BCryptPasswordEncoder jer su vaše lozinke heširane BCryptom ($2a$10...)
        return new BCryptPasswordEncoder();
    }
}
