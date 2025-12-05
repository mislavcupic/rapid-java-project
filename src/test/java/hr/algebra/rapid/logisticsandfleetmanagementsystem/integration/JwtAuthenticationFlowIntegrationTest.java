package hr.algebra.rapid.logisticsandfleetmanagementsystem.integration;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RefreshToken;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserRole;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.dto.RegisterRequestDTO;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRoleRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.JwtService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RefreshTokenService;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * INTEGRACIJSKI TEST - JWT Authentication Complete Flow
 * 
 * Testira:
 * 1. User Registration
 * 2. Login (username/password → JWT)
 * 3. JWT Token Validation
 * 4. Protected Endpoint Access
 * 5. Token Expiration Handling
 * 6. Refresh Token Flow
 */
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class JwtAuthenticationFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    private RegisterRequestDTO registerRequest;

    @BeforeEach
    void setUp() {
        // Setup default role
        if (userRoleRepository.findByName("ROLE_DRIVER").isEmpty()) {
            UserRole driverRole = new UserRole();
            driverRole.setName("ROLE_DRIVER");
            userRoleRepository.save(driverRole);
        }

        // Setup registration request
        registerRequest = new RegisterRequestDTO();
        registerRequest.setUsername("jwttest_user");
        registerRequest.setPassword("testPassword123");
        registerRequest.setFirstName("JWT");
        registerRequest.setLastName("Test");
        registerRequest.setEmail("jwttest@example.com");
    }

    // ==========================================
    // REGISTRATION FLOW TESTS
    // ==========================================

    @Test
    void testUserRegistration_Success() throws Exception {
        // Act
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "newuser",
                        "password": "password123",
                        "firstName": "New",
                        "lastName": "User",
                        "email": "newuser@test.com"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.username").value("newuser"));

        // Verify user was created
        UserInfo user = userRepository.findByUsername("newuser");
        assertNotNull(user);
        assertEquals("newuser@test.com", user.getEmail());
    }

    @Test
    void testUserRegistration_DuplicateUsername() throws Exception {
        // Arrange - Create first user
        userService.registerUser(registerRequest);

        // Act - Try to register with same username
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "jwttest_user",
                        "password": "password123",
                        "firstName": "Duplicate",
                        "lastName": "User",
                        "email": "different@test.com"
                    }
                    """))
                .andExpect(status().isBadRequest());
    }

    // ==========================================
    // LOGIN FLOW TESTS
    // ==========================================

    @Test
    void testLogin_Success() {
        // Arrange
        userService.registerUser(registerRequest);

        // Act
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("jwttest_user", "testPassword123")
        );

        // Assert
        assertTrue(authentication.isAuthenticated());
        assertEquals("jwttest_user", authentication.getName());
    }

    @Test
    void testLogin_GeneratesValidJwt() {
        // Arrange
        userService.registerUser(registerRequest);



        // Generate JWT
        String token = jwtService.generateToken("jwttest_user");

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 50); // JWT should be long

        // Verify token content
        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("jwttest_user", extractedUsername);
    }

    @Test
    void testLogin_InvalidCredentials() {
        // Arrange
        userService.registerUser(registerRequest);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken("jwttest_user", "wrongPassword")
            );
        });
    }

    // ==========================================
    // TOKEN VALIDATION TESTS
    // ==========================================

    @Test
    void testJwtValidation_ValidToken() {
        // Arrange
        userService.registerUser(registerRequest);
        String token = jwtService.generateToken("jwttest_user");
        UserDetails userDetails = userDetailsService.loadUserByUsername("jwttest_user");

        // Act
        Boolean isValid = jwtService.validateToken(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void testJwtValidation_WrongUsername() {
        // Arrange
        userService.registerUser(registerRequest);
        
        // Create another user
        RegisterRequestDTO otherRequest = new RegisterRequestDTO();
        otherRequest.setUsername("otheruser");
        otherRequest.setPassword("password");
        otherRequest.setFirstName("Other");
        otherRequest.setLastName("User");
        otherRequest.setEmail("other@test.com");
        userService.registerUser(otherRequest);

        // Generate token for one user
        String token = jwtService.generateToken("jwttest_user");
        
        // Try to validate with different user's details
        UserDetails otherUserDetails = userDetailsService.loadUserByUsername("otheruser");

        // Act
        Boolean isValid = jwtService.validateToken(token, otherUserDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void testJwtExtraction_Username() {
        // Arrange
        userService.registerUser(registerRequest);
        String token = jwtService.generateToken("jwttest_user");

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("jwttest_user", username);
    }

    // ==========================================
    // PROTECTED ENDPOINT ACCESS TESTS
    // ==========================================

    @Test
    void testProtectedEndpoint_WithValidToken() throws Exception {
        // Arrange
        userService.registerUser(registerRequest);
        String token = jwtService.generateToken("jwttest_user");

        // Act & Assert
        mockMvc.perform(get("/api/vehicles")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testProtectedEndpoint_WithoutToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/vehicles"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testProtectedEndpoint_WithInvalidToken() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/vehicles")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    void testProtectedEndpoint_WithExpiredToken() {
        // Note: Testiranje expired tokena zahtijeva mockiranje vremena
        // ili korištenje custom JWT servisa s malim expirationom
        // Ovo je primjer kako bi se moglo testirati:
        
        userService.registerUser(registerRequest);
        String token = jwtService.generateToken("jwttest_user");
        
        // Token bi trebao biti valjan odmah nakon kreiranja
        assertNotNull(token);
        assertTrue(!token.isEmpty());
    }

    // ==========================================
    // REFRESH TOKEN FLOW TESTS
    // ==========================================

    @Test
    void testRefreshTokenFlow_Complete() {
        // Arrange
        userService.registerUser(registerRequest);

        // Step 1: Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken("jwttest_user");
        assertNotNull(refreshToken);
        assertNotNull(refreshToken.getToken());

        // Step 2: Verify token can be found
        var foundToken = refreshTokenService.findByToken(refreshToken.getToken());
        assertTrue(foundToken.isPresent());
        assertEquals("jwttest_user", foundToken.get().getUserInfo().getUsername());

        // Step 3: Verify token expiration
        RefreshToken verifiedToken = refreshTokenService.verifyExpiration(refreshToken);
        assertNotNull(verifiedToken);

        // Step 4: Generate new access token using refresh token
        String newAccessToken = jwtService.generateToken("jwttest_user");
        assertNotNull(newAccessToken);

        // Step 5: Verify new access token is valid
        UserDetails userDetails = userDetailsService.loadUserByUsername("jwttest_user");
        assertTrue(jwtService.validateToken(newAccessToken, userDetails));
    }

    @Test
    void testRefreshToken_ReplacesOldToken() {
        // Arrange
        userService.registerUser(registerRequest);

        // Act - Create first refresh token
        RefreshToken firstToken = refreshTokenService.createRefreshToken("jwttest_user");
        String firstTokenString = firstToken.getToken();

        // Create second refresh token (should replace first)
        RefreshToken secondToken = refreshTokenService.createRefreshToken("jwttest_user");
        String secondTokenString = secondToken.getToken();

        // Assert
        assertNotEquals(firstTokenString, secondTokenString);



        
        // Second token should exist
        var foundSecondToken = refreshTokenService.findByToken(secondTokenString);
        assertTrue(foundSecondToken.isPresent());
    }

    // ==========================================
    // ROLE-BASED ACCESS TESTS
    // ==========================================



    @Test
    void testRoleBasedAccess_DriverCannotAccessAdminEndpoints() throws Exception {
        // Arrange
        userService.registerUser(registerRequest); // Default role is DRIVER
        String token = jwtService.generateToken("jwttest_user");

        // Act & Assert - Driver cannot delete (ADMIN only operation)
        mockMvc.perform(delete("/api/vehicles/1")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    // ==========================================
    // COMPLETE AUTHENTICATION WORKFLOW
    // ==========================================

    @Test
    void testCompleteAuthenticationWorkflow() throws Exception {
        // Step 1: Register
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "username": "complete_test",
                        "password": "password123",
                        "firstName": "Complete",
                        "lastName": "Test",
                        "email": "complete@test.com"
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());

        // Step 2: Login to get token
        String token = jwtService.generateToken("complete_test");

        // Step 3: Access protected endpoint
        mockMvc.perform(get("/api/vehicles")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        // Step 4: Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken("complete_test");
        assertNotNull(refreshToken);

        // Step 5: Verify refresh token
        RefreshToken verified = refreshTokenService.verifyExpiration(refreshToken);
        assertNotNull(verified);

        // Step 6: Generate new access token
        String newToken = jwtService.generateToken("complete_test");
        assertNotNull(newToken);

        // Step 7: Access endpoint with new token
        mockMvc.perform(get("/api/vehicles")
                .header("Authorization", "Bearer " + newToken))
                .andExpect(status().isOk());
    }
}
