package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testovi za ConflictException.
 * Testira konstruktore, ResponseStatus anotaciju i osnovne funkcionalnosti.
 */
@DisplayName("ConflictException Tests")
class ConflictExceptionTest {

    @Test
    @DisplayName("Konstruktor s porukom - provjera poruke")
    void testConstructorWithMessage_MessageIsCorrect() {
        // Arrange
        String expectedMessage = "Resurs već postoji u sustavu";

        // Act
        ConflictException exception = new ConflictException(expectedMessage);

        // Assert
        assertEquals(expectedMessage, exception.getMessage(), 
            "Poruka iznimke bi trebala biti identična predanoj poruci");
    }

    @Test
    @DisplayName("Iznimka je RuntimeException")
    void testExceptionIsRuntimeException() {
        // Arrange & Act
        ConflictException exception = new ConflictException("Test message");

        // Assert
        assertInstanceOf(RuntimeException.class, exception, 
            "ConflictException bi trebala biti podtip RuntimeException");
    }

    @Test
    @DisplayName("ResponseStatus anotacija je prisutna")
    void testResponseStatusAnnotationPresent() {
        // Act
        ResponseStatus annotation = ConflictException.class.getAnnotation(ResponseStatus.class);

        // Assert
        assertNotNull(annotation, 
            "@ResponseStatus anotacija bi trebala biti prisutna na klasi");
    }

    @Test
    @DisplayName("ResponseStatus ima HTTP status CONFLICT (409)")
    void testResponseStatusIsConflict() {
        // Act
        ResponseStatus annotation = ConflictException.class.getAnnotation(ResponseStatus.class);

        // Assert
        assertEquals(HttpStatus.CONFLICT, annotation.value(), 
            "HTTP status bi trebao biti 409 CONFLICT");
    }

    @Test
    @DisplayName("Bacanje i hvatanje iznimke")
    void testThrowingAndCatchingException() {
        // Arrange
        String expectedMessage = "Konflikt: entitet već postoji";

        // Act & Assert
        ConflictException exception = assertThrows(
            ConflictException.class,
            () -> {
                throw new ConflictException(expectedMessage);
            },
            "Iznimka bi trebala biti bacena i uhvaćena"
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Null poruka - provjera ponašanja")
    void testNullMessage() {
        // Act
        ConflictException exception = new ConflictException(null);

        // Assert
        assertNull(exception.getMessage(), 
            "Poruka bi trebala biti null ako je null predana");
    }

    @Test
    @DisplayName("Prazna poruka")
    void testEmptyMessage() {
        // Arrange
        String emptyMessage = "";

        // Act
        ConflictException exception = new ConflictException(emptyMessage);

        // Assert
        assertEquals(emptyMessage, exception.getMessage(), 
            "Prazna poruka bi trebala biti prihvaćena");
        assertTrue(exception.getMessage().isEmpty(), 
            "Poruka bi trebala biti prazna");
    }

    @Test
    @DisplayName("Duga poruka - provjera podrške")
    void testLongMessage() {
        // Arrange
        String longMessage = "A".repeat(1000); // 1000 karaktera

        // Act
        ConflictException exception = new ConflictException(longMessage);

        // Assert
        assertEquals(longMessage, exception.getMessage(), 
            "Duga poruka bi trebala biti ispravno pohranjena");
        assertEquals(1000, exception.getMessage().length(), 
            "Duljina poruke bi trebala biti 1000 karaktera");
    }

    @Test
    @DisplayName("Specijalni znakovi u poruci")
    void testSpecialCharactersInMessage() {
        // Arrange
        String messageWithSpecialChars = "Greška: UTF-8 znakovi ćčđšž, emoji 🚀, simboli @#$%";

        // Act
        ConflictException exception = new ConflictException(messageWithSpecialChars);

        // Assert
        assertEquals(messageWithSpecialChars, exception.getMessage(), 
            "Poruka sa specijalnim znakovima bi trebala biti ispravno pohranjena");
    }

    @Test
    @DisplayName("Višelinijska poruka")
    void testMultiLineMessage() {
        // Arrange
        String multiLineMessage = "Prva linija\nDruga linija\nTreća linija";

        // Act
        ConflictException exception = new ConflictException(multiLineMessage);

        // Assert
        assertEquals(multiLineMessage, exception.getMessage(), 
            "Višelinijska poruka bi trebala biti ispravno pohranjena");
        assertTrue(exception.getMessage().contains("\n"), 
            "Poruka bi trebala sadržavati nove retke");
    }

    @Test
    @DisplayName("Stack trace se ispravno generira")
    void testStackTraceGeneration() {
        // Arrange
        String message = "Test message";

        // Act
        ConflictException exception = new ConflictException(message);

        // Assert
        assertNotNull(exception.getStackTrace(), 
            "Stack trace ne bi trebao biti null");
        assertTrue(exception.getStackTrace().length > 0, 
            "Stack trace bi trebao imati elemente");
    }

    @Test
    @DisplayName("Testiranje toString metode")
    void testToStringMethod() {
        // Arrange
        String message = "Conflict occurred";
        ConflictException exception = new ConflictException(message);

        // Act
        String result = exception.toString();

        // Assert
        assertNotNull(result, "toString ne bi trebao vraćati null");
        assertTrue(result.contains("ConflictException"), 
            "toString bi trebao sadržavati ime klase");
        assertTrue(result.contains(message), 
            "toString bi trebao sadržavati poruku");
    }

    @Test
    @DisplayName("Različiti scenariji poruka")
    void testDifferentMessageScenarios() {
        // Arrange & Act
        ConflictException duplicateUser = new ConflictException(
            "Korisnik s email adresom već postoji"
        );
        ConflictException duplicateVehicle = new ConflictException(
            "Vozilo s registracijom XY-1234-AB već postoji"
        );
        ConflictException duplicateShipment = new ConflictException(
            "Pošiljka s tracking brojem već postoji"
        );

        // Assert
        assertAll("Različiti scenariji konfliktnih poruka",
            () -> assertNotNull(duplicateUser.getMessage()),
            () -> assertNotNull(duplicateVehicle.getMessage()),
            () -> assertNotNull(duplicateShipment.getMessage()),
            () -> assertTrue(duplicateUser.getMessage().contains("email")),
            () -> assertTrue(duplicateVehicle.getMessage().contains("registracijom")),
            () -> assertTrue(duplicateShipment.getMessage().contains("tracking"))
        );
    }

    @Test
    @DisplayName("Cause je null (ConflictException nema cause konstruktor)")
    void testCauseIsNull() {
        // Arrange
        String message = "Test message";

        // Act
        ConflictException exception = new ConflictException(message);

        // Assert
        assertNull(exception.getCause(), 
            "Cause bi trebao biti null jer nema konstruktora sa cause parametrom");
    }
}
