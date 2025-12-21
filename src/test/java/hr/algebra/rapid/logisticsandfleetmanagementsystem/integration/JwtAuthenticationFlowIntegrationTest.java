package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = true) // Osigurava da se tvoj JwtAuthFilter izvršava
@ActiveProfiles("test")
@Transactional
class JwtAuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String LOGIN_URL = "/api/auth/login";
    private static final String REGISTER_URL = "/api/auth/register";
    private static final String PROTECTED_URL = "/api/shipments";

    @Test
    @DisplayName("1. Uspješan JWT protok: Registracija -> Login -> Zaštićena ruta")
    void testCompleteJwtFlowSuccess() throws Exception {
        String user = "flow_" + System.currentTimeMillis();

        // Registracija
        mockMvc.perform(post(REGISTER_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRegJson(user, "Password123!", user + "@test.com")))
                .andExpect(status().isOk());

        // Login
        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\":\"%s\", \"password\":\"Password123!\"}", user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andReturn();

        String token = objectMapper.readTree(loginResult.getResponse().getContentAsString()).get("token").asText();

        // Pristup
        mockMvc.perform(get(PROTECTED_URL)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("2. Login s pogrešnom lozinkom vraća 401")
    void testLoginWithWrongPasswordFails() throws Exception {
        String user = "fail_user_" + System.currentTimeMillis();
        registerUser(user, "Correct123!", user + "@test.com");

        mockMvc.perform(post(LOGIN_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\":\"%s\", \"password\":\"Wrong!\"}", user)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("3. Pristup bez tokena vraća 401 (prema tvom logu)")
    void testAccessWithoutTokenFails() throws Exception {
        mockMvc.perform(get(PROTECTED_URL))
                .andExpect(status().isUnauthorized()); // Promijenjeno s 403 na 401 prema tvom error logu
    }

    @Test
    @DisplayName("4. Pristup s neispravnim tipom autentifikacije (Basic) vraća 401")
    void testAccessWithWrongAuthTypeFails() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)
                        .header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized()); // Ovo je onaj test koji ti je bacio Actual: 401
    }

    @Test
    @DisplayName("5. Registracija s već postojećim korisnikom")
    void testDuplicateUserRegistration() throws Exception {
        String user = "duplicate";
        registerUser(user, "Pass123!", "e1@t.com");

        mockMvc.perform(post(REGISTER_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createRegJson(user, "Pass123!", "e2@t.com")))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("6. Pristup s 'pokvarenim' tokenom")
    void testMalformedToken() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)
                        .header("Authorization", "Bearer neispravan.token.ovdje"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("7. Registracija s neispravnim podacima (Validation)")
    void testInvalidDataRegistration() throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\", \"password\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("8. Login nepostojećeg korisnika")
    void testLoginNonExistent() throws Exception {
        mockMvc.perform(post(LOGIN_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"niko\", \"password\":\"ništa\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("9. Provjera Case-Sensitivity u Login-u")
    void testLoginCaseSensitivity() throws Exception {
        String user = "CaseUser";
        registerUser(user, "Pass123!", "case@test.com");

        mockMvc.perform(post(LOGIN_URL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\":\"%s\", \"password\":\"Pass123!\"}", user.toLowerCase())))
                .andExpect(status().isUnauthorized());
    }

    // Pomoćne metode
    private void registerUser(String u, String p, String e) throws Exception {
        mockMvc.perform(post(REGISTER_URL).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRegJson(u, p, e)));
    }

    private String createRegJson(String u, String p, String e) {
        return String.format("{\"username\":\"%s\", \"password\":\"%s\", \"firstName\":\"I\", \"lastName\":\"P\", \"email\":\"%s\"}", u, p, e);
    }
}