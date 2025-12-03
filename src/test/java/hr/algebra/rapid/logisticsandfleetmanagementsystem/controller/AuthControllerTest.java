package hr.algebra.rapid.logisticsandfleetmanagementsystem.controller;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
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
        newUser.setFirstName("New");
        
        when(userService.registerUser(any(RegisterRequestDTO.class))).thenReturn(newUser);
        when(jwtService.generateToken("newuser")).thenReturn("token123");

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

        verify(userService).registerUser(any(RegisterRequestDTO.class));
        verify(jwtService).generateToken("newuser");
    }

    @Test
    @DisplayName("POST /auth/login - should login user")
    void authenticateAndGetToken_ShouldReturnToken() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(jwtService.generateToken("user1")).thenReturn("token123");

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
                .andExpect(jsonPath("$.accessToken").value("token123"))
                .andExpect(jsonPath("$.username").value("user1"));

        verify(jwtService).generateToken("user1");
    }

    @Test
    @DisplayName("POST /auth/login - should return 401 on invalid credentials")
    void authenticateAndGetToken_WithInvalidCredentials_ShouldReturn401() throws Exception {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Invalid credentials"));

        String json = """
            {
                "username": "invalid",
                "password": "wrong"
            }
            """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }
}
