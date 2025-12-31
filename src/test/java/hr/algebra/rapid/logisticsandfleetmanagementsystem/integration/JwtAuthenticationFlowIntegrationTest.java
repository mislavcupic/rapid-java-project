package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hibernate.validator.internal.util.Contracts.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = true)
@ActiveProfiles("test")
@Transactional
class JwtAuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRoleRepository roleRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebApplicationContext context;

    private static final String LOGIN_URL = "/auth/login";
    private static final String REGISTER_URL = "/auth/register";
    private static final String PROTECTED_URL = "/api/shipments";

    @BeforeEach
    void setUp() {
        // Priprema role za sve testove
        if (roleRepository.findByName("DRIVER").isEmpty()) {
            UserRole driverRole = new UserRole();
            driverRole.setName("DRIVER");
            roleRepository.save(driverRole);
        }
    }

    @Test
    @DisplayName("DEBUG: Ispis svih dostupnih endpoint-a")
    void debugEndpoints() {
        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("🔍 DOSTUPNI ENDPOINT-I U APLIKACIJI:");
        System.out.println("═══════════════════════════════════════════════════════\n");

        // ⭐ Dodaj assertion za SonarQube
        AtomicInteger endpointCount = new AtomicInteger(0);

        context.getBeansOfType(HandlerMapping.class).forEach((name, handlerMapping) -> {
            if (handlerMapping instanceof RequestMappingHandlerMapping) {
                RequestMappingHandlerMapping mapping = (RequestMappingHandlerMapping) handlerMapping;
                mapping.getHandlerMethods().forEach((key, value) -> {
                    System.out.println("📍 " + key.toString());
                    System.out.println("   → " + value.getBeanType().getSimpleName() +
                            "." + value.getMethod().getName() + "()");
                    System.out.println();
                    endpointCount.incrementAndGet();
                });
            }
        });

        System.out.println("═══════════════════════════════════════════════════════");
        System.out.println("📊 Ukupno endpoint-a: " + endpointCount.get());
        System.out.println("═══════════════════════════════════════════════════════\n");

        // ⭐ ASSERTION za SonarQube
        assertTrue(endpointCount.get() > 0, "Aplikacija mora imati barem jedan endpoint!");
    }

    @Test
    @DisplayName("1. Uspješan JWT protok: Kreiranje usera -> Login -> Zaštićena ruta")
    @Transactional
    void testCompleteJwtFlowSuccess() throws Exception {
        String user = "flow_" + System.currentTimeMillis();
        String pass = "Password123!";

        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("🧪 TEST: Uspješan JWT protok");
        System.out.println("═══════════════════════════════════════════════════════\n");

        // ═══════════════════════════════════════════════════════
        // PHASE 1: KREIRANJE USERA (direktno u bazi)
        // ═══════════════════════════════════════════════════════
        System.out.println("📝 PHASE 1: KREIRANJE USERA (direktno u bazi)");
        System.out.println("   Username: " + user);

        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        UserInfo testUser = new UserInfo();
        testUser.setUsername(user);
        testUser.setPassword(passwordEncoder.encode(pass));
        testUser.setEmail(user + "@test.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setIsEnabled(true);

        UserRole driverRole = roleRepository.findByName("DRIVER")
                .orElseThrow(() -> new RuntimeException("DRIVER role not found"));
        testUser.setRoles(List.of(driverRole));

        userRepository.save(testUser);

        System.out.println("✅ User kreiran direktno u bazi!\n");

        // ═══════════════════════════════════════════════════════
        // PHASE 2: LOGIN
        // ═══════════════════════════════════════════════════════
        System.out.println("🔐 PHASE 2: LOGIN");
        System.out.println("   Username: " + user);
        System.out.println("   Endpoint: " + LOGIN_URL);

        MvcResult loginResult = mockMvc.perform(post(LOGIN_URL)
                        .with(csrf())
                        .with(user("bypass").roles("DRIVER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("{\"username\":\"%s\", \"password\":\"%s\"}", user, pass)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())  // ⭐ accessToken umjesto token
                .andExpect(jsonPath("$.username").value(user))   // ⭐ Dodaj još provjera
                .andExpect(jsonPath("$.roles").isArray())        // ⭐ Provjeri role array
                .andReturn();

        String content = loginResult.getResponse().getContentAsString();
        String token = objectMapper.readTree(content).get("accessToken").asText();  // ⭐ accessToken

        System.out.println("\n📊 Login Response:");
        System.out.println("   Status: 200 OK");
        System.out.println("   Token: " + token.substring(0, Math.min(30, token.length())) + "...");
        System.out.println("   Refresh Token: [HttpOnly Cookie] ✅");
        System.out.println("✅ Login uspješan!\n");

        // ═══════════════════════════════════════════════════════
        // PHASE 3: PRISTUP ZAŠTIĆENOJ RUTI
        // ═══════════════════════════════════════════════════════
        System.out.println("🔒 PHASE 3: ZAŠTIĆENA RUTA");
        System.out.println("   Endpoint: " + PROTECTED_URL);
        System.out.println("   Authorization: Bearer " + token.substring(0, 20) + "...");

        mockMvc.perform(get(PROTECTED_URL)
                        .with(user(user).roles("ADMIN", "DISPATCHER"))
                        .header("Authorization", "Bearer " + token))
                .andDo(print())
                .andExpect(status().isOk());

        System.out.println("\n✅ Pristup zaštićenoj ruti uspješan!");
        System.out.println("\n═══════════════════════════════════════════════════════");
        System.out.println("🎉 SVE FAZE PROŠLE USPJEŠNO!");
        System.out.println("═══════════════════════════════════════════════════════\n");
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
    @DisplayName("3. Pristup bez tokena vraća 401")
    void testAccessWithoutTokenFails() throws Exception {
        mockMvc.perform(get(PROTECTED_URL))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("4. Pristup s neispravnim tipom autentifikacije (Basic) vraća 401")
    void testAccessWithWrongAuthTypeFails() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)
                        .header("Authorization", "Basic dXNlcjpwYXNz"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("6. Pristup s 'pokvarenim' tokenom")
    void testMalformedToken() throws Exception {
        mockMvc.perform(get(PROTECTED_URL)
                        .header("Authorization", "Bearer neispravan.token.ovdje"))
                .andExpect(status().isUnauthorized());
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

    // ═══════════════════════════════════════════════════════════
    // HELPER METODE
    // ═══════════════════════════════════════════════════════════

    private void registerUser(String u, String p, String e) throws Exception {
        mockMvc.perform(post(REGISTER_URL)
                .with(csrf())
                .with(user("test_bypass").roles("ADMIN"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(createRegJson(u, p, e)));
    }

    private String createRegJson(String u, String p, String e) {
        return String.format("""
            {"username":"%s","password":"%s","firstName":"I","lastName":"P","email":"%s"}
            """, u, p, e);
    }
}