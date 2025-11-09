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
import org.springframework.security.config.Customizer;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtAuthFilter jwtAuthenticationFilter;

    // --- 1. CORS Konfiguracija (OSTALO NETAKNUTO) ---
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String reactDevPort = "http://localhost:5173";

        registry.addMapping("/**")
                .allowedOriginPatterns(reactDevPort)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // --- 2. Security Filter Chain (Pravila) - AŽURIRANO s Driver Dashboard ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((auth) -> auth

                        // ================================================================
                        // A. JAVNO DOSTUPNE RUTE (PermitAll)
                        // ================================================================
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/auth/login", "/auth/register").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/welcome").permitAll()

                        // ================================================================
                        // B. DRIVER RUTE - ✅ NOVO za Driver Dashboard
                        // ================================================================

                        // Driver Dashboard - dohvat svojih Assignment-a
                        .requestMatchers(HttpMethod.GET, "/api/assignments/my-schedule").hasRole("DRIVER")

                        // Driver može vidjeti svoje Assignment-e i Shipment-e (dodatna @PreAuthorize provjera u Controllerima)
                        .requestMatchers(HttpMethod.GET, "/api/assignments/{id}").hasAnyRole("ADMIN", "DISPATCHER", "DRIVER")
                        .requestMatchers(HttpMethod.GET, "/api/shipments/{id}").hasAnyRole("ADMIN", "DISPATCHER", "DRIVER")

                        // Driver - Akcije za Assignment
                        .requestMatchers(HttpMethod.PUT, "/api/assignments/{id}/start").hasRole("DRIVER")
                        .requestMatchers(HttpMethod.PUT, "/api/assignments/{id}/complete").hasRole("DRIVER")

                        // Driver - Akcije za Shipment
                        .requestMatchers(HttpMethod.PUT, "/api/shipments/{id}/start").hasRole("DRIVER")
                        .requestMatchers(HttpMethod.POST, "/api/shipments/{id}/complete").hasRole("DRIVER")
                        .requestMatchers(HttpMethod.PUT, "/api/shipments/{id}/report-issue").hasRole("DRIVER")

                        // Driver - vlastiti profil
                        .requestMatchers(HttpMethod.GET, "/api/drivers/my-info").hasAnyRole("REGISTERED", "DRIVER")
                        .requestMatchers(HttpMethod.PUT, "/api/drivers/{id}").hasAnyRole("REGISTERED", "DRIVER")

                        // ================================================================
                        // C. DISPATCHER RUTE
                        // ================================================================

                        // Liste za kreiranje Assignment-a (potrebne za dropdown-e)
                        .requestMatchers(HttpMethod.GET, "/api/users/drivers").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/**").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.GET, "/api/shipments/**").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.GET, "/api/drivers/**").hasAnyRole("ADMIN", "DISPATCHER")

                        // Assignment CRUD (Dispatcher kreira i upravlja Assignment-ima)
                        .requestMatchers(HttpMethod.GET, "/api/assignments").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.POST, "/api/assignments").hasRole("DISPATCHER")
                        .requestMatchers(HttpMethod.PUT, "/api/assignments/{id}").hasRole("DISPATCHER")

                        // Shipment CRUD (Dispatcher kreira i upravlja Shipment-ima)
                        .requestMatchers(HttpMethod.POST, "/api/shipments").hasRole("DISPATCHER")
                        .requestMatchers(HttpMethod.PUT, "/api/shipments/{id}").hasRole("DISPATCHER")

                        // ================================================================
                        // D. ANALITIKA RUTE
                        // ================================================================
                        .requestMatchers(HttpMethod.GET, "/api/analytics/shipments/average-active-weight").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.POST, "/api/analytics/shipments/mark-overdue").hasRole("ADMIN")

                        // ================================================================
                        // E. ADMIN RUTE (Puna kontrola)
                        // ================================================================

                        // Brisanje (samo Admin)
                        .requestMatchers(HttpMethod.DELETE, "/api/assignments/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/shipments/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/drivers/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/vehicles/**").hasRole("ADMIN")

                        // Ostale Admin rute
                        .requestMatchers("/api/routes/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")
                        .requestMatchers("/auth/update-role/**").hasRole("ADMIN")

                        // ================================================================
                        // F. SVE OSTALO - Zahtijeva autentifikaciju
                        // ================================================================
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(authenticationEntryPoint())
                                .accessDeniedHandler(accessDeniedHandler()))
                .cors(httpSecurityCorsConfigurer -> {});

        return http.build();
    }

    // --- 3. Exception Handlers (OSTALO ISTO) ---

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
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}