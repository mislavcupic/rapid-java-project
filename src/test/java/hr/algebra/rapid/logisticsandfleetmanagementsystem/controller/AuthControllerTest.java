package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RefreshToken;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RefreshTokenService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("AuthController Coverage Fix")
class AuthControllerTest {

    @Mock private UserService userService;
    @Mock private JwtService jwtService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private Authentication authentication;

    @InjectMocks private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    // 1. REGISTRACIJA - USPJEH (Popravljeni podaci za validaciju)
    @Test
    @DisplayName("Branch: Register Success - Valid Data")
    void register_Success() throws Exception {
        UserInfo user = new UserInfo();
        user.setUsername("validUser");
        RefreshToken rt = new RefreshToken();
        rt.setToken("rt-123");

        when(userService.registerUser(any())).thenReturn(user);
        when(jwtService.generateToken(anyString())).thenReturn("at-123");
        when(refreshTokenService.createRefreshToken(anyString())).thenReturn(rt);

        // Koristimo duža imena da prođemo @Size validaciju
        String body = "{\"username\":\"validUser\",\"password\":\"password123\",\"firstName\":\"Imeee\",\"lastName\":\"Prezimeee\",\"email\":\"test@test.com\"}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    // 2. REGISTRACIJA - KONFLIKT (Grana iznimke)
    @Test
    @DisplayName("Branch: Register Conflict - Service Throws")
    void register_Conflict() throws Exception {
        // Simuliramo da servis baci grešku nakon što validacija prođe
        when(userService.registerUser(any())).thenThrow(new RuntimeException("Conflict"));

        String body = "{\"username\":\"validUser\",\"password\":\"password123\",\"firstName\":\"Imeee\",\"lastName\":\"Prezimeee\",\"email\":\"test@test.com\"}";

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isInternalServerError());
    }

    // 3. LOGIN - USPJEH
    @Test
    @DisplayName("Branch: Login Success")
    void login_Success() throws Exception {
        RefreshToken rt = new RefreshToken();
        rt.setToken("rt-123");

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtService.generateToken(any())).thenReturn("at-123");
        when(refreshTokenService.createRefreshToken(any())).thenReturn(rt);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user123\",\"password\":\"pass123\"}"))
                .andExpect(status().isOk());
    }

    // 4. LOGIN - NEUSPJEH (isAuthenticated = false grana)
    @Test
    @DisplayName("Branch: Login - Not Authenticated")
    void login_NotAuthenticated() throws Exception {
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"user123\",\"password\":\"pass123\"}"))
                .andExpect(status().isUnauthorized());
    }

    // 5. REFRESH TOKEN - USPJEH (Popravljen Optional i UserInfo)
    @Test
    @DisplayName("Branch: Refresh Success - FINAL (Checking Cookie instead of Body)")
    void refreshToken_Success() throws Exception {
        // 1. Podaci
        String oldToken = "old-token-123";
        String newToken = "new-refresh-token-456";
        String username = "mislav";

        UserInfo ui = new UserInfo();
        ui.setUsername(username);
        ui.setRoles(new java.util.ArrayList<>());

        RefreshToken rt = new RefreshToken();
        rt.setToken(oldToken);
        rt.setUserInfo(ui);

        RefreshToken nextRt = new RefreshToken();
        nextRt.setToken(newToken);
        nextRt.setUserInfo(ui);

        // 2. Mockanje
        lenient().when(refreshTokenService.findByToken(eq(oldToken))).thenReturn(Optional.of(rt));
        lenient().when(refreshTokenService.verifyExpiration(any())).thenReturn(rt);
        lenient().when(refreshTokenService.createRefreshToken(eq(username))).thenReturn(nextRt);
        lenient().when(jwtService.generateToken(eq(username))).thenReturn("new-access-token");

        // 3. Poziv i provjera
        mockMvc.perform(post("/auth/refreshToken")
                        .cookie(new jakarta.servlet.http.Cookie("refreshToken", oldToken))
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                // Provjeravamo Access Token u Body-ju
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                // Provjeravamo Refresh Token u COOKIE-u (jer je u body-ju null)
                .andExpect(cookie().value("refreshToken", newToken))
                .andExpect(cookie().httpOnly("refreshToken", true));
    }
    // 6. REFRESH TOKEN - ISTEKAO (401 grana)
    @Test
    @DisplayName("Branch: Refresh Expired")
    void refreshToken_Expired() throws Exception {
        RefreshToken rt = new RefreshToken();
        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.of(rt));

        // Bacamo specifičnu iznimku koju tvoj sustav mapira na 401
        when(refreshTokenService.verifyExpiration(any()))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Expired") {});

        mockMvc.perform(post("/auth/refreshToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"expired-token\"}"))
                .andExpect(status().isUnauthorized());
    }

    // 7. REFRESH TOKEN - NEPOSTOJEĆI (Optional.empty grana)
    @Test
    @DisplayName("Branch: Refresh Not Found")
    void refreshToken_NotFound() throws Exception {
        when(refreshTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/refreshToken")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"unknown\"}"))
                .andExpect(status().isUnauthorized());
    }
}