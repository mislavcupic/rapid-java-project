package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testovi za DuplicateResourceException.
 */
@DisplayName("DuplicateResourceException Tests")
class DuplicateResourceExceptionTest {

    @Test
    @DisplayName("Konstruktor s porukom - provjera poruke")
    void testConstructorWithMessage_MessageIsCorrect() {
        String expectedMessage = "Duplikat: resurs već postoji u bazi";

        // Ispravljeno: Proslijeđeni fiksni stringovi umjesto nepostojećeg 'request' objekta
        DuplicateResourceException exception = new DuplicateResourceException(expectedMessage);

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Iznimka je RuntimeException")
    void testExceptionIsRuntimeException() {
        DuplicateResourceException exception = new DuplicateResourceException("Test message");
        assertInstanceOf(RuntimeException.class, exception);
    }

    @Test
    @DisplayName("ResponseStatus anotacija je prisutna")
    void testResponseStatusAnnotationPresent() {
        ResponseStatus annotation = DuplicateResourceException.class.getAnnotation(ResponseStatus.class);
        assertNotNull(annotation, "@ResponseStatus anotacija bi trebala biti prisutna na klasi");
    }

    @Test
    @DisplayName("ResponseStatus ima HTTP status CONFLICT (409)")
    void testResponseStatusIsConflict() {
        ResponseStatus annotation = DuplicateResourceException.class.getAnnotation(ResponseStatus.class);
        assertEquals(HttpStatus.CONFLICT, annotation.value(), "HTTP status bi trebao biti 409 CONFLICT");
    }

    @Test
    @DisplayName("Bacanje i hvatanje iznimke")
    void testThrowingAndCatchingException() {
        String expectedMessage = "Vozilo s registracijom već postoji";

        DuplicateResourceException exception = assertThrows(
                DuplicateResourceException.class,
                () -> {
                    throw new DuplicateResourceException(expectedMessage);
                }
        );

        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Null poruka - provjera ponašanja")
    void testNullMessage() {
        DuplicateResourceException exception = new DuplicateResourceException(null);
        assertNull(exception.getMessage());
    }

    @Test
    @DisplayName("Prazna poruka")
    void testEmptyMessage() {
        String emptyMessage = "";
        DuplicateResourceException exception = new DuplicateResourceException(emptyMessage);
        assertEquals(emptyMessage, exception.getMessage());
    }

    @Test
    @DisplayName("Scenariji duplikata za različite resurse")
    void testDifferentDuplicateResourceScenarios() {
        DuplicateResourceException duplicateEmail = new DuplicateResourceException(
                "Korisnik s email adresom test@example.com već postoji");
        DuplicateResourceException duplicatePlate = new DuplicateResourceException(
                "Vozilo s registracijom ZG-1234-AB već postoji u sustavu");

        assertAll("Različiti scenariji duplikata resursa",
                () -> assertTrue(duplicateEmail.getMessage().contains("email")),
                () -> assertTrue(duplicatePlate.getMessage().contains("registracijom"))
        );
    }

    @Test
    @DisplayName("Usporedba s ConflictException - obje imaju isti HTTP status")
    void testSameHttpStatusAsConflictException() {
        ResponseStatus duplicateAnnotation = DuplicateResourceException.class.getAnnotation(ResponseStatus.class);
        ResponseStatus conflictAnnotation = ConflictException.class.getAnnotation(ResponseStatus.class);

        assertNotNull(duplicateAnnotation);
        assertNotNull(conflictAnnotation);
        assertEquals(duplicateAnnotation.value(), conflictAnnotation.value());
        assertEquals(HttpStatus.CONFLICT, duplicateAnnotation.value());
    }
}