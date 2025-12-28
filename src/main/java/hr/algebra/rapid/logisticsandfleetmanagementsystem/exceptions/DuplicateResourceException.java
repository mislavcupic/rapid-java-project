package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;

import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus automatski postavlja HTTP status kod 409 CONFLICT
// kada se ova iznimka baci iz kontrolera
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message, String trackingNumber, @NotBlank String number) {
        super(message);
    }


}