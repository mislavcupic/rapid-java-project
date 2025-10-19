package hr.algebra.rapid.logisticsandfleetmanagementsystem.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.FutureOrPresent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ShipmentRequest {

    @NotBlank(message = "Tracking number is required")
    private String trackingNumber;

    @NotBlank(message = "Origin address is required")
    private String originAddress;

    @NotBlank(message = "Destination address is required")
    private String destinationAddress;

    @NotNull(message = "Weight is required")
    @DecimalMin(value = "0.1", message = "Weight must be positive")
    private BigDecimal weightKg;

    // Volumen nije obavezan
    private BigDecimal volumeM3;

    @NotNull(message = "Expected delivery date is required")
    @FutureOrPresent(message = "Delivery date cannot be in the past")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime expectedDeliveryDate;

    // Opis nije obavezan
    private String description;

    // Status nije potreban u requestu, jer ga backend postavlja na "PENDING"
}
