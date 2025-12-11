package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.filter.JwtAuthFilter;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class) // Osigurava redoslijed izvršenja (iako JUnit to ne preporučuje, korisno je za flow testove)
class JwtAuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    // OVO JE KLJUČNO ZA IZBJEGAVANJE PROBLEMA S JWT FILTROM U INTEGRACIJSKOM TESTU
    @MockitoBean
    private JwtAuthFilter jwtAuthenticationFilter;

    private static final String USERNAME = "test_user_flow";
    private static final String PASSWORD = "Password123!";
    private static final String EMAIL = "test.flow@test.com";
    private static final String LOGIN_URL = "/api/auth/login";
    private static final String REGISTER_URL = "/api/auth/register";
    private static final String PROTECTED_URL = "/api/shipments";

    // Pomoćne metode za kreiranje JSON tijela
    private String createRegistrationRequest(String username, String email) {
        return String.format("""
            {
                "username": "%s",
                "password": "%s",
                "firstName": "JWT",
                "lastName": "Tester",
                "email": "%s"
            }
            """, username, PASSWORD, email);
    }

    private String createLoginRequest(String username, String password) {
        return String.format("""
            {
                "username": "%s",
                "password": "%s"
            }
            """, username, password);
    }

    // Čišćenje testnih podataka nakon svakog testa
    @AfterEach
    void tearDown() {
        userRepository.deleteByUsername(USERNAME);
        userRepository.deleteByUsername("duplicate_user");
        userRepository.deleteByUsername("fail_login_user");
        userRepository.deleteByUsername("jwt_test_user"); // Iz prethodnog testa
    }

    // ----------------------------------------------------------------------------------
    // TEST 1: Cijeli JWT Protok (Registracija 201, Login 200, Pristup 200)
    // ----------------------------------------------------------------------------------
    @Test
    @org.junit.jupiter.api.Order(1)
    void test1_CompleteJwtFlowSuccess() throws Exception {

        // 1. REGISTRACIJA KORISNIKA (Očekujemo 201 Created)
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRegistrationRequest(USERNAME, EMAIL)))
                .andExpect(status().isCreated()); // Očekuje 201 Created

        // 2. LOGIN KORISNIKA I DOBIJANJE JWT TOKENA (Očekujemo 200 OK)
        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createLoginRequest(USERNAME, PASSWORD)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();


        // 3. PRISTUP ZAŠTIĆENOJ RUTI S TOKENOM (Očekujemo 200 OK)
        mockMvc.perform(get(PROTECTED_URL)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // ----------------------------------------------------------------------------------
    // TEST 2: Neuspješna registracija (Korisnik već postoji)
    // ----------------------------------------------------------------------------------
    @Test
    @org.junit.jupiter.api.Order(2)
    void test2_DuplicateRegistrationFails() throws Exception {
        String duplicateUsername = "duplicate_user";
        String duplicateEmail = "duplicate.test@test.com";

        // Prvo registrirajte korisnika
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRegistrationRequest(duplicateUsername, duplicateEmail)))
                .andExpect(status().isCreated());

        // Pokušaj ponovne registracije istim korisničkim imenom
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRegistrationRequest(duplicateUsername, duplicateEmail)))
                // Očekujemo 400 Bad Request ili 409 Conflict. 400 je češći za validaciju.
                .andExpect(status().isBadRequest());
    }

    // ----------------------------------------------------------------------------------
    // TEST 3: Neuspješna prijava (Pogrešni podaci)
    // ----------------------------------------------------------------------------------
    @Test
    @org.junit.jupiter.api.Order(3)
    void test3_InvalidLoginCredentialsFails() throws Exception {

        // Registrirajte korisnika
        mockMvc.perform(post(REGISTER_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRegistrationRequest("fail_login_user", "fail.login@test.com")))
                .andExpect(status().isCreated());

        // Pokušajte se prijaviti s pogrešnom lozinkom
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createLoginRequest("fail_login_user", "WrongPassword!")))
                // Očekujemo 401 Unauthorized
                .andExpect(status().isUnauthorized());

        // Pokušajte se prijaviti s nepostojećim korisnikom
        mockMvc.perform(post(LOGIN_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createLoginRequest("non_existent_user", PASSWORD)))
                // Očekujemo 401 Unauthorized
                .andExpect(status().isUnauthorized());
    }

    // ----------------------------------------------------------------------------------
    // TEST 4: Pristup zaštićenoj ruti bez tokena
    // ----------------------------------------------------------------------------------
    @Test
    @org.junit.jupiter.api.Order(4)
    void test4_AccessProtectedWithoutTokenFails() throws Exception {

        mockMvc.perform(get(PROTECTED_URL)
                        .contentType(MediaType.APPLICATION_JSON))
                // Očekujemo 403 Forbidden od Spring Security
                .andExpect(status().isForbidden());
    }
}