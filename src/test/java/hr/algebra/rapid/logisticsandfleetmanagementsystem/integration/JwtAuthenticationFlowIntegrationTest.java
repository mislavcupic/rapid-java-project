package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.AuthRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

/**
 * ✅ POPRAVLJEN INTEGRACIJSKI TEST - JWT Authentication Flow
 *
 * FOKUS: Minimalistički MockMvc pristup bez injektiranja servisa.
 *
 * KLJUČNI POPRAVAK:
 * 1. RegisterRequestDTO se šalje kao JSON string, jer klasa ne podržava setRole()
 * i nema konstruktor sa svim argumentima, eliminirajući greške "Cannot resolve method 'setRole'"
 * i "Cannot resolve constructor".
 * 2. Token se dohvaća iz odgovora stvarnog REST API poziva na /api/auth/login.
 * 3. Uklonjene sve injekcije servisa koje nisu MockMvc ili ObjectMapper.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
// DirtiesContext je važan za JWT testove da očisti Spring Security kontekst
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JwtAuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRoleRepository userRoleRepository; // Ostavljamo samo za setUp uloge

    private final String testUsername = "jwt_test_user";
    private final String testPassword = "Password123!";

    /**
     * Priprema baze podataka prije svakog testa.
     * U H2 bazi, uloge (Roles) moraju postojati prije nego što se korisnik registrira.
     */
    @BeforeEach
    @Transactional
    void setUp() {
        // Osiguravanje da uloge postoje u bazi za H2 testni kontekst
        // Ovo je neophodno jer se DDL (create-drop) pokreće prije svakog testa
        if (userRoleRepository.findByName("ADMIN").isEmpty()) {
            UserRole adminRole = new UserRole();
            adminRole.setName("ADMIN");
            userRoleRepository.save(adminRole);
        }
        if (userRoleRepository.findByName("DISPATCHER").isEmpty()) {
            UserRole dispatcherRole = new UserRole();
            dispatcherRole.setName("DISPATCHER");
            userRoleRepository.save(dispatcherRole);
        }
    }

    /**
     * Testira kompletan uspješni tok JWT autentifikacije i autorizacije:
     * Registracija -> Prijava (dohvat tokena) -> Pristup zaštićenom resursu.
     */
    @Test
    @Transactional
    void testCompleteJwtFlowSuccess() throws Exception {
        // ===================================
        // Step 1: Registracija novog korisnika
        // ===================================
        // Slanje JSON stringa za registraciju, jer ne možemo koristiti setRole()
        // Pretpostavljamo da Controller sam dodjeljuje ulogu DISPATCHER,
        // ili da prihvaća ulogu putem ovog DTO-a iako nema polja "role".
        String registrationJson = String.format("""
            {
                "username": "%s",
                "password": "%s",
                "firstName": "JWT",
                "lastName": "Tester",
                "email": "jwt.tester@test.com"
            }
            """, testUsername, testPassword);

        // Važno: Koristite /api/auth/register ili /auth/register, ovisno o Vašem Controlleru.
        // Ostavljam /api/auth/register kako je bilo u prethodnoj verziji.
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrationJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully!"));


        // ===================================
        // Step 2: Prijava i dohvaćanje JWT Tokena
        // ===================================
        AuthRequestDTO authRequest = new AuthRequestDTO(testUsername, testPassword);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        // Izvuci token iz JSON odgovora
        String responseContent = result.getResponse().getContentAsString();
        String accessToken = objectMapper.readTree(responseContent).get("accessToken").asText();
        assertNotNull(accessToken, "Access token mora biti prisutan nakon prijave");

        // ===================================
        // Step 3: Pristup zaštićenom endpointu s tokenom
        // (Pretpostavka: /api/vehicles je zaštićen i dozvoljen DISPATCHER-u)
        // ===================================
        mockMvc.perform(get("/api/vehicles")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray()); // Očekujemo array (barem prazan)


        // ===================================
        // Step 4: Provjera pristupa bez tokena (Unauthorized)
        // ===================================
        mockMvc.perform(get("/api/vehicles")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); // Očekujemo 401 Unauthorized
    }

    /**
     * Testira neuspješnu prijavu s nepostojećim korisnikom ili krivom lozinkom.
     */
    @Test
    @Transactional
    void testInvalidLoginFails() throws Exception {
        // ===================================
        // Provjera neispravne prijave
        // ===================================
        // Korisnik nije registriran.
        AuthRequestDTO authRequest = new AuthRequestDTO(testUsername, testPassword);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized()); // Očekujemo 401 Unauthorized
    }
}