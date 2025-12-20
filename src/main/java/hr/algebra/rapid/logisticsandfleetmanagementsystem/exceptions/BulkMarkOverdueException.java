package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;

/**
 * Iznimka koja se baca ako dođe do kritične greške prilikom masovnog (bulk) ažuriranja
 * statusa pošiljaka na 'OVERDUE' u bazi podataka.
 */
public class BulkMarkOverdueException extends RuntimeException {

    /**
     * Konstruktor s porukom i uzrokom greške.
     * @param message Detaljna poruka greške.
     * @param cause Originalna iznimka (npr. SQLException ili Spring Exception).
     */
    public BulkMarkOverdueException(String message, Throwable cause) {
        super(message, cause);
    }
}