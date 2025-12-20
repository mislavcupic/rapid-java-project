package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit testovi za ResourceNotFoundException.
 * Testira konstruktore, gettere, ResponseStatus anotaciju i sve funkcionalnosti.
 */
@DisplayName("ResourceNotFoundException Tests")
class ResourceNotFoundExceptionTest {

    @Test
    @DisplayName("Konstruktor s tri parametra - provjera poruke")
    void testThreeParameterConstructor_MessageIsCorrect() {
        // Arrange
        String resourceName = "Vozilo";
        String fieldName = "ID";
        Object fieldValue = 123L;

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(
            resourceName, fieldName, fieldValue
        );

        // Assert
        String expectedMessage = "Vozilo nije pronađen s ID : '123'";
        assertEquals(expectedMessage, exception.getMessage(), 
            "Poruka bi trebala biti formirana prema predlošku");
    }

    @Test
    @DisplayName("Konstruktor - provjera resourceName gettera")
    void testGetResourceName() {
        // Arrange
        String expectedResourceName = "Vozač";
        ResourceNotFoundException exception = new ResourceNotFoundException(
            expectedResourceName, "OIB", "12345678901"
        );

        // Act
        String actualResourceName = exception.getResourceName();

        // Assert
        assertEquals(expectedResourceName, actualResourceName, 
            "getResourceName() bi trebao vratiti točan naziv resursa");
    }

    @Test
    @DisplayName("Konstruktor - provjera fieldName gettera")
    void testGetFieldName() {
        // Arrange
        String expectedFieldName = "email";
        ResourceNotFoundException exception = new ResourceNotFoundException(
            "Korisnik", expectedFieldName, "test@example.com"
        );

        // Act
        String actualFieldName = exception.getFieldName();

        // Assert
        assertEquals(expectedFieldName, actualFieldName, 
            "getFieldName() bi trebao vratiti točan naziv polja");
    }

    @Test
    @DisplayName("Konstruktor - provjera fieldValue gettera")
    void testGetFieldValue() {
        // Arrange
        Object expectedFieldValue = 999L;
        ResourceNotFoundException exception = new ResourceNotFoundException(
            "Pošiljka", "ID", expectedFieldValue
        );

        // Act
        Object actualFieldValue = exception.getFieldValue();

        // Assert
        assertEquals(expectedFieldValue, actualFieldValue, 
            "getFieldValue() bi trebao vratiti točnu vrijednost polja");
    }

    @Test
    @DisplayName("Iznimka je RuntimeException")
    void testExceptionIsRuntimeException() {
        // Arrange & Act
        ResourceNotFoundException exception = new ResourceNotFoundException(
            "Resurs", "ID", 1
        );

        // Assert
        assertInstanceOf(RuntimeException.class, exception, 
            "ResourceNotFoundException bi trebala biti podtip RuntimeException");
    }

    @Test
    @DisplayName("ResponseStatus anotacija je prisutna")
    void testResponseStatusAnnotationPresent() {
        // Act
        ResponseStatus annotation = ResourceNotFoundException.class
            .getAnnotation(ResponseStatus.class);

        // Assert
        assertNotNull(annotation, 
            "@ResponseStatus anotacija bi trebala biti prisutna na klasi");
    }

    @Test
    @DisplayName("ResponseStatus ima HTTP status NOT_FOUND (404)")
    void testResponseStatusIsNotFound() {
        // Act
        ResponseStatus annotation = ResourceNotFoundException.class
            .getAnnotation(ResponseStatus.class);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, annotation.value(), 
            "HTTP status bi trebao biti 404 NOT_FOUND");
    }

    @Test
    @DisplayName("Bacanje i hvatanje iznimke")
    void testThrowingAndCatchingException() {
        // Arrange
        String resourceName = "Vozilo";
        String fieldName = "registracija";
        String fieldValue = "ZG-1234-AB";

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> {
                throw new ResourceNotFoundException(resourceName, fieldName, fieldValue);
            },
            "Iznimka bi trebala biti bacena i uhvaćena"
        );

        assertTrue(exception.getMessage().contains(resourceName));
        assertTrue(exception.getMessage().contains(fieldName));
        assertTrue(exception.getMessage().contains(fieldValue));
    }

    @Test
    @DisplayName("Formatiranje poruke za Long ID")
    void testMessageFormattingForLongId() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException(
            "Driver", "ID", 42L
        );

        // Act
        String message = exception.getMessage();

        // Assert
        assertEquals("Driver nije pronađen s ID : '42'", message, 
            "Poruka bi trebala biti ispravno formirana za Long ID");
    }

    @Test
    @DisplayName("Formatiranje poruke za String vrijednost")
    void testMessageFormattingForStringValue() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException(
            "Korisnik", "username", "john_doe"
        );

        // Act
        String message = exception.getMessage();

        // Assert
        assertEquals("Korisnik nije pronađen s username : 'john_doe'", message, 
            "Poruka bi trebala biti ispravno formirana za String vrijednost");
    }

    @Test
    @DisplayName("Različiti tipovi resursa")
    void testDifferentResourceTypes() {
        // Arrange & Act
        ResourceNotFoundException vehicleNotFound = new ResourceNotFoundException(
            "Vozilo", "ID", 1L
        );
        ResourceNotFoundException driverNotFound = new ResourceNotFoundException(
            "Vozač", "OIB", "12345678901"
        );
        ResourceNotFoundException shipmentNotFound = new ResourceNotFoundException(
            "Pošiljka", "trackingNumber", "TRACK123"
        );
        ResourceNotFoundException userNotFound = new ResourceNotFoundException(
            "Korisnik", "email", "user@example.com"
        );

        // Assert
        assertAll("Različiti tipovi resursa",
            () -> assertEquals("Vozilo", vehicleNotFound.getResourceName()),
            () -> assertEquals("Vozač", driverNotFound.getResourceName()),
            () -> assertEquals("Pošiljka", shipmentNotFound.getResourceName()),
            () -> assertEquals("Korisnik", userNotFound.getResourceName())
        );
    }

    @Test
    @DisplayName("Null vrijednosti u konstruktoru")
    void testNullValuesInConstructor() {
        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(
            null, null, null
        );

        // Assert
        assertAll("Null vrijednosti",
            () -> assertNull(exception.getResourceName()),
            () -> assertNull(exception.getFieldName()),
            () -> assertNull(exception.getFieldValue()),
            () -> assertTrue(exception.getMessage().contains("null"))
        );
    }

    @Test
    @DisplayName("Prazni stringovi u konstruktoru")
    void testEmptyStringsInConstructor() {
        // Arrange
        String emptyString = "";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(
            emptyString, emptyString, emptyString
        );

        // Assert
        assertAll("Prazni stringovi",
            () -> assertEquals("", exception.getResourceName()),
            () -> assertEquals("", exception.getFieldName()),
            () -> assertEquals("", exception.getFieldValue()),
            () -> assertNotNull(exception.getMessage())
        );
    }

    @Test
    @DisplayName("Stack trace se ispravno generira")
    void testStackTraceGeneration() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException(
            "Vozilo", "ID", 123
        );

        // Assert
        assertNotNull(exception.getStackTrace(), 
            "Stack trace ne bi trebao biti null");
        assertTrue(exception.getStackTrace().length > 0, 
            "Stack trace bi trebao imati elemente");
    }

    @Test
    @DisplayName("ToString metoda")
    void testToStringMethod() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException(
            "Vozilo", "ID", 999L
        );

        // Act
        String result = exception.toString();

        // Assert
        assertNotNull(result, "toString ne bi trebao vraćati null");
        assertTrue(result.contains("ResourceNotFoundException"), 
            "toString bi trebao sadržavati ime klase");
        assertTrue(result.contains("Vozilo"), 
            "toString bi trebao sadržavati naziv resursa");
    }

    @Test
    @DisplayName("Verschiedene Feldtypen - Integer, Long, String")
    void testDifferentFieldValueTypes() {
        // Arrange & Act
        ResourceNotFoundException withInteger = new ResourceNotFoundException(
            "Resurs", "count", 42
        );
        ResourceNotFoundException withLong = new ResourceNotFoundException(
            "Resurs", "id", 123456789L
        );
        ResourceNotFoundException withString = new ResourceNotFoundException(
            "Resurs", "code", "ABC123"
        );
        ResourceNotFoundException withBoolean = new ResourceNotFoundException(
            "Resurs", "active", true
        );

        // Assert
        assertAll("Različiti tipovi vrijednosti polja",
            () -> assertEquals(42, withInteger.getFieldValue()),
            () -> assertEquals(123456789L, withLong.getFieldValue()),
            () -> assertEquals("ABC123", withString.getFieldValue()),
            () -> assertEquals(true, withBoolean.getFieldValue())
        );
    }

    @Test
    @DisplayName("Poruka s specijalnim znakovima u nazivima")
    void testMessageWithSpecialCharacters() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException(
            "Vozilo (Heavy)", "registracija (plate)", "ZG-ĆČ-123"
        );

        // Act
        String message = exception.getMessage();

        // Assert
        assertAll("Specijalni znakovi u poruci",
            () -> assertTrue(message.contains("Vozilo (Heavy)")),
            () -> assertTrue(message.contains("registracija (plate)")),
            () -> assertTrue(message.contains("ZG-ĆČ-123"))
        );
    }

    @Test
    @DisplayName("Immutabilnost fieldName i fieldValue (transient polja)")
    void testFieldsAreTransient() throws NoSuchFieldException {
        // Act
        boolean fieldNameIsTransient = java.lang.reflect.Modifier.isTransient(
            ResourceNotFoundException.class.getDeclaredField("fieldName").getModifiers()
        );
        boolean fieldValueIsTransient = java.lang.reflect.Modifier.isTransient(
            ResourceNotFoundException.class.getDeclaredField("fieldValue").getModifiers()
        );

        // Assert
        assertTrue(fieldNameIsTransient, 
            "fieldName bi trebao biti transient (ne serializira se)");
        assertTrue(fieldValueIsTransient, 
            "fieldValue bi trebao biti transient (ne serializira se)");
    }

    @Test
    @DisplayName("resourceName je final")
    void testResourceNameIsFinal() throws NoSuchFieldException {
        // Act
        boolean resourceNameIsFinal = java.lang.reflect.Modifier.isFinal(
            ResourceNotFoundException.class.getDeclaredField("resourceName").getModifiers()
        );

        // Assert
        assertTrue(resourceNameIsFinal, 
            "resourceName bi trebao biti final (ne može se promijeniti)");
    }

    @Test
    @DisplayName("Kompleksna poruka s više detalja")
    void testComplexMessageScenario() {
        // Arrange
        String complexResourceName = "Vozač (Professional Driver)";
        String complexFieldName = "OIB (Personal Identification Number)";
        String complexFieldValue = "12345678901";

        // Act
        ResourceNotFoundException exception = new ResourceNotFoundException(
            complexResourceName, complexFieldName, complexFieldValue
        );

        // Assert
        String expectedMessage = String.format(
            "%s nije pronađen s %s : '%s'", 
            complexResourceName, complexFieldName, complexFieldValue
        );
        assertEquals(expectedMessage, exception.getMessage(), 
            "Kompleksna poruka bi trebala biti ispravno formirana");
    }

    @Test
    @DisplayName("Testiranje s realističnim podacima iz aplikacije")
    void testWithRealisticApplicationData() {
        // Arrange & Act
        ResourceNotFoundException vehicleNotFound = new ResourceNotFoundException(
            "Vehicle", "registrationPlate", "ZG-1234-AB"
        );
        ResourceNotFoundException shipmentNotFound = new ResourceNotFoundException(
            "Shipment", "trackingNumber", "SHIP2024001"
        );
        ResourceNotFoundException assignmentNotFound = new ResourceNotFoundException(
            "Assignment", "assignmentId", 5L
        );

        // Assert
        assertAll("Realistični podaci iz aplikacije",
            () -> {
                assertEquals("Vehicle", vehicleNotFound.getResourceName());
                assertEquals("registrationPlate", vehicleNotFound.getFieldName());
                assertEquals("ZG-1234-AB", vehicleNotFound.getFieldValue());
            },
            () -> {
                assertEquals("Shipment", shipmentNotFound.getResourceName());
                assertEquals("trackingNumber", shipmentNotFound.getFieldName());
                assertEquals("SHIP2024001", shipmentNotFound.getFieldValue());
            },
            () -> {
                assertEquals("Assignment", assignmentNotFound.getResourceName());
                assertEquals("assignmentId", assignmentNotFound.getFieldName());
                assertEquals(5L, assignmentNotFound.getFieldValue());
            }
        );
    }
}
