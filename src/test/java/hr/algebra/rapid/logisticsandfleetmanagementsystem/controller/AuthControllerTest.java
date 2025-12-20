package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RefreshToken;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RefreshTokenService; // DODANO
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthController Tests")
class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private RefreshTokenService refreshTokenService; // KLJUČNO: Dodan mock servisa koji je falio

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("POST /auth/register - should register user")
    void registerUser_ShouldReturnToken() throws Exception {
        UserInfo newUser = new UserInfo();
        newUser.setUsername("newuser");

        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken(UUID.randomUUID().toString());

        when(userService.registerUser(any(RegisterRequestDTO.class))).thenReturn(newUser);
        when(jwtService.generateToken("newuser")).thenReturn("token123");
        // Popravljamo NPE:
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(mockRefreshToken);

        String json = """
            {
                "username": "newuser",
                "password": "password123",
                "email": "new@example.com",
                "firstName": "New",
                "lastName": "User"
            }
            """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("token123"))
                .andExpect(jsonPath("$.username").value("newuser"));
    }

    @Test
    @DisplayName("POST /auth/login - should login user")
    void authenticateAndGetToken_ShouldReturnToken() throws Exception {
        RefreshToken mockRefreshToken = new RefreshToken();
        mockRefreshToken.setToken("refresh-123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtService.generateToken("user1")).thenReturn("token123");
        // Popravljamo NPE:
        when(refreshTokenService.createRefreshToken("user1")).thenReturn(mockRefreshToken);

        String json = """
            {
                "username": "user1",
                "password": "password123"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("token123"));
    }

    @Test
    @DisplayName("POST /auth/register - korisnik već postoji (Conflict/Internal Error branch)")
    void register_UserAlreadyExists_ShouldReturnError() throws Exception {
        // Simuliramo da servis baci grešku
        when(userService.registerUser(any())).thenThrow(new RuntimeException("Username taken"));

        // DODANA POLJA DA PROĐE VALIDACIJU (FirstName i LastName su obavezni po tvom logu)
        String json = """
        {
            "username": "existing_user",
            "password": "password123",
            "email": "existing@example.com",
            "firstName": "Test",
            "lastName": "Test"
        }
        """;

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @DisplayName("POST /auth/login - pogrešna lozinka (401 Unauthorized)")
    void login_WrongPassword_ShouldReturn401() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.BadCredentialsException("Wrong"));

        String json = "{\"username\": \"test\", \"password\": \"wrong\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @DisplayName("POST /auth/login - neuspješna autentifikacija (isAuthenticated = false)")
    void login_NotAuthenticated_ShouldReturn401() throws Exception {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        // Testiramo granu: if (!authentication.isAuthenticated())
        when(authentication.isAuthenticated()).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user\",\"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login - korisnik je zaključan (LockedException)")
    void login_UserLocked_ShouldReturn401() throws Exception {
        // Testiramo specifičnu granu Exception Handlera
        when(authenticationManager.authenticate(any()))
                .thenThrow(new org.springframework.security.authentication.LockedException("Account locked"));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"locked\",\"password\":\"pass\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /auth/login - neispravan JSON format / kredencijali")
    void login_InvalidJson_ShouldReturn401() throws Exception {
        // Log kaže da AuthController.java:154 baca UsernameNotFoundException
        // Spring to automatski pretvara u 401.
        String badJson = "{\"username\": \"nepostojeći\", \"password\": \"loša_lozinka\"}";

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // 2. REGISTRACIJA - SVE GRANE (BRANCHES)
    // ==========================================

    @Test
    @DisplayName("POST /auth/refreshToken - token istekao")
    void refreshToken_Expired_ShouldReturn401() throws Exception {
        RefreshToken expiredToken = new RefreshToken();
        expiredToken.setToken("expired-123");

        // Dodaj lenient() ovdje
        lenient().when(refreshTokenService.findByToken("expired-123")).thenReturn(Optional.of(expiredToken));
        lenient().when(refreshTokenService.verifyExpiration(any())).thenThrow(new RuntimeException("Token expired"));

        mockMvc.perform(post("/auth/refreshToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\": \"expired-123\"}"))
                .andExpect(status().isUnauthorized());
    }

}