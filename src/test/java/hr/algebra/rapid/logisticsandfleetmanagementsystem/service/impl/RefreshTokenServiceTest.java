package hr.algebra.rapid.logisticsandfleetmanagementsystem.service.impl;

import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.RefreshToken;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.domain.UserInfo;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions.TokenExpiredException;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.RefreshTokenRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.repository.UserRepository;
import hr.algebra.rapid.logisticsandfleetmanagementsystem.service.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * UNIT TESTOVI ZA RefreshTokenService
 * Pokriva JWT refresh token creation, validation, expiration
 */
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private UserInfo testUser;
    private RefreshToken testToken;

    @BeforeEach
    void setUp() {
        testUser = new UserInfo();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        testToken = RefreshToken.builder()
                .id(1)
                .userInfo(testUser)
                .token("test-refresh-token-uuid")
                .expiryDate(Instant.now().plusMillis(600000))
                .build();
    }

    // ==========================================
    // CREATE REFRESH TOKEN TESTS
    // ==========================================

    @Test
    void testCreateRefreshToken_NewUser() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(refreshTokenRepository.findByUserInfo(testUser)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testToken);

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(testUser, result.getUserInfo());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(refreshTokenRepository, times(1)).findByUserInfo(testUser);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testCreateRefreshToken_DeletesOldToken() {
        // Arrange
        RefreshToken oldToken = RefreshToken.builder()
                .id(2)
                .userInfo(testUser)
                .token("old-token")
                .expiryDate(Instant.now().minusMillis(10000))
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(refreshTokenRepository.findByUserInfo(testUser)).thenReturn(Optional.of(oldToken));
        doNothing().when(refreshTokenRepository).delete(oldToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testToken);

        // Act
        RefreshToken result = refreshTokenService.createRefreshToken("testuser");

        // Assert
        assertNotNull(result);
        verify(refreshTokenRepository, times(1)).delete(oldToken);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testCreateRefreshToken_TokenIsUUID() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(refreshTokenRepository.findByUserInfo(testUser)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            assertNotNull(token.getToken());
            assertTrue(token.getToken().length() > 20); // UUID format check
            return token;
        });

        // Act
        refreshTokenService.createRefreshToken("testuser");

        // Assert
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testCreateRefreshToken_ExpiryDateSet() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(refreshTokenRepository.findByUserInfo(testUser)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            assertNotNull(token.getExpiryDate());
            assertTrue(token.getExpiryDate().isAfter(Instant.now()));
            return token;
        });

        // Act
        refreshTokenService.createRefreshToken("testuser");

        // Assert
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    // ==========================================
    // FIND BY TOKEN TESTS
    // ==========================================

    @Test
    void testFindByToken_Success() {
        // Arrange
        when(refreshTokenRepository.findByToken("test-refresh-token-uuid"))
                .thenReturn(Optional.of(testToken));

        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken("test-refresh-token-uuid");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("test-refresh-token-uuid", result.get().getToken());
        verify(refreshTokenRepository, times(1)).findByToken("test-refresh-token-uuid");
    }

    @Test
    void testFindByToken_NotFound() {
        // Arrange
        when(refreshTokenRepository.findByToken("nonexistent-token"))
                .thenReturn(Optional.empty());

        // Act
        Optional<RefreshToken> result = refreshTokenService.findByToken("nonexistent-token");

        // Assert
        assertFalse(result.isPresent());
        verify(refreshTokenRepository, times(1)).findByToken("nonexistent-token");
    }

    // ==========================================
    // VERIFY EXPIRATION TESTS
    // ==========================================

    @Test
    void testVerifyExpiration_ValidToken() {
        // Arrange
        testToken.setExpiryDate(Instant.now().plusMillis(300000)); // Valid for 5 more minutes

        // Act
        RefreshToken result = refreshTokenService.verifyExpiration(testToken);

        // Assert
        assertNotNull(result);
        assertEquals(testToken, result);
        verify(refreshTokenRepository, never()).delete(any(RefreshToken.class));
    }

    @Test
    void testVerifyExpiration_ExpiredToken() {
        // Arrange
        testToken.setExpiryDate(Instant.now().minusMillis(10000)); // Expired 10 seconds ago
        doNothing().when(refreshTokenRepository).delete(testToken);

        // Act & Assert
        TokenExpiredException exception = assertThrows(TokenExpiredException.class, () -> {
            refreshTokenService.verifyExpiration(testToken);
        });

        assertTrue(exception.getMessage().contains("expired"));
        verify(refreshTokenRepository, times(1)).delete(testToken);
    }

    @Test
    void testVerifyExpiration_JustExpired() {
        // Arrange
        testToken.setExpiryDate(Instant.now().minusMillis(1)); // Just expired 1ms ago
        doNothing().when(refreshTokenRepository).delete(testToken);

        // Act & Assert
        assertThrows(TokenExpiredException.class, () -> {
            refreshTokenService.verifyExpiration(testToken);
        });

        verify(refreshTokenRepository, times(1)).delete(testToken);
    }

    @Test
    void testVerifyExpiration_DeletesExpiredToken() {
        // Arrange
        testToken.setExpiryDate(Instant.now().minusMillis(60000)); // Expired 1 minute ago
        doNothing().when(refreshTokenRepository).delete(testToken);

        // Act
        try {
            refreshTokenService.verifyExpiration(testToken);
        } catch (TokenExpiredException _) {
            // Expected
        }

        // Assert
        verify(refreshTokenRepository, times(1)).delete(testToken);
    }

    // ==========================================
    // INTEGRATION SCENARIO TESTS
    // ==========================================

    @Test
    void testCompleteRefreshFlow_NewToken() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(refreshTokenRepository.findByUserInfo(testUser)).thenReturn(Optional.empty());
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testToken);
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.of(testToken));

        // Act - Create token
        RefreshToken created = refreshTokenService.createRefreshToken("testuser");

        // Act - Find token
        Optional<RefreshToken> found = refreshTokenService.findByToken(created.getToken());

        // Act - Verify token
        RefreshToken verified = refreshTokenService.verifyExpiration(found.get());

        // Assert
        assertNotNull(verified);
        assertEquals(testUser, verified.getUserInfo());
    }

    @Test
    void testCompleteRefreshFlow_ReplaceOldToken() {
        // Arrange
        RefreshToken oldToken = RefreshToken.builder()
                .id(2)
                .userInfo(testUser)
                .token("old-token")
                .expiryDate(Instant.now().plusMillis(300000))
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(refreshTokenRepository.findByUserInfo(testUser))
                .thenReturn(Optional.of(oldToken))
                .thenReturn(Optional.empty());
        doNothing().when(refreshTokenRepository).delete(oldToken);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenReturn(testToken);

        // Act - Create new token (should delete old one)
        RefreshToken newToken = refreshTokenService.createRefreshToken("testuser");

        // Assert
        assertNotNull(newToken);
        verify(refreshTokenRepository, times(1)).delete(oldToken);
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    void testCreateRefreshToken_ExpiryDateIs10Minutes() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(refreshTokenRepository.findByUserInfo(testUser)).thenReturn(Optional.empty());

        Instant[] capturedExpiry = new Instant[1];
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken token = invocation.getArgument(0);
            capturedExpiry[0] = token.getExpiryDate();
            return token;
        });

        // Act
        Instant beforeCreate = Instant.now();
        refreshTokenService.createRefreshToken("testuser");
        Instant afterCreate = Instant.now();

        // Assert - Expiry should be ~10 minutes (600000ms) in the future
        long minExpiry = beforeCreate.plusMillis(595000).toEpochMilli();
        long maxExpiry = afterCreate.plusMillis(605000).toEpochMilli();
        long actualExpiry = capturedExpiry[0].toEpochMilli();

        assertTrue(actualExpiry >= minExpiry && actualExpiry <= maxExpiry,
                "Expiry should be approximately 10 minutes from now");
    }
}
