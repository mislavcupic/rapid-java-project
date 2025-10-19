package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.dao.DataIntegrityViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 🎯 Hvata greške u vezi s UNIQUE/PRIMARY KEY (kao na vašoj drugoj slici)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Object> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Data Conflict");

        // Klijentu šaljemo samo čistu poruku
        body.put("message", "Greška u podacima: Entitet s ovim ID-om ili ključem već postoji. Provjerite Tracking No. ili ID.");

        return new ResponseEntity<>(body, HttpStatus.CONFLICT); // HTTP 409 Conflict
    }

    // 🎯 Hvata sve ostale nekontrolirane 500 greške (kao na vašoj prvoj slici)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex) {

        // Ovdje se SAMO logira puni trace (za vas), ali se NE šalje klijentu.
        // System.err.println("INTERNAL SERVER ERROR: " + ex); // Koristite logger umjesto System.err

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        // Klijentu šaljemo samo generičku poruku
        body.put("message", "Došlo je do neočekivane greške na serveru. Molimo pokušajte ponovno.");

        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
