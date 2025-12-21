package hr.algebra.rapid.logisticsandfleetmanagementsystem.exceptions;

public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message)
    {
        super(message);
    }
    public TokenExpiredException(String message, Throwable cause) {
        super(message, cause);
    }
}
