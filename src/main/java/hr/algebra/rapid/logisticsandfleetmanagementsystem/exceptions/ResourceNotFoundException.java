package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.Serializable;

/**
 * Custom iznimka koja se baca kada traženi resurs (poput Vozila ili Korisnika) nije pronađen.
 * @ResponseStatus(value = HttpStatus.NOT_FOUND) osigurava da Spring automatski
 * vraća HTTP status 404 kada se ova iznimka baci iz kontrolera.
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    /**
     * Konstruktor koji kreira opisnu poruku greške.
     * @param resourceName Naziv entiteta (npr. "Vozilo")
     * @param fieldName Naziv polja po kojem se tražilo (npr. "ID")
     * @param fieldValue Vrijednost koja je korištena za pretragu
     */
    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        // Kreiranje poruke: "Vozilo nije pronađeno s ID : '1'"
        super(String.format("%s nije pronađen s %s : '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public Object getFieldValue() {
        return fieldValue;
    }
}
