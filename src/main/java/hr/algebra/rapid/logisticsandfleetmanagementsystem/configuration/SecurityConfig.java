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
                .allowedOrigins(reactDevPort)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }

    // --- 2. Security Filter Chain (Pravila) - Korigirano ---
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((auth) -> auth

                        // A. Javno Dostupne Rute (PermitAll) - ZADRŽANO DA BI LOGIN RADIO!
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll() // CORS preflight
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/auth/login", "/auth/register").permitAll() // Login i registracija
                        .requestMatchers(HttpMethod.GET, "/api/v1/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/welcome").permitAll()

                        // B. Rute za KORISNIKE/VOZAČE - ISPRAVLJENE PUTANJE
                        .requestMatchers(HttpMethod.GET, "/api/drivers/my-info").hasAnyRole("REGISTERED", "DRIVER") // Uklonjen v1
                        .requestMatchers(HttpMethod.PUT, "/api/drivers/{id}").hasAnyRole("REGISTERED", "DRIVER") // Uklonjen v1

                        // C. DODANE RUTE ZA DISPATCHER I ADMINISTRATORA

                        // 1. DISPATCHER: Potrebne liste za Assignment Form (GET)
                        // /api/users/drivers je KRITIČNA putanja za frontend
                        .requestMatchers(HttpMethod.GET, "/api/users/drivers").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.GET, "/api/vehicles/**").hasAnyRole("ADMIN", "DISPATCHER") // Čitanje vozila
                        .requestMatchers(HttpMethod.GET, "/api/shipments/**").hasAnyRole("ADMIN", "DISPATCHER") // Čitanje pošiljaka
                        .requestMatchers(HttpMethod.GET, "/api/drivers/**").hasAnyRole("ADMIN", "DISPATCHER") // Čitanje svih Driver profila

                        // 2. DISPATCHER/ADMIN CRUD
                        .requestMatchers("/api/assignments/**").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.POST, "/api/shipments").hasAnyRole("ADMIN", "DISPATCHER")
                        .requestMatchers(HttpMethod.PUT, "/api/shipments/**").hasAnyRole("ADMIN", "DISPATCHER")

                        // 3. ADMIN Rute (Puna kontrola) - ISPRAVLJENE PUTANJE
                        // Sve što nije izričito dozvoljeno DISPATCHER-u ili DRIVER-u spada ovdje (npr. DELETE, update role, routes, reports)
                        .requestMatchers(HttpMethod.DELETE, "/api/shipments/**").hasRole("ADMIN")
                        .requestMatchers("/api/vehicles/**").hasRole("ADMIN") // Općenita PUT/POST/DELETE vozila
                        .requestMatchers("/api/routes/**").hasRole("ADMIN")
                        .requestMatchers("/api/drivers/**").hasRole("ADMIN")
                        .requestMatchers("/api/reports/**").hasRole("ADMIN")
                        .requestMatchers("/auth/update-role/**").hasRole("ADMIN")

                        // D. Sve ostalo zahtijeva autentifikaciju
                        .anyRequest().authenticated()
                )
                // Dodajemo naš custom JWT filter PRIJE standardnog Spring Security filtera
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(authenticationEntryPoint())
                                .accessDeniedHandler(accessDeniedHandler()))
                // KLJUČNO: Ostavljeno kao u Vašem originalu da bi CORS radio
                .cors(httpSecurityCorsConfigurer -> {});

        return http.build();
    }

    // --- 3. Exception Handlers, AuthenticationManager, PasswordEncoder ostaju isti ---
    // ...
    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            System.out.println("NEAUTENTIFICIRAN zahtjev na: " + request.getRequestURI());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Korisnik nije autentificiran ili token nije ispravan.");
        };
    }

    // ...
    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            System.out.println("ZABRANJEN pristup na: " + request.getRequestURI());
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Korisnik nema potrebnu ulogu (role) za pristup ovom resursu.");
        };
    }

    // ...
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ...
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}