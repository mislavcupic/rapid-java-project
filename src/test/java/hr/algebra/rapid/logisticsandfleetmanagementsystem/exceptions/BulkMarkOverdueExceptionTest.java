package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testovi za BulkMarkOverdueException.
 * Testira sve konstruktore i osnovne funkcionalnosti iznimke.
 */
@DisplayName("BulkMarkOverdueException Tests")
class BulkMarkOverdueExceptionTest {

    @Test
    @DisplayName("Konstruktor s porukom i uzrokom - provjera poruke")
    void testConstructorWithMessageAndCause_MessageIsCorrect() {
        // Arrange
        String expectedMessage = "Greška prilikom bulk ažuriranja statusa na OVERDUE";
        Throwable cause = new RuntimeException("SQL Connection error");

        // Act
        BulkMarkOverdueException exception = new BulkMarkOverdueException(expectedMessage, cause);

        // Assert
        assertEquals(expectedMessage, exception.getMessage(), 
            "Poruka iznimke bi trebala biti identična predanoj poruci");
    }

    @Test
    @DisplayName("Konstruktor s porukom i uzrokom - provjera uzroka")
    void testConstructorWithMessageAndCause_CauseIsCorrect() {
        // Arrange
        String message = "Bulk operacija nije uspjela";
        Throwable expectedCause = new RuntimeException("Database timeout");

        // Act
        BulkMarkOverdueException exception = new BulkMarkOverdueException(message, expectedCause);

        // Assert
        assertSame(expectedCause, exception.getCause(), 
            "Uzrok iznimke bi trebao biti identičan predanom uzroku");
    }

    @Test
    @DisplayName("Iznimka je RuntimeException")
    void testExceptionIsRuntimeException() {
        // Arrange & Act
        BulkMarkOverdueException exception = new BulkMarkOverdueException(
            "Test message", 
            new Exception("Test cause")
        );

        // Assert
        assertInstanceOf(RuntimeException.class, exception, 
            "BulkMarkOverdueException bi trebala biti podtip RuntimeException");
    }

    @Test
    @DisplayName("Bacanje i hvatanje iznimke")
    void testThrowingAndCatchingException() {
        // Arrange
        String expectedMessage = "Kritična greška u bulk update operaciji";
        Throwable cause = new IllegalStateException("Nevaljan status");

        // Act & Assert
        BulkMarkOverdueException exception = assertThrows(
            BulkMarkOverdueException.class,
            () -> {
                throw new BulkMarkOverdueException(expectedMessage, cause);
            },
            "Iznimka bi trebala biti bacena i uhvaćena"
        );

        assertEquals(expectedMessage, exception.getMessage());
        assertEquals(cause, exception.getCause());
    }

    @Test
    @DisplayName("Null poruka - provjera ponašanja")
    void testNullMessage() {
        // Arrange
        Throwable cause = new RuntimeException("Cause");

        // Act
        BulkMarkOverdueException exception = new BulkMarkOverdueException(null, cause);

        // Assert
        assertNull(exception.getMessage(), 
            "Poruka bi trebala biti null ako je null predana");
        assertNotNull(exception.getCause(), 
            "Uzrok ne bi trebao biti null");
    }

    @Test
    @DisplayName("Null uzrok - provjera ponašanja")
    void testNullCause() {
        // Arrange
        String message = "Test poruka";

        // Act
        BulkMarkOverdueException exception = new BulkMarkOverdueException(message, null);

        // Assert
        assertNotNull(exception.getMessage(), 
            "Poruka ne bi trebala biti null");
        assertNull(exception.getCause(), 
            "Uzrok bi trebao biti null ako je null predan");
    }

    @Test
    @DisplayName("Stack trace se ispravno generira")
    void testStackTraceGeneration() {
        // Arrange
        String message = "Test message";
        Throwable cause = new RuntimeException("Original error");

        // Act
        BulkMarkOverdueException exception = new BulkMarkOverdueException(message, cause);

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
        Throwable rootCause = new IllegalArgumentException("Root problem");
        Throwable intermediateCause = new RuntimeException("Intermediate problem", rootCause);
        String message = "Bulk update failed";

        // Act
        BulkMarkOverdueException exception = new BulkMarkOverdueException(message, intermediateCause);

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
        BulkMarkOverdueException sqlException = new BulkMarkOverdueException(
            "SQL error", 
            new java.sql.SQLException("Connection failed")
        );
        
        BulkMarkOverdueException springException = new BulkMarkOverdueException(
            "Spring error", 
            new org.springframework.dao.DataAccessException("DAO error") {}
        );

        // Assert
        assertInstanceOf(java.sql.SQLException.class, sqlException.getCause(), 
            "SQLException bi trebao biti prihvaćen kao uzrok");
        assertInstanceOf(org.springframework.dao.DataAccessException.class, springException.getCause(), 
            "Spring DataAccessException bi trebao biti prihvaćen kao uzrok");
    }

    @Test
    @DisplayName("Testiranje toString metode")
    void testToStringMethod() {
        // Arrange
        String message = "Bulk operation failure";
        Throwable cause = new RuntimeException("Database error");
        BulkMarkOverdueException exception = new BulkMarkOverdueException(message, cause);

        // Act
        String result = exception.toString();

        // Assert
        assertNotNull(result, "toString ne bi trebao vraćati null");
        assertTrue(result.contains("BulkMarkOverdueException"), 
            "toString bi trebao sadržavati ime klase");
        assertTrue(result.contains(message), 
            "toString bi trebao sadržavati poruku");
    }
}
