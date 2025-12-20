package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testovi za GlobalExceptionHandler.
 * Testira sve exception handlere i provjera odgovora.
 */
@DisplayName("GlobalExceptionHandler Tests")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    // ===== DataIntegrityViolationException Tests =====

    @Test
    @DisplayName("handleDataIntegrityViolation - provjera HTTP statusa")
    void testHandleDataIntegrityViolation_HttpStatus() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Unique constraint violation"
        );

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(exception);

        // Assert
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode(), 
            "HTTP status bi trebao biti 409 CONFLICT");
    }

    @Test
    @DisplayName("handleDataIntegrityViolation - provjera strukture odgovora")
    void testHandleDataIntegrityViolation_ResponseStructure() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Duplicate key error"
        );

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        assertNotNull(body, "Response body ne bi trebao biti null");
        assertTrue(body.containsKey("timestamp"), "Response bi trebao sadržavati timestamp");
        assertTrue(body.containsKey("status"), "Response bi trebao sadržavati status");
        assertTrue(body.containsKey("error"), "Response bi trebao sadržavati error");
        assertTrue(body.containsKey("message"), "Response bi trebao sadržavati message");
    }

    @Test
    @DisplayName("handleDataIntegrityViolation - provjera timestamp vrijednosti")
    void testHandleDataIntegrityViolation_TimestampValue() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Constraint violation"
        );
        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        LocalDateTime timestamp = (LocalDateTime) body.get("timestamp");
        assertNotNull(timestamp, "Timestamp ne bi trebao biti null");
        assertTrue(!timestamp.isBefore(beforeCall) && !timestamp.isAfter(afterCall), 
            "Timestamp bi trebao biti između prije i poslije poziva");
    }

    @Test
    @DisplayName("handleDataIntegrityViolation - provjera status polja")
    void testHandleDataIntegrityViolation_StatusField() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Primary key violation"
        );

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        assertEquals(409, body.get("status"), 
            "Status polje bi trebalo biti 409");
    }

    @Test
    @DisplayName("handleDataIntegrityViolation - provjera error polja")
    void testHandleDataIntegrityViolation_ErrorField() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Foreign key constraint"
        );

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        assertEquals("Data Conflict", body.get("error"), 
            "Error polje bi trebalo biti 'Data Conflict'");
    }

    @Test
    @DisplayName("handleDataIntegrityViolation - provjera message polja")
    void testHandleDataIntegrityViolation_MessageField() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Unique index violation on tracking_number"
        );

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        String message = (String) body.get("message");
        assertNotNull(message, "Message ne bi trebao biti null");
        assertTrue(message.contains("Greška u podacima"), 
            "Message bi trebao sadržavati 'Greška u podacima'");
        assertFalse(message.contains("tracking_number"), 
            "Message NE bi trebao otkrivati detalje poput naziva polja (sigurnost)");
    }

    @Test
    @DisplayName("handleDataIntegrityViolation - generička poruka za klijenta")
    void testHandleDataIntegrityViolation_GenericClientMessage() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Very detailed technical SQL error that should not be exposed to client"
        );

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        String message = (String) body.get("message");
        String expectedMessage = "Greška u podacima: Entitet s ovim ID-om ili ključem već postoji. Provjerite Tracking No. ili ID.";
        assertEquals(expectedMessage, message, 
            "Poruka bi trebala biti generička i sigurna za klijenta");
    }

    // ===== General Exception Tests =====

    @Test
    @DisplayName("handleAllExceptions - provjera HTTP statusa")
    void testHandleAllExceptions_HttpStatus() {
        // Arrange
        Exception exception = new Exception("Unexpected error");

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), 
            "HTTP status bi trebao biti 500 INTERNAL_SERVER_ERROR");
    }

    @Test
    @DisplayName("handleAllExceptions - provjera strukture odgovora")
    void testHandleAllExceptions_ResponseStructure() {
        // Arrange
        Exception exception = new Exception("Server error");

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        assertNotNull(body, "Response body ne bi trebao biti null");
        assertTrue(body.containsKey("timestamp"), "Response bi trebao sadržavati timestamp");
        assertTrue(body.containsKey("status"), "Response bi trebao sadržavati status");
        assertTrue(body.containsKey("error"), "Response bi trebao sadržavati error");
        assertTrue(body.containsKey("message"), "Response bi trebao sadržavati message");
    }

    @Test
    @DisplayName("handleAllExceptions - provjera status polja")
    void testHandleAllExceptions_StatusField() {
        // Arrange
        Exception exception = new RuntimeException("Runtime error");

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        assertEquals(500, body.get("status"), 
            "Status polje bi trebalo biti 500");
    }

    @Test
    @DisplayName("handleAllExceptions - provjera error polja")
    void testHandleAllExceptions_ErrorField() {
        // Arrange
        Exception exception = new NullPointerException("Null pointer");

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        assertEquals("Internal Server Error", body.get("error"), 
            "Error polje bi trebalo biti 'Internal Server Error'");
    }

    @Test
    @DisplayName("handleAllExceptions - generička poruka za klijenta")
    void testHandleAllExceptions_GenericClientMessage() {
        // Arrange
        Exception exception = new Exception(
            "Very detailed stack trace with sensitive information that should never reach the client"
        );

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        String message = (String) body.get("message");
        String expectedMessage = "Došlo je do neočekivane greške na serveru. Molimo pokušajte ponovno.";
        assertEquals(expectedMessage, message, 
            "Poruka bi trebala biti generička i NE bi trebala otkrivati tehničke detalje");
    }

    @Test
    @DisplayName("handleAllExceptions - timestamp vrijednost")
    void testHandleAllExceptions_TimestampValue() {
        // Arrange
        Exception exception = new IllegalStateException("Illegal state");
        LocalDateTime beforeCall = LocalDateTime.now();

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        LocalDateTime afterCall = LocalDateTime.now();

        // Assert
        LocalDateTime timestamp = (LocalDateTime) body.get("timestamp");
        assertNotNull(timestamp, "Timestamp ne bi trebao biti null");
        assertTrue(!timestamp.isBefore(beforeCall) && !timestamp.isAfter(afterCall), 
            "Timestamp bi trebao biti između prije i poslije poziva");
    }

    // ===== Različite vrste iznimki =====

    @Test
    @DisplayName("handleAllExceptions - različite vrste RuntimeException")
    void testHandleAllExceptions_DifferentRuntimeExceptions() {
        // Arrange & Act
        ResponseEntity<Object> nullPointerResponse = exceptionHandler.handleAllExceptions(
            new NullPointerException("Null pointer")
        );
        ResponseEntity<Object> illegalArgumentResponse = exceptionHandler.handleAllExceptions(
            new IllegalArgumentException("Illegal argument")
        );
        ResponseEntity<Object> illegalStateResponse = exceptionHandler.handleAllExceptions(
            new IllegalStateException("Illegal state")
        );

        // Assert
        assertAll("Različite RuntimeException bi trebale vraćati isti format",
            () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, nullPointerResponse.getStatusCode()),
            () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, illegalArgumentResponse.getStatusCode()),
            () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, illegalStateResponse.getStatusCode())
        );
    }

    @Test
    @DisplayName("handleAllExceptions - checked exceptions")
    void testHandleAllExceptions_CheckedExceptions() {
        // Arrange
        Exception checkedException = new Exception("Checked exception");

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(checkedException);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode(), 
            "Checked exceptions bi trebale biti obrađene kao 500 errors");
    }

    // ===== Sigurnost - ne otkrivanje osjetljivih informacija =====

    @Test
    @DisplayName("Sigurnost - DataIntegrityViolation ne otkriva SQL detalje")
    void testSecurity_DataIntegrityDoesNotExposeSqlDetails() {
        // Arrange
        String sensitiveMessage = "SQL Error: INSERT INTO users (email) VALUES ('test@example.com') " +
                                  "failed with constraint violation on unique_email_idx";
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            sensitiveMessage
        );

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        String clientMessage = (String) body.get("message");

        // Assert
        assertFalse(clientMessage.contains("SQL"), 
            "Message ne bi trebao sadržavati 'SQL'");
        assertFalse(clientMessage.contains("INSERT"), 
            "Message ne bi trebao sadržavati SQL naredbe");
        assertFalse(clientMessage.contains("test@example.com"), 
            "Message ne bi trebao sadržavati korisničke podatke");
        assertFalse(clientMessage.contains("unique_email_idx"), 
            "Message ne bi trebao sadržavati nazive baza podataka");
    }

    @Test
    @DisplayName("Sigurnost - General exception ne otkriva stack trace")
    void testSecurity_GeneralExceptionDoesNotExposeStackTrace() {
        // Arrange
        Exception exception = new RuntimeException(
            "Error in UserService.createUser() at line 42: NullPointerException in password validation"
        );

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        String clientMessage = (String) body.get("message");

        // Assert
        assertFalse(clientMessage.contains("UserService"), 
            "Message ne bi trebao sadržavati nazive klasa");
        assertFalse(clientMessage.contains("line 42"), 
            "Message ne bi trebao sadržavati detalje o lokaciji greške");
        assertFalse(clientMessage.contains("password"), 
            "Message ne bi trebao sadržavati osjetljive pojmove");
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Edge case - null exception message")
    void testEdgeCase_NullExceptionMessage() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException(null);

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        assertNotNull(body.get("message"), 
            "Client message ne bi trebao biti null čak i kad je exception message null");
    }

    @Test
    @DisplayName("Edge case - prazna exception poruka")
    void testEdgeCase_EmptyExceptionMessage() {
        // Arrange
        Exception exception = new Exception("");

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        assertNotNull(body.get("message"), 
            "Client message ne bi trebao biti null");
        assertFalse(((String) body.get("message")).isEmpty(), 
            "Client message ne bi trebao biti prazan");
    }

    // ===== Response Body Format Tests =====

    @Test
    @DisplayName("Response body - LinkedHashMap održava redoslijed polja")
    void testResponseBody_MaintainsFieldOrder() {
        // Arrange
        DataIntegrityViolationException exception = new DataIntegrityViolationException(
            "Test violation"
        );

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleDataIntegrityViolation(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        assertNotNull(body, "Body ne bi trebao biti null");
        // LinkedHashMap održava insertion order
        String[] expectedOrder = {"timestamp", "status", "error", "message"};
        String[] actualOrder = body.keySet().toArray(new String[0]);
        assertArrayEquals(expectedOrder, actualOrder, 
            "Polja u response body bi trebala biti u očekivanom redoslijedu");
    }

    @Test
    @DisplayName("Response body - sva polja su ispravnih tipova")
    void testResponseBody_CorrectFieldTypes() {
        // Arrange
        Exception exception = new Exception("Test");

        // Act
        ResponseEntity<Object> response = exceptionHandler.handleAllExceptions(exception);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        // Assert
        assertAll("Provjera tipova polja",
            () -> assertInstanceOf(LocalDateTime.class, body.get("timestamp")),
            () -> assertInstanceOf(Integer.class, body.get("status")),
            () -> assertInstanceOf(String.class, body.get("error")),
            () -> assertInstanceOf(String.class, body.get("message"))
        );
    }

    // ===== Integration-like Tests =====

    @Test
    @DisplayName("Simulacija različitih scenarija - CONFLICT errors")
    void testSimulateConflictScenarios() {
        // Arrange
        DataIntegrityViolationException duplicateEmail = new DataIntegrityViolationException(
            "Duplicate entry 'user@example.com' for key 'users.email_unique'"
        );
        DataIntegrityViolationException duplicatePlate = new DataIntegrityViolationException(
            "Duplicate entry 'ZG-1234-AB' for key 'vehicles.registration_unique'"
        );

        // Act
        ResponseEntity<Object> emailResponse = exceptionHandler.handleDataIntegrityViolation(duplicateEmail);
        ResponseEntity<Object> plateResponse = exceptionHandler.handleDataIntegrityViolation(duplicatePlate);

        // Assert
        assertAll("Oba CONFLICT scenarija bi trebala biti konzistentno obrađena",
            () -> assertEquals(HttpStatus.CONFLICT, emailResponse.getStatusCode()),
            () -> assertEquals(HttpStatus.CONFLICT, plateResponse.getStatusCode()),
            () -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> emailBody = (Map<String, Object>) emailResponse.getBody();
                @SuppressWarnings("unchecked")
                Map<String, Object> plateBody = (Map<String, Object>) plateResponse.getBody();
                assertEquals(emailBody.get("message"), plateBody.get("message"), 
                    "Obje poruke bi trebale biti identične (generička poruka)");
            }
        );
    }

    @Test
    @DisplayName("Simulacija različitih scenarija - INTERNAL_SERVER_ERROR")
    void testSimulateInternalServerErrorScenarios() {
        // Arrange
        NullPointerException npe = new NullPointerException("Cannot invoke method on null");
        IllegalStateException ise = new IllegalStateException("Service is in invalid state");
        RuntimeException re = new RuntimeException("Unexpected runtime error");

        // Act
        ResponseEntity<Object> npeResponse = exceptionHandler.handleAllExceptions(npe);
        ResponseEntity<Object> iseResponse = exceptionHandler.handleAllExceptions(ise);
        ResponseEntity<Object> reResponse = exceptionHandler.handleAllExceptions(re);

        // Assert
        assertAll("Svi INTERNAL_SERVER_ERROR scenariji bi trebali biti konzistentno obrađeni",
            () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, npeResponse.getStatusCode()),
            () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, iseResponse.getStatusCode()),
            () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, reResponse.getStatusCode()),
            () -> {
                @SuppressWarnings("unchecked")
                Map<String, Object> npeBody = (Map<String, Object>) npeResponse.getBody();
                @SuppressWarnings("unchecked")
                Map<String, Object> iseBody = (Map<String, Object>) iseResponse.getBody();
                @SuppressWarnings("unchecked")
                Map<String, Object> reBody = (Map<String, Object>) reResponse.getBody();
                
                assertEquals(npeBody.get("message"), iseBody.get("message"), 
                    "Sve poruke bi trebale biti identične (generička poruka)");
                assertEquals(iseBody.get("message"), reBody.get("message"), 
                    "Sve poruke bi trebale biti identične (generička poruka)");
            }
        );
    }
}
