package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testovi za TokenExpiredException.
 * Testira oba konstruktora i osnovne funkcionalnosti iznimke.
 */
@DisplayName("TokenExpiredException Tests")
class TokenExpiredExceptionTest {

    @Test
    @DisplayName("Konstruktor s porukom - provjera poruke")
    void testConstructorWithMessage_MessageIsCorrect() {
        // Arrange
        String expectedMessage = "JWT token je istekao";

        // Act
        TokenExpiredException exception = new TokenExpiredException(expectedMessage);

        // Assert
        assertEquals(expectedMessage, exception.getMessage(), 
            "Poruka iznimke bi trebala biti identična predanoj poruci");
    }

    @Test
    @DisplayName("Konstruktor s porukom i uzrokom - provjera poruke")
    void testConstructorWithMessageAndCause_MessageIsCorrect() {
        // Arrange
        String expectedMessage = "Refresh token je istekao";
        Throwable cause = new RuntimeException("Underlying JWT error");

        // Act
        TokenExpiredException exception = new TokenExpiredException(expectedMessage, cause);

        // Assert
        assertEquals(expectedMessage, exception.getMessage(), 
            "Poruka iznimke bi trebala biti identična predanoj poruci");
    }

    @Test
    @DisplayName("Konstruktor s porukom i uzrokom - provjera uzroka")
    void testConstructorWithMessageAndCause_CauseIsCorrect() {
        // Arrange
        String message = "Token validation failed";
        Throwable expectedCause = new IllegalStateException("Invalid token signature");

        // Act
        TokenExpiredException exception = new TokenExpiredException(message, expectedCause);

        // Assert
        assertSame(expectedCause, exception.getCause(), 
            "Uzrok iznimke bi trebao biti identičan predanom uzroku");
    }

    @Test
    @DisplayName("Iznimka je RuntimeException")
    void testExceptionIsRuntimeException() {
        // Arrange & Act
        TokenExpiredException exception = new TokenExpiredException("Test message");

        // Assert
        assertInstanceOf(RuntimeException.class, exception, 
            "TokenExpiredException bi trebala biti podtip RuntimeException");
    }

    @Test
    @DisplayName("Bacanje i hvatanje iznimke - konstruktor s porukom")
    void testThrowingAndCatchingException_MessageOnly() {
        // Arrange
        String expectedMessage = "Access token je istekao";

        // Act & Assert
        TokenExpiredException exception = assertThrows(
            TokenExpiredException.class,
            () -> {
                throw new TokenExpiredException(expectedMessage);
            },
            "Iznimka bi trebala biti bacena i uhvaćena"
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Bacanje i hvatanje iznimke - konstruktor s porukom i uzrokom")
    void testThrowingAndCatchingException_MessageAndCause() {
        // Arrange
        String expectedMessage = "Token expired during authentication";
        Throwable cause = new Exception("JWT parsing failed");

        // Act & Assert
        TokenExpiredException exception = assertThrows(
            TokenExpiredException.class,
            () -> {
                throw new TokenExpiredException(expectedMessage, cause);
            },
            "Iznimka bi trebala biti bacena i uhvaćena"
        );

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Null poruka - konstruktor s porukom")
    void testNullMessage() {
        // Act
        TokenExpiredException exception = new TokenExpiredException(null);

        // Assert
        assertNull(exception.getMessage(), 
            "Poruka bi trebala biti null ako je null predana");
    }

    @Test
    @DisplayName("Null poruka - konstruktor s porukom i uzrokom")
    void testNullMessageWithCause() {
        // Arrange
        Throwable cause = new RuntimeException("Some cause");

        // Act
        TokenExpiredException exception = new TokenExpiredException(null, cause);

        // Assert
        assertNull(exception.getMessage(), 
            "Poruka bi trebala biti null ako je null predana");
        assertNotNull(exception.getCause(), 
            "Uzrok ne bi trebao biti null");
    }

    @Test
    @DisplayName("Null uzrok")
    void testNullCause() {
        // Arrange
        String message = "Token expired";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message, null);

        // Assert
        assertNotNull(exception.getMessage(), 
            "Poruka ne bi trebala biti null");
        assertNull(exception.getCause(), 
            "Uzrok bi trebao biti null ako je null predan");
    }

    @Test
    @DisplayName("Prazna poruka")
    void testEmptyMessage() {
        // Arrange
        String emptyMessage = "";

        // Act
        TokenExpiredException exception = new TokenExpiredException(emptyMessage);

        // Assert
        assertEquals(emptyMessage, exception.getMessage(), 
            "Prazna poruka bi trebala biti prihvaćena");
        assertTrue(exception.getMessage().isEmpty(), 
            "Poruka bi trebala biti prazna");
    }

    @Test
    @DisplayName("Stack trace se ispravno generira")
    void testStackTraceGeneration() {
        // Arrange
        String message = "Token has expired";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNotNull(exception.getStackTrace(), 
            "Stack trace ne bi trebao biti null");
        assertTrue(exception.getStackTrace().length > 0, 
            "Stack trace bi trebao imati elemente");
    }

    @Test
    @DisplayName("Nested cause - provjera lanca iznimki")
    void testNestedCause() {
        // Arrange
        Throwable rootCause = new IllegalArgumentException("Invalid token format");
        Throwable intermediateCause = new RuntimeException("Token processing error", rootCause);
        String message = "Token expired";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message, intermediateCause);

        // Assert
        assertEquals(intermediateCause, exception.getCause(), 
            "Direktan uzrok bi trebao biti intermediate cause");
        assertEquals(rootCause, exception.getCause().getCause(), 
            "Root cause bi trebao biti dostupan kroz lanac");
    }

    @Test
    @DisplayName("Različite vrste uzročnih iznimki")
    void testDifferentCauseTypes() {
        // Arrange & Act
        TokenExpiredException jwtException = new TokenExpiredException(
            "JWT expired", 
            new io.jsonwebtoken.ExpiredJwtException(null, null, "Token expired")
        );
        
        TokenExpiredException securityException = new TokenExpiredException(
            "Security error", 
            new SecurityException("Unauthorized access")
        );
        
        TokenExpiredException illegalArgException = new TokenExpiredException(
            "Invalid argument", 
            new IllegalArgumentException("Null token provided")
        );

        // Assert
        assertAll("Različite vrste uzročnih iznimki",
            () -> assertInstanceOf(io.jsonwebtoken.ExpiredJwtException.class, 
                jwtException.getCause(), "JWT iznimka bi trebala biti prihvaćena"),
            () -> assertInstanceOf(SecurityException.class, 
                securityException.getCause(), "SecurityException bi trebao biti prihvaćen"),
            () -> assertInstanceOf(IllegalArgumentException.class, 
                illegalArgException.getCause(), "IllegalArgumentException bi trebao biti prihvaćen")
        );
    }

    @Test
    @DisplayName("Testiranje toString metode")
    void testToStringMethod() {
        // Arrange
        String message = "Access token expired";
        TokenExpiredException exception = new TokenExpiredException(message);

        // Act
        String result = exception.toString();

        // Assert
        assertNotNull(result, "toString ne bi trebao vraćati null");
        assertTrue(result.contains("TokenExpiredException"), 
            "toString bi trebao sadržavati ime klase");
        assertTrue(result.contains(message), 
            "toString bi trebao sadržavati poruku");
    }

    @Test
    @DisplayName("Realistične poruke za JWT token scenarije")
    void testRealisticJwtTokenMessages() {
        // Arrange & Act
        TokenExpiredException accessTokenExpired = new TokenExpiredException(
            "Access token je istekao. Molimo prijavite se ponovno."
        );
        TokenExpiredException refreshTokenExpired = new TokenExpiredException(
            "Refresh token je istekao. Potrebna je ponovna autentifikacija."
        );
        TokenExpiredException sessionExpired = new TokenExpiredException(
            "Vaša sesija je istekla zbog neaktivnosti."
        );

        // Assert
        assertAll("Realistične JWT poruke",
            () -> assertTrue(accessTokenExpired.getMessage().contains("Access token")),
            () -> assertTrue(refreshTokenExpired.getMessage().contains("Refresh token")),
            () -> assertTrue(sessionExpired.getMessage().contains("sesija"))
        );
    }

    @Test
    @DisplayName("Poruke s vremenskim detaljima")
    void testMessagesWithTimeDetails() {
        // Arrange
        String messageWithTimestamp = "Token istekao u 2024-12-20 15:30:00 UTC";
        String messageWithDuration = "Token istekao nakon 3600 sekundi (1 sat)";

        // Act
        TokenExpiredException withTimestamp = new TokenExpiredException(messageWithTimestamp);
        TokenExpiredException withDuration = new TokenExpiredException(messageWithDuration);

        // Assert
        assertAll("Poruke s vremenskim detaljima",
            () -> assertTrue(withTimestamp.getMessage().contains("2024-12-20")),
            () -> assertTrue(withDuration.getMessage().contains("3600 sekundi"))
        );
    }

    @Test
    @DisplayName("Specijalni znakovi u poruci")
    void testSpecialCharactersInMessage() {
        // Arrange
        String messageWithSpecialChars = "Token istekao! Potrebna re-autentifikacija @ API/v1/auth 🔒";

        // Act
        TokenExpiredException exception = new TokenExpiredException(messageWithSpecialChars);

        // Assert
        assertEquals(messageWithSpecialChars, exception.getMessage(), 
            "Poruka sa specijalnim znakovima bi trebala biti ispravno pohranjena");
    }

    @Test
    @DisplayName("Višelinijska poruka s detaljima")
    void testMultiLineMessageWithDetails() {
        // Arrange
        String multiLineMessage = "Token Expiration Error\n" +
                                  "Type: JWT Access Token\n" +
                                  "Issued: 2024-12-20 14:00:00\n" +
                                  "Expired: 2024-12-20 15:00:00\n" +
                                  "Action: Please login again";

        // Act
        TokenExpiredException exception = new TokenExpiredException(multiLineMessage);

        // Assert
        assertEquals(multiLineMessage, exception.getMessage(), 
            "Višelinijska poruka bi trebala biti ispravno pohranjena");
        assertTrue(exception.getMessage().contains("\n"), 
            "Poruka bi trebala sadržavati nove retke");
    }

    @Test
    @DisplayName("Cause je null za konstruktor samo s porukom")
    void testCauseIsNullForMessageOnlyConstructor() {
        // Arrange
        String message = "Token expired";

        // Act
        TokenExpiredException exception = new TokenExpiredException(message);

        // Assert
        assertNull(exception.getCause(), 
            "Cause bi trebao biti null kada se koristi konstruktor samo s porukom");
    }

    @Test
    @DisplayName("Usporedba oba konstruktora")
    void testBothConstructors() {
        // Arrange
        String message = "Token expired";
        Throwable cause = new RuntimeException("Underlying error");

        // Act
        TokenExpiredException withMessageOnly = new TokenExpiredException(message);
        TokenExpiredException withMessageAndCause = new TokenExpiredException(message, cause);

        // Assert
        assertAll("Usporedba oba konstruktora",
            () -> assertEquals(message, withMessageOnly.getMessage()),
            () -> assertEquals(message, withMessageAndCause.getMessage()),
            () -> assertNull(withMessageOnly.getCause()),
            () -> assertNotNull(withMessageAndCause.getCause()),
            () -> assertEquals(cause, withMessageAndCause.getCause())
        );
    }

    @Test
    @DisplayName("Simulacija real-world JWT expiration scenarija")
    void testRealWorldJwtExpirationScenario() {
        // Arrange
        String message = "JWT token istekao nakon 24 sata";
        io.jsonwebtoken.ExpiredJwtException jwtCause = new io.jsonwebtoken.ExpiredJwtException(
            null, null, "JWT expired at 2024-12-20T15:00:00Z"
        );

        // Act
        TokenExpiredException exception = new TokenExpiredException(message, jwtCause);

        // Assert
        assertAll("Real-world JWT expiration",
            () -> assertEquals(message, exception.getMessage()),
            () -> assertInstanceOf(io.jsonwebtoken.ExpiredJwtException.class, exception.getCause()),
            () -> assertTrue(exception.getCause().getMessage().contains("JWT expired"))
        );
    }
}
