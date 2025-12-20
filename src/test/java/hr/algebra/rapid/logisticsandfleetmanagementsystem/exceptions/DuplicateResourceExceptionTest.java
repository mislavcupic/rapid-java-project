package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testovi za DuplicateResourceException.
 * Testira konstruktore, ResponseStatus anotaciju i osnovne funkcionalnosti.
 */
@DisplayName("DuplicateResourceException Tests")
class DuplicateResourceExceptionTest {

    @Test
    @DisplayName("Konstruktor s porukom - provjera poruke")
    void testConstructorWithMessage_MessageIsCorrect() {
        // Arrange
        String expectedMessage = "Duplikat: resurs već postoji u bazi";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(expectedMessage);

        // Assert
        assertEquals(expectedMessage, exception.getMessage(), 
            "Poruka iznimke bi trebala biti identična predanoj poruci");
    }

    @Test
    @DisplayName("Iznimka je RuntimeException")
    void testExceptionIsRuntimeException() {
        // Arrange & Act
        DuplicateResourceException exception = new DuplicateResourceException("Test message");

        // Assert
        assertInstanceOf(RuntimeException.class, exception, 
            "DuplicateResourceException bi trebala biti podtip RuntimeException");
    }

    @Test
    @DisplayName("ResponseStatus anotacija je prisutna")
    void testResponseStatusAnnotationPresent() {
        // Act
        ResponseStatus annotation = DuplicateResourceException.class.getAnnotation(ResponseStatus.class);

        // Assert
        assertNotNull(annotation, 
            "@ResponseStatus anotacija bi trebala biti prisutna na klasi");
    }

    @Test
    @DisplayName("ResponseStatus ima HTTP status CONFLICT (409)")
    void testResponseStatusIsConflict() {
        // Act
        ResponseStatus annotation = DuplicateResourceException.class.getAnnotation(ResponseStatus.class);

        // Assert
        assertEquals(HttpStatus.CONFLICT, annotation.value(), 
            "HTTP status bi trebao biti 409 CONFLICT");
    }

    @Test
    @DisplayName("Bacanje i hvatanje iznimke")
    void testThrowingAndCatchingException() {
        // Arrange
        String expectedMessage = "Vozilo s registracijom već postoji";

        // Act & Assert
        DuplicateResourceException exception = assertThrows(
            DuplicateResourceException.class,
            () -> {
                throw new DuplicateResourceException(expectedMessage);
            },
            "Iznimka bi trebala biti bacena i uhvaćena"
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Null poruka - provjera ponašanja")
    void testNullMessage() {
        // Act
        DuplicateResourceException exception = new DuplicateResourceException(null);

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
        DuplicateResourceException exception = new DuplicateResourceException(emptyMessage);

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
        String message = "Duplicate resource found";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(message);

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
        String message = "Duplicate vehicle registration";
        DuplicateResourceException exception = new DuplicateResourceException(message);

        // Act
        String result = exception.toString();

        // Assert
        assertNotNull(result, "toString ne bi trebao vraćati null");
        assertTrue(result.contains("DuplicateResourceException"), 
            "toString bi trebao sadržavati ime klase");
        assertTrue(result.contains(message), 
            "toString bi trebao sadržavati poruku");
    }

    @Test
    @DisplayName("Scenariji duplikata za različite resurse")
    void testDifferentDuplicateResourceScenarios() {
        // Arrange & Act
        DuplicateResourceException duplicateEmail = new DuplicateResourceException(
            "Korisnik s email adresom test@example.com već postoji"
        );
        DuplicateResourceException duplicatePlate = new DuplicateResourceException(
            "Vozilo s registracijom ZG-1234-AB već postoji u sustavu"
        );
        DuplicateResourceException duplicateTracking = new DuplicateResourceException(
            "Pošiljka s tracking brojem TRACK123456 već postoji"
        );
        DuplicateResourceException duplicateOib = new DuplicateResourceException(
            "Vozač s OIB-om 12345678901 već postoji"
        );

        // Assert
        assertAll("Različiti scenariji duplikata resursa",
            () -> assertTrue(duplicateEmail.getMessage().contains("email")),
            () -> assertTrue(duplicatePlate.getMessage().contains("registracijom")),
            () -> assertTrue(duplicateTracking.getMessage().contains("tracking")),
            () -> assertTrue(duplicateOib.getMessage().contains("OIB"))
        );
    }

    @Test
    @DisplayName("Specijalni znakovi u poruci")
    void testSpecialCharactersInMessage() {
        // Arrange
        String messageWithSpecialChars = "Greška: Duplikat pronađen! Znakovi: ćčđšž, @#$%, 🚨";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(messageWithSpecialChars);

        // Assert
        assertEquals(messageWithSpecialChars, exception.getMessage(), 
            "Poruka sa specijalnim znakovima bi trebala biti ispravno pohranjena");
    }

    @Test
    @DisplayName("Višelinijska poruka s detaljima")
    void testMultiLineMessageWithDetails() {
        // Arrange
        String multiLineMessage = "Duplikat resursa!\n" +
                                  "Tip: Vozilo\n" +
                                  "Polje: Registracija\n" +
                                  "Vrijednost: ZG-1234-AB";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(multiLineMessage);

        // Assert
        assertEquals(multiLineMessage, exception.getMessage(), 
            "Višelinijska poruka s detaljima bi trebala biti ispravno pohranjena");
        assertTrue(exception.getMessage().contains("\n"), 
            "Poruka bi trebala sadržavati nove retke");
    }

    @Test
    @DisplayName("Duga poruka - provjera podrške")
    void testLongMessage() {
        // Arrange
        String longMessage = "Duplikat resursa: " + "A".repeat(500);

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(longMessage);

        // Assert
        assertEquals(longMessage, exception.getMessage(), 
            "Duga poruka bi trebala biti ispravno pohranjena");
        assertTrue(exception.getMessage().length() > 500, 
            "Duljina poruke bi trebala biti veća od 500 karaktera");
    }

    @Test
    @DisplayName("Poruka s ID-jevima i specifičnim vrijednostima")
    void testMessageWithIdsAndValues() {
        // Arrange
        String message = "Duplikat: Vozilo ID=123 s registracijom 'ZG-5678-CD' već postoji u bazi podataka";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(message);

        // Assert
        assertAll("Poruka s ID-jevima i vrijednostima",
            () -> assertTrue(exception.getMessage().contains("ID=123")),
            () -> assertTrue(exception.getMessage().contains("ZG-5678-CD")),
            () -> assertTrue(exception.getMessage().contains("bazi podataka"))
        );
    }

    @Test
    @DisplayName("Cause je null (DuplicateResourceException nema cause konstruktor)")
    void testCauseIsNull() {
        // Arrange
        String message = "Duplicate resource";

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(message);

        // Assert
        assertNull(exception.getCause(), 
            "Cause bi trebao biti null jer nema konstruktora sa cause parametrom");
    }

    @Test
    @DisplayName("Usporedba s ConflictException - obje imaju isti HTTP status")
    void testSameHttpStatusAsConflictException() {
        // Act
        ResponseStatus duplicateAnnotation = DuplicateResourceException.class
            .getAnnotation(ResponseStatus.class);
        ResponseStatus conflictAnnotation = ConflictException.class
            .getAnnotation(ResponseStatus.class);

        // Assert
        assertEquals(duplicateAnnotation.value(), conflictAnnotation.value(), 
            "DuplicateResourceException i ConflictException bi trebali imati isti HTTP status");
        assertEquals(HttpStatus.CONFLICT, duplicateAnnotation.value(), 
            "Obje iznimke bi trebale koristiti HTTP 409 CONFLICT");
    }

    @Test
    @DisplayName("Formatirana poruka s parametrima")
    void testFormattedMessageWithParameters() {
        // Arrange
        String resourceType = "Vozilo";
        String fieldName = "registracija";
        String fieldValue = "ZG-9999-XY";
        String formattedMessage = String.format(
            "Duplikat: %s s %s '%s' već postoji u sustavu", 
            resourceType, fieldName, fieldValue
        );

        // Act
        DuplicateResourceException exception = new DuplicateResourceException(formattedMessage);

        // Assert
        assertAll("Formatirana poruka s parametrima",
            () -> assertTrue(exception.getMessage().contains(resourceType)),
            () -> assertTrue(exception.getMessage().contains(fieldName)),
            () -> assertTrue(exception.getMessage().contains(fieldValue)),
            () -> assertEquals(formattedMessage, exception.getMessage())
        );
    }
}
